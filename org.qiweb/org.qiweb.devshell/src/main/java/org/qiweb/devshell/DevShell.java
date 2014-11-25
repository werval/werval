/*
 * Copyright (c) 2013-2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.devshell;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.qiweb.api.exceptions.PassivationException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.spi.dev.DevShellRebuildException;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.dev.DevShellSPIWrapper;

import static java.util.Collections.singletonMap;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;
import static org.qiweb.runtime.util.AnsiColor.cyan;
import static org.qiweb.runtime.util.AnsiColor.red;
import static org.qiweb.runtime.util.AnsiColor.white;
import static org.qiweb.runtime.util.AnsiColor.yellow;
import static org.qiweb.util.ClassLoaders.printLoadedClasses;
import static org.qiweb.util.ClassLoaders.printURLs;

/**
 * QiWeb DevShell.
 *
 * Bind a build plugin to a QiWeb runtime using a DevShellSPI.
 * <p>
 * Class reloading is implemented using <a href="https://github.com/sonatype/plexus-classworlds">ClassWorlds</a>.
 * <p>
 * See {@literal src/doc/classloader-hierary.png} in the source tree for an overview of how the ClassLoader hierarchy is
 * set up.
 */
public final class DevShell
{
    /**
     * Decorate DevShellSPI to reload classes after a rebuild.
     *
     * This is the decorated instance of DevShellSPI that is passed to the HttpServer.
     */
    private final class DevShellSPIDecorator
        extends DevShellSPIWrapper
    {
        private final Object appInstance;
        private final Method reloadMethod;

        private DevShellSPIDecorator( DevShellSPI wrapped, Object appInstance )
            throws NoSuchMethodException
        {
            super( wrapped );
            this.appInstance = appInstance;
            this.reloadMethod = appInstance.getClass().getMethod(
                "reload",
                new Class<?>[]
                {
                    ClassLoader.class
                }
            );
        }

        @Override
        @SuppressWarnings( "UseSpecificCatch" )
        public synchronized void rebuild()
        {
            if( isSourceChanged() )
            {
                try
                {
                    super.rebuild();
                }
                catch( DevShellRebuildException ex )
                {
                    throw ex;
                }
                catch( Exception ex )
                {
                    throw new DevShellRebuildException( ex );
                }
                try
                {
                    reSetupApplicationRealms();
                    reloadMethod.invoke(
                        appInstance,
                        classWorld.getRealm( currentApplicationRealmID() )
                    );
                }
                catch( Exception ex )
                {
                    throw new DevShellRebuildException( "Unable to reload Application", ex );
                }
            }
        }
    }

    private static final String DEVSHELL_REALM_ID = "DevShellRealm";
    private static final String DEPENDENCIES_REALM_ID = "DependenciesRealm";
    private static final String APPLICATION_REALM_ID = "ApplicationRealm";
    private static final String CONFIG_API_CLASS = "org.qiweb.api.Config";
    private static final String CONFIG_RUNTIME_CLASS = "org.qiweb.runtime.ConfigInstance";
    private static final String CRYPTO_RUNTIME_CLASS = "org.qiweb.runtime.CryptoInstance";
    private static final String APPLICATION_RUNTIME_CLASS = "org.qiweb.runtime.ApplicationInstance";
    private static final String MODE_API_CLASS = "org.qiweb.api.Mode";
    private static final String APPLICATION_SPI_CLASS = "org.qiweb.spi.ApplicationSPI";
    private static final String NETTY_SERVER_CLASS = "org.qiweb.server.netty.NettyServer";
    private static final File RUN_LOCK_FILE = new File( Paths.get( "" ).toAbsolutePath().toFile(), ".devshell.lock" );
    private static final long RUN_LOCK_FILE_POLL_INTERVAL_MILLIS = 500;
    private static final AtomicLong APPLICATION_REALM_COUNT = new AtomicLong( 0L );

    private final DevShellSPI spi;
    private final String configResource;
    private final File configFile;
    private final URL configUrl;
    private final boolean openBrowser;
    private final URLClassLoader originalLoader;
    private ClassWorld classWorld;
    private Object httpServer;
    private volatile boolean running = false;

    public DevShell( DevShellSPI spi )
    {
        this( spi, null, null, null, true );
    }

    public DevShell( DevShellSPI spi, String configResource )
    {
        this( spi, configResource, null, null, true );
    }

    public DevShell( DevShellSPI spi, File configFile )
    {
        this( spi, null, configFile, null, true );
    }

    public DevShell( DevShellSPI spi, URL configUrl )
    {
        this( spi, null, null, configUrl, true );
    }

    public DevShell( DevShellSPI spi, String configResource, File configFile, URL configUrl, boolean openBrowser )
    {
        this.spi = spi;
        this.configResource = configResource;
        this.configFile = configFile;
        this.configUrl = configUrl;
        this.openBrowser = openBrowser;
        this.originalLoader = ( (URLClassLoader) Thread.currentThread().getContextClassLoader() );
    }

    private static String nextApplicationRealmID()
    {
        return APPLICATION_REALM_ID + "-" + APPLICATION_REALM_COUNT.incrementAndGet();
    }

    private static String currentApplicationRealmID()
    {
        return APPLICATION_REALM_ID + "-" + APPLICATION_REALM_COUNT.get();
    }

    @SuppressWarnings( "unchecked" )
    public void start()
    {
        if( lockFileExist() )
        {
            throw new IllegalStateException(
                "Unable to start DevShell, lock file '" + RUN_LOCK_FILE + "' already exists. "
                + "Is another instance already running?"
            );
        }
        System.out.println( white( ">> QiWeb DevShell starting..." ) );
        try
        {
            System.out.println( cyan( "Isolating worlds..." ) );

            setupRealms();
            ClassRealm appRealm = classWorld.getRealm( currentApplicationRealmID() );
            Thread.currentThread().setContextClassLoader( appRealm );

            System.out.println( cyan( "Starting isolated QiWeb Application..." ) );

            // Config
            Class<?> configClass = appRealm.loadClass( CONFIG_API_CLASS );
            Class<?> configRuntimeClass = appRealm.loadClass( CONFIG_RUNTIME_CLASS );
            Constructor<?> configRuntimeCtor = configRuntimeClass.getConstructor(
                new Class<?>[]
                {
                    ClassLoader.class, String.class, File.class, URL.class, Map.class
                }
            );
            Object configInstance = configRuntimeCtor.newInstance(
                new Object[]
                {
                    appRealm, configResource, configFile, configUrl, null
                }
            );
            try
            {
                // Check if the application has an `app.secret` configuration property
                configClass.getMethod(
                    "string",
                    new Class<?>[]
                    {
                        String.class
                    }
                ).invoke(
                    configInstance, "app.secret"
                );
            }
            catch( Exception noAppSecret )
            {
                // The application has no `app.secret` configuration property, generating a random one
                System.out.println(
                    red( "Application has no 'app.secret', generating a random one for development mode!" )
                );
                Object secret = appRealm.loadClass( CRYPTO_RUNTIME_CLASS )
                    .getMethod( "newRandomSecret256BitsHex" )
                    .invoke( null );
                configInstance = configRuntimeCtor.newInstance(
                    new Object[]
                    {
                        appRealm, configResource, configFile, configUrl, singletonMap( "app.secret", secret )
                    }
                );
                System.out.println( red(
                    "  The 'app.secret' will last as long as the development mode is running and survive reloads.\n"
                    + "  If you set it in configuration you'll have to restart the development mode!"
                ) );
            }

            // Application
            Class<?> appClass = appRealm.loadClass( APPLICATION_RUNTIME_CLASS );
            Class<?> modeClass = appRealm.loadClass( MODE_API_CLASS );
            Object appInstance = appClass.getConstructor(
                new Class<?>[]
                {
                    modeClass,
                    configRuntimeClass,
                    ClassLoader.class,
                    DevShellSPI.class
                }
            ).newInstance(
                new Object[]
                {
                    // Dev Mode
                    modeClass.getEnumConstants()[0],
                    configInstance,
                    appRealm,
                    spi
                }
            );

            // Create HttpServer instance
            httpServer = appRealm.loadClass( NETTY_SERVER_CLASS ).newInstance();

            // Set ApplicationSPI on HttpServer
            httpServer.getClass().getMethod(
                "setApplicationSPI",
                new Class<?>[]
                {
                    appRealm.loadClass( APPLICATION_SPI_CLASS )
                }
            ).invoke(
                httpServer,
                appInstance
            );

            // Set DevShellSPI on HttpServer
            httpServer.getClass().getMethod(
                "setDevShellSPI",
                new Class<?>[]
                {
                    DevShellSPI.class
                }
            ).invoke(
                httpServer,
                new DevShellSPIDecorator( spi, appInstance )
            );

            // Get application URL
            String appUrl = applicationUrl( configInstance, configClass );

            // Activate HttpServer
            httpServer.getClass().getMethod( "activate" ).invoke( httpServer );

            System.out.println( white( ">> Ready for requests on " + appUrl + " !" ) );

            // Eventually open default browser
            if( openBrowser && Desktop.isDesktopSupported() )
            {
                try
                {
                    Desktop.getDesktop().browse( new URI( appUrl ) );
                }
                catch( IOException | URISyntaxException ex )
                {
                    System.out.println( yellow( "Unable to open the default browser: " + ex.getMessage() ) );
                }
            }

            // Interrupt Loop
            running = true;
            createLockFile();
            for( ;; )
            {
                if( running && lockFileExist() )
                {
                    Thread.sleep( RUN_LOCK_FILE_POLL_INTERVAL_MILLIS );
                }
                else
                {
                    stop();
                    break;
                }
            }
        }
        catch( DuplicateRealmException | NoSuchRealmException | ClassNotFoundException |
               NoSuchMethodException | SecurityException | InstantiationException |
               IllegalAccessException | IllegalArgumentException | InvocationTargetException |
               InterruptedException ex )
        {
            Throwable cause = ex;
            if( ex instanceof InvocationTargetException )
            {
                cause = ex.getCause();
            }
            String msg = "Unable to start QiWeb DevShell: " + cause.getClass().getSimpleName() + " " + cause.getMessage();
            System.err.println( red( msg ) );
            throw new DevShellStartException( msg, cause );
        }
    }

    /**
     * Stop DevShell.
     */
    // Can be called concurrently by client code and by the lock file polling loop.
    public synchronized void stop()
    {
        if( running )
        {
            running = false;
            System.out.println( white( ">> QiWeb DevShell stopping..." ) );

            // Record all passivation errors here to report them at once at the end
            List<Exception> passivationErrors = new ArrayList<>();

            // Passivate HTTP Server
            try
            {
                passivateHttpServer();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new QiWebException( "Error while passivating HTTP Server: " + ex.getMessage(), ex )
                );
            }

            // Dispose Realms
            try
            {
                disposeRealms();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new QiWebException( "Error while disposing Classworld Realms: " + ex.getMessage(), ex )
                );
            }

            // Remove lock file
            try
            {
                deleteLockFile();
            }
            catch( Exception ex )
            {
                passivationErrors.add( ex );
            }

            // Report errors if any
            if( !passivationErrors.isEmpty() )
            {
                PassivationException ex = new PassivationException( "Unable to stop QiWeb DevShell" );
                System.err.println( red( ex.getMessage() ) );
                for( Exception passivationError : passivationErrors )
                {
                    ex.addSuppressed( passivationError );
                }
                throw ex;
            }
        }
    }

    private boolean lockFileExist()
    {
        return RUN_LOCK_FILE.exists();
    }

    private void createLockFile()
    {
        try
        {
            Files.write( RUN_LOCK_FILE.toPath(), new byte[ 0 ] );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    private void deleteLockFile()
    {
        if( RUN_LOCK_FILE.exists() )
        {
            try
            {
                Files.delete( RUN_LOCK_FILE.toPath() );
            }
            catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }
    }

    private void setupRealms()
        throws DuplicateRealmException
    {
        classWorld = new ClassWorld();

        // DevShell Realm delegates to the original ClassLoader (Either the CLI or the Build Plugin One)
        ClassRealm devRealm = classWorld.newRealm( DEVSHELL_REALM_ID, originalLoader );

        // Dependencies Realm contains all Application dependencies JARs
        // and import QiWeb DevShell and Dev SPI packages from DevShell Realm
        ClassRealm depRealm = classWorld.newRealm( DEPENDENCIES_REALM_ID, null );
        for( URL runtimeClasspathElement : spi.runtimeClassPath() )
        {
            depRealm.addURL( runtimeClasspathElement );
        }
        depRealm.importFrom( devRealm, "org.qiweb.devshell.*" );
        depRealm.importFrom( devRealm, "org.qiweb.spi.dev.*" );

        setupApplicationRealm( depRealm );
    }

    private void reSetupApplicationRealms()
        throws DuplicateRealmException, NoSuchRealmException
    {
        classWorld.disposeRealm( currentApplicationRealmID() );
        setupApplicationRealm( classWorld.getRealm( DEPENDENCIES_REALM_ID ) );
    }

    private void setupApplicationRealm( ClassRealm depRealm )
        throws DuplicateRealmException
    {
        // Application Realm contains all Application compiler output directories
        // and it check itself first and then check Dependencies Realm
        ClassRealm appRealm = classWorld.newRealm( nextApplicationRealmID(), null );
        for( URL applicationClasspathElement : spi.applicationClassPath() )
        {
            appRealm.addURL( applicationClasspathElement );
        }
        appRealm.setParentRealm( depRealm );
    }

    private void disposeRealms()
        throws NoSuchRealmException
    {
        if( classWorld != null )
        {
            classWorld.disposeRealm( DEVSHELL_REALM_ID );
            classWorld.disposeRealm( DEPENDENCIES_REALM_ID );
            classWorld.disposeRealm( currentApplicationRealmID() );
            classWorld = null;
        }
    }

    private void passivateHttpServer()
        throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if( httpServer != null )
        {
            httpServer.getClass().getMethod( "passivate" ).invoke( httpServer );
            httpServer = null;
        }
    }

    private String applicationUrl( Object configInstance, Class<?> configClass )
        throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class<?>[] argsTypes = new Class<?>[]
        {
            String.class
        };
        String httpHost = (String) configClass
            .getMethod( "string", argsTypes )
            .invoke( configInstance, QIWEB_HTTP_ADDRESS );
        int httpPort = (int) configClass
            .getMethod( "intNumber", argsTypes )
            .invoke( configInstance, QIWEB_HTTP_PORT );
        if( "127.0.0.1".equals( httpHost ) )
        {
            httpHost = "localhost";
        }
        return "http://" + httpHost + ":" + httpPort + "/";
    }

    // This is dead debug code waiting for a necromancer
    private void printRealms()
        throws NoSuchRealmException
    {
        ClassRealm devRealm = classWorld.getRealm( DEVSHELL_REALM_ID );
        ClassRealm depRealm = classWorld.getRealm( DEPENDENCIES_REALM_ID );
        ClassRealm appRealm = classWorld.getRealm( currentApplicationRealmID() );
        System.out.println( white( "Realms / ClassLoaders" ) );
        System.out.println( yellow( "Original ClassLoader" ) );
        printURLs( originalLoader );
        printLoadedClasses( originalLoader );
        System.out.println( yellow( "DevShell ClassLoader" ) );
        printURLs( devRealm );
        printLoadedClasses( devRealm );
        System.out.println( yellow( "Dependencies ClassLoader" ) );
        printURLs( depRealm );
        printLoadedClasses( depRealm );
        System.out.println( yellow( "Application ClassLoader" ) );
        printURLs( appRealm );
        printLoadedClasses( appRealm );
    }
}
