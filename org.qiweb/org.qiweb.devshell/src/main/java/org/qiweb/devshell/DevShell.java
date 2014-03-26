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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.dev.DevShellSPIWrapper;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;
import static org.qiweb.runtime.util.AnsiColor.cyan;
import static org.qiweb.runtime.util.AnsiColor.red;
import static org.qiweb.runtime.util.AnsiColor.white;
import static org.qiweb.runtime.util.AnsiColor.yellow;
import static org.qiweb.runtime.util.ClassLoaders.printLoadedClasses;
import static org.qiweb.runtime.util.ClassLoaders.printURLs;

/**
 * QiWeb DevShell.
 *
 * Bind a build plugin to a QiWeb runtime using a DevShellSPI.
 * <p>
 * Class reloading is implemented using <a href="https://github.com/sonatype/plexus-classworlds">ClassWorlds</a>.
 */
public final class DevShell
{
    /**
     * Decorate DevShellSPI to reload classes after a rebuild.
     * <p>
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
        public synchronized void rebuild()
        {
            if( isSourceChanged() )
            {
                try
                {
                    super.rebuild();
                    reSetupApplicationRealms();
                    reloadMethod.invoke(
                        appInstance,
                        classWorld.getRealm( currentApplicationRealmID() )
                    );
                }
                catch( DuplicateRealmException | NoSuchRealmException |
                       SecurityException | IllegalAccessException | IllegalArgumentException |
                       InvocationTargetException ex )
                {
                    throw new QiWebDevShellException( "Unable to reload Application: " + ex.getMessage(), ex );
                }
            }
        }
    }

    private static final String DEVSHELL_REALM_ID = "DevShellRealm";
    private static final String DEPENDENCIES_REALM_ID = "DependenciesRealm";
    private static final String APPLICATION_REALM_ID = "ApplicationRealm";
    private static final String CONFIG_API_CLASS = "org.qiweb.api.Config";
    private static final String CONFIG_RUNTIME_CLASS = "org.qiweb.runtime.ConfigInstance";
    private static final String ROUTES_PROVIDER_CLASS = "org.qiweb.runtime.routes.RoutesProvider";
    private static final String DEVSHELL_ROUTES_PROVIDER_CLASS = "org.qiweb.runtime.dev.DevShellRoutesProvider";
    private static final String APPLICATION_RUNTIME_CLASS = "org.qiweb.runtime.ApplicationInstance";
    private static final String MODE_API_CLASS = "org.qiweb.api.Mode";
    private static final String APPLICATION_SPI_CLASS = "org.qiweb.spi.ApplicationSPI";

    private static final AtomicLong APPLICATION_REALM_COUNT = new AtomicLong( 0L );
    private final DevShellSPI spi;
    private final URLClassLoader originalLoader;
    private ClassWorld classWorld;

    public DevShell( DevShellSPI spi )
    {
        this.spi = spi;
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
            Object configInstance = appRealm.loadClass( CONFIG_RUNTIME_CLASS ).getConstructor(
                new Class<?>[]
                {
                    ClassLoader.class
                }
            ).newInstance(
                new Object[]
                {
                    appRealm
                }
            );

            // RoutesProvider
            Class<?> routesProviderClass = appRealm.loadClass( ROUTES_PROVIDER_CLASS );
            Object routesProviderInstance = appRealm.loadClass( DEVSHELL_ROUTES_PROVIDER_CLASS ).newInstance();

            // Application
            Class<?> appClass = appRealm.loadClass( APPLICATION_RUNTIME_CLASS );
            Class<?> modeClass = appRealm.loadClass( MODE_API_CLASS );
            Object appInstance = appClass.getConstructor(
                new Class<?>[]
                {
                    modeClass,
                    configClass,
                    ClassLoader.class,
                    routesProviderClass,
                    DevShellSPI.class
                }
            ).newInstance(
                new Object[]
                {
                    // Dev Mode
                    modeClass.getEnumConstants()[0],
                    configInstance,
                    appRealm,
                    routesProviderInstance,
                    spi
                }
            );

            // Create HttpServer instance
            Object httpServer = appRealm.loadClass( "org.qiweb.server.netty.NettyServer" ).newInstance();

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

            // Register shutdown hook and activate HttpServer
            httpServer.getClass().getMethod( "registerPassivationShutdownHook" ).invoke( httpServer );
            httpServer.getClass().getMethod( "activate" ).invoke( httpServer );

            // ---------------------------------------------------------------------------------------------------------
            // printRealms();
            Runtime.getRuntime().addShutdownHook( new Thread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        stop();
                    }
                },
                "qiweb-devshell-shutdown"
            ) );

            System.out.println( white( ">> Ready for requests on " + appUrl + " !" ) );
            Thread.sleep( Long.MAX_VALUE );
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
            throw new QiWebDevShellException( msg, cause );
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

    /**
     * Stop DevShell.
     */
    // Can be called concurrently by client code and automatic JVM shutdown hook.
    // This is why this method is synchronized and disposeRealm() check classWorld for null.
    public synchronized void stop()
    {
        try
        {
            System.out.println( white( ">> QiWeb DevShell stopping..." ) );
            disposeRealms();
        }
        catch( Exception ex )
        {
            String msg = "Unable to stop QiWeb DevShell: " + ex.getMessage();
            System.err.println( red( msg ) );
            throw new QiWebDevShellException( msg, ex );
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
