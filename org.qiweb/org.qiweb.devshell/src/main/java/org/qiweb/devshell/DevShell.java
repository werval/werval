package org.qiweb.devshell;

import java.io.File;
import org.qiweb.spi.dev.DevShellSPI;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import static org.qiweb.runtime.util.ClassLoaders.*;

public class DevShell
{

    private class DevShellSPIDecorator
        implements DevShellSPI
    {

        private final DevShellSPI decorated;

        public DevShellSPIDecorator( DevShellSPI decorated )
        {
            this.decorated = decorated;
        }

        @Override
        public String name()
        {
            return decorated.name();
        }

        @Override
        public File rootDir()
        {
            return decorated.rootDir();
        }

        @Override
        public File buildDir()
        {
            return decorated.buildDir();
        }

        @Override
        public Set<File> mainSources()
        {
            return decorated.mainSources();
        }

        @Override
        public File mainOutput()
        {
            return decorated.mainOutput();
        }

        @Override
        public URL[] mainClassPath()
        {
            return decorated.mainClassPath();
        }

        @Override
        public boolean hasMainChanged()
        {
            return decorated.hasMainChanged();
        }

        @Override
        public void rebuildMain()
        {
            try
            {
                decorated.rebuildMain();
                reSetupApplicationRealm();
            }
            catch( NoSuchRealmException | DuplicateRealmException ex )
            {
                throw new RuntimeException( "Unable to reload Application: " + ex.getMessage(), ex );
            }
        }

        @Override
        public Set<File> testSources()
        {
            return decorated.testSources();
        }

        @Override
        public File testOutput()
        {
            return decorated.testOutput();
        }

        @Override
        public URL[] testClassPath()
        {
            return decorated.testClassPath();
        }

        @Override
        public boolean hasTestChanged()
        {
            return decorated.hasTestChanged();
        }

        @Override
        public void rebuildTest()
        {
            decorated.rebuildTest();
        }
    }
    private static final String DEVSHELL_REALM_ID = "DevShellRealm";
    private static final String DEPENDENCIES_REALM_ID = "DependenciesRealm";
    private static final String APPLICATION_REALM_ID = "ApplicationRealm";
    private static final AtomicLong APPLICAITON_REALM_COUNT = new AtomicLong( 0L );
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
        return APPLICATION_REALM_ID + "-" + APPLICAITON_REALM_COUNT.incrementAndGet();
    }

    private static String currentApplicationRealmID()
    {
        return APPLICATION_REALM_ID + "-" + APPLICAITON_REALM_COUNT.get();
    }

    @SuppressWarnings( "unchecked" )
    public void start()
    {
        highlight( ">> QiWeb DevShell for " + spi.name() + " starting..." );
        try
        {
            info( "Compiling..." );
            spi.rebuildMain();

            info( "Isolating worlds..." );

            warn( "DevShell Class ClassLoader is: " + getClass().getClassLoader() );
            warn( "Current Thread Context ClassLoader is: " + originalLoader );

            setupRealms();

            // Enter sandboxed ClassLoader

            ClassRealm appRealm = classWorld.getRealm( currentApplicationRealmID() );

            Class<?> routesProviderClass = appRealm.loadClass( "org.qiweb.runtime.http.routes.RoutesProvider" );
            Object routesProviderInstance = appRealm.loadClass( "org.qiweb.runtime.http.routes.RoutesParserProvider" ).getConstructor( new Class<?>[]
            {
                String.class
            } ).newInstance( new Object[]
            {
                "GET / com.acme.app.FakeController.index()" // Routes
            } );
            Class<?> httpAppClass = appRealm.loadClass( "org.qiweb.runtime.http.HttpApplication" );
            Object httpAppInstance = appRealm.loadClass( "org.qiweb.runtime.http.HttpApplicationInstance" ).getConstructor( new Class<?>[]
            {
                ClassLoader.class,
                routesProviderClass
            } ).newInstance( new Object[]
            {
                appRealm,
                routesProviderInstance
            } );
            Class<?> httpServerInstanceClass = appRealm.loadClass( "org.qiweb.runtime.http.server.HttpServerInstance" );
            Constructor<?> httpServerInstanceCtor = httpServerInstanceClass.getConstructor( new Class<?>[]
            {
                String.class, String.class, int.class, httpAppClass, DevShellSPI.class
            } );

            highlight( "Attempting to instanciate HttpServerInstance from Dependencies ClassRealm loaded class" );
            Object httpServer = httpServerInstanceCtor.newInstance( new Object[]
            {
                "devshell-httpserver",
                "127.0.0.1", 23023,
                httpAppInstance,
                new DevShellSPIDecorator( spi )
            } );

            httpServer.getClass().getMethod( "activateService" ).invoke( httpServer );

            // ---------------------------------------------------------------------------------------------------------

            printRealms();

            // DumbNetty netty = new DumbNetty();
            // netty.start();

            highlight( ">> Ready for requests!" );
            Thread.sleep( Long.MAX_VALUE );
        }
        catch( Exception ex )
        {
            String msg = "Unable to start QiWeb DevShell: " + ex.getMessage();
            error( msg );
            throw new RuntimeException( msg, ex );
        }
    }

    public void stop()
    {
        try
        {
            highlight( ">> QiWeb DevShell for " + spi.name() + " stopping..." );
            disposeRealms();
        }
        catch( Exception ex )
        {
            String msg = "Unable to stop QiWeb DevShell: " + ex.getMessage();
            error( msg );
            throw new RuntimeException( msg, ex );
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
        for( URL jarUrl : jars( spi.mainClassPath() ) )
        {
            depRealm.addURL( jarUrl );
        }
        depRealm.importFrom( devRealm, "org.qiweb.devshell.*" );
        depRealm.importFrom( devRealm, "org.qiweb.spi.dev.*" );

        // Application Realm contains all Application compiler output directories
        // and it check itself first and then check Dependencies Realm
        ClassRealm appRealm = classWorld.newRealm( nextApplicationRealmID(), null );
        for( URL dirUrl : directories( spi.mainClassPath() ) )
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
        for( URL dirUrl : directories( spi.mainClassPath() ) )
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
        highlight( "Realms / ClassLoaders" );
        warn( "Original ClassLoader" );
        printURLs( originalLoader );
        printLoadedClasses( originalLoader );
        warn( "DevShell ClassLoader" );
        printURLs( devRealm );
        printLoadedClasses( devRealm );
        warn( "Dependencies ClassLoader" );
        printURLs( depRealm );
        printLoadedClasses( depRealm );
        warn( "Application ClassLoader" );
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
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void success( String message )
    {
        System.out.println( ANSI_RESET + ANSI_GREEN + message + ANSI_RESET );
    }

    public static void highlight( String message )
    {
        System.out.println( ANSI_RESET + ANSI_WHITE + message + ANSI_RESET );
    }

    public static void info( String message )
    {
        System.out.println( ANSI_RESET + message + ANSI_RESET );
    }

    public static void warn( String message )
    {
        System.out.println( ANSI_RESET + ANSI_YELLOW + message + ANSI_RESET );
    }

    public static void error( String message )
    {
        System.out.println( ANSI_RESET + ANSI_RED + message + ANSI_RESET );
    }
}
