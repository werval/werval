/**
 * Copyright (c) 2013 the original author or authors
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.dev.DevShellSPIWrapper;

import static org.qiweb.runtime.util.AnsiColor.cyan;
import static org.qiweb.runtime.util.AnsiColor.red;
import static org.qiweb.runtime.util.AnsiColor.white;
import static org.qiweb.runtime.util.AnsiColor.yellow;
import static org.qiweb.runtime.util.ClassLoaders.printLoadedClasses;
import static org.qiweb.runtime.util.ClassLoaders.printURLs;

/**
 * QiWeb DevShell.
 * <p>Bind a build plugin to a QiWeb runtime using a DevShellSPI.</p>
 * <p>Class reloading is implemented using <a href="https://github.com/sonatype/plexus-classworlds">ClassWorlds</a>.</p>
 */
public final class DevShell
{
    /**
     * Decorate DevShellSPI to reload classes after a rebuild.
     * <p>This is the decorated instance of DevShellSPI that is passed to the HttpServer.</p>
     */
    private final class DevShellSPIDecorator
        extends DevShellSPIWrapper
    {

        private final Object httpAppInstance;

        private DevShellSPIDecorator( DevShellSPI wrapped, Object httpAppInstance )
        {
            super( wrapped );
            this.httpAppInstance = httpAppInstance;
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
                    ClassLoader appLoader = classWorld.getRealm( currentApplicationRealmID() );
                    Class<?>[] paramTypes = new Class<?>[]
                    {
                        ClassLoader.class
                    };
                    httpAppInstance.getClass().getMethod( "reload", paramTypes ).invoke( httpAppInstance, appLoader );
                }
                catch( DuplicateRealmException | NoSuchRealmException |
                       NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
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
            Class<?> configClass = appRealm.loadClass( "org.qiweb.api.Config" );
            Object configInstance = appRealm.loadClass( "org.qiweb.runtime.ConfigInstance" ).getConstructor(
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
            Class<?> routesProviderClass = appRealm.loadClass( "org.qiweb.runtime.routes.RoutesProvider" );
            Object routesProviderInstance = appRealm.loadClass( "org.qiweb.runtime.dev.DevShellRoutesProvider" ).newInstance();

            // Application
            Class<?> appClass = appRealm.loadClass( "org.qiweb.runtime.ApplicationInstance" );
            Class<?> modeClass = appRealm.loadClass( "org.qiweb.api.Application$Mode" );
            Object appInstance = appClass.getConstructor(
                new Class<?>[]
                {
                    modeClass,
                    configClass,
                    ClassLoader.class,
                    routesProviderClass
                }
            ).newInstance(
                new Object[]
                {
                    // Dev Mode
                    modeClass.getEnumConstants()[0],
                    configInstance,
                    appRealm,
                    routesProviderInstance
                }
            );

            // HttpServer
            Object httpServer = appRealm.loadClass( "org.qiweb.runtime.server.HttpServerInstance" ).
                getConstructor( new Class<?>[]
                    {
                        String.class, appClass, DevShellSPI.class
                } ).
                newInstance( new Object[]
                    {
                        "devshell-httpserver", appInstance, new DevShellSPIDecorator( spi, appInstance )
                } );

            httpServer.getClass().getMethod( "registerPassivationShutdownHook" ).invoke( httpServer );
            httpServer.getClass().getMethod( "activate" ).invoke( httpServer );

            // ---------------------------------------------------------------------------------------------------------
            // printRealms();
            Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    stop();
                }
            }, "qiweb-devshell-shutdown" ) );

            System.out.println( white( ">> Ready for requests!" ) );
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

    public void stop()
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
        classWorld.disposeRealm( DEVSHELL_REALM_ID );
        classWorld.disposeRealm( DEPENDENCIES_REALM_ID );
        classWorld.disposeRealm( currentApplicationRealmID() );
        classWorld = null;
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
