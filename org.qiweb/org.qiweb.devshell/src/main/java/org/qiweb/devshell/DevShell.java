package org.qiweb.devshell;

import java.io.File;
import org.qiweb.spi.dev.DevShellSPI;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.qiweb.spi.dev.DevShellSPIWrapper;

import static org.qiweb.devshell.Color.*;
import static org.qiweb.runtime.util.ClassLoaders.*;

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
                    reSetupApplicationRealm();
                    httpAppInstance.getClass().getMethod( "reload", new Class<?>[]
                    {
                        ClassLoader.class
                    } ).
                        invoke( httpAppInstance, classWorld.getRealm( currentApplicationRealmID() ) );
                }
                catch( Exception ex )
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
        white( ">> QiWeb DevShell starting..." );
        try
        {
            cyan( "Isolating worlds..." );
            yellow( "DevShell Class ClassLoader is: " + getClass().getClassLoader() );
            yellow( "Current Thread Context ClassLoader is: " + originalLoader );

            setupRealms();

            // Enter sandboxed ClassLoader

            ClassRealm appRealm = classWorld.getRealm( currentApplicationRealmID() );

            // Config
            Class<?> configClass = appRealm.loadClass( "org.qiweb.api.Config" );
            Object configInstance = appRealm.loadClass( "org.qiweb.runtime.ConfigInstance" ).getConstructor( new Class<?>[]
            {
                ClassLoader.class
            } ).newInstance( new Object[]
            {
                appRealm
            } );

            // RoutesProvider
            Class<?> routesProviderClass = appRealm.loadClass( "org.qiweb.runtime.routes.RoutesProvider" );
            Object routesProviderInstance = appRealm.loadClass( "org.qiweb.runtime.routes.RoutesParserProvider" ).
                getConstructor( new Class<?>[]
            {
                String.class
            } ).
                newInstance( new Object[]
            {
                // Routes
                "GET /favicon.ico org.qiweb.controller.Default.notFound()\n"
                + "GET / com.acme.app.FakeControllerInstance.index()\n"
                + "GET /resources/*path org.qiweb.controller.MetaInfResources.resource( String path )"
            } );

            // Application
            Class<?> appClass = appRealm.loadClass( "org.qiweb.runtime.ApplicationInstance" );
            Class<?> modeClass = appRealm.loadClass( "org.qiweb.api.Application$Mode" );
            Object appInstance = appClass.getConstructor( new Class<?>[]
            {
                modeClass,
                configClass,
                ClassLoader.class,
                routesProviderClass
            } ).
                newInstance( new Object[]
            {
                // Dev Mode
                modeClass.getEnumConstants()[0],
                configInstance,
                appRealm,
                routesProviderInstance
            } );

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

            httpServer.getClass().getMethod( "activate" ).invoke( httpServer );

            // ---------------------------------------------------------------------------------------------------------

            // printRealms();

            white( ">> Ready for requests!" );
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
            red( msg );
            throw new QiWebDevShellException( msg, cause );
        }
    }

    public void stop()
    {
        try
        {
            white( ">> QiWeb DevShell stopping..." );
            disposeRealms();
        }
        catch( Exception ex )
        {
            String msg = "Unable to stop QiWeb DevShell: " + ex.getMessage();
            red( msg );
            throw new QiWebDevShellException( msg, ex );
        }
    }

    private void setupRealms()
        throws DuplicateRealmException
    {
        classWorld = new ClassWorld();

        // DevShell Realm delegates to the original ClassLoader (The Build Plugin One)
        ClassRealm devRealm = classWorld.newRealm( DEVSHELL_REALM_ID, originalLoader );

        // Dependencies Realm contains all Application dependencies JARs
        // and import QiWeb DevShell and Dev SPI packages from current ClassLoader (The Build Plugin One)
        ClassRealm depRealm = classWorld.newRealm( DEPENDENCIES_REALM_ID, null );
        for( URL jarUrl : jars( spi.classPath() ) )
        {
            depRealm.addURL( jarUrl );
        }
        depRealm.importFrom( devRealm, "org.qiweb.devshell.*" );
        depRealm.importFrom( devRealm, "org.qiweb.spi.dev.*" );

        // Application Realm contains all Application compiler output directories
        // and it check itself first and then check Dependencies Realm
        ClassRealm appRealm = classWorld.newRealm( nextApplicationRealmID(), null );
        for( URL dirUrl : directories( spi.classPath() ) )
        {
            appRealm.addURL( dirUrl );
        }
        appRealm.setParentRealm( depRealm );
    }

    private void reSetupApplicationRealm()
        throws DuplicateRealmException, NoSuchRealmException
    {
        classWorld.disposeRealm( currentApplicationRealmID() );
        ClassRealm appRealm = classWorld.newRealm( nextApplicationRealmID(), null );
        for( URL dirUrl : directories( spi.classPath() ) )
        {
            appRealm.addURL( dirUrl );
        }
        appRealm.setParentRealm( classWorld.getRealm( DEPENDENCIES_REALM_ID ) );
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
        white( "Realms / ClassLoaders" );
        yellow( "Original ClassLoader" );
        printURLs( originalLoader );
        printLoadedClasses( originalLoader );
        yellow( "DevShell ClassLoader" );
        printURLs( devRealm );
        printLoadedClasses( devRealm );
        yellow( "Dependencies ClassLoader" );
        printURLs( depRealm );
        printLoadedClasses( depRealm );
        yellow( "Application ClassLoader" );
        printURLs( appRealm );
        printLoadedClasses( appRealm );
    }

    private URL[] directories( URL[] urls )
    {
        Set<URL> set = new LinkedHashSet<>();
        for( URL url : urls )
        {
            if( !url.toString().endsWith( ".jar" ) )
            {
                File dir = new File( url.getFile() );
                if( !dir.exists() && !dir.mkdirs() )
                {
                    throw new QiWebDevShellException( "Unable to create inexistant classpath directory: " + dir );
                }
                set.add( url );
            }
        }
        return set.toArray( new URL[ set.size() ] );
    }

    private URL[] jars( URL[] urls )
    {
        Set<URL> set = new LinkedHashSet<>();
        for( URL url : urls )
        {
            if( url.toString().endsWith( ".jar" ) )
            {
                set.add( url );
            }
        }
        return set.toArray( new URL[ set.size() ] );
    }
}
