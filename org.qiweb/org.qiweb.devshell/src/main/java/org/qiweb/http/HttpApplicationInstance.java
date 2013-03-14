package org.qiweb.http;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.ResourceFilter;
import io.netty.channel.DefaultEventExecutorGroup;
import io.netty.channel.EventExecutorGroup;
import java.net.URLClassLoader;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.http.routes.Routes;
import org.qiweb.http.routes.RoutesProvider;
import org.qiweb.http.server.HttpApplicationClassLoader;

/**
 * Scratch attempt to a development mode Qi4j Application.
 */
public class HttpApplicationInstance
    implements HttpApplication
{

    private final RoutesProvider routesProvider;
    private final HttpApplicationClassLoader appClassLoader;
    private final EventExecutorGroup httpExecutors;

    public HttpApplicationInstance( RoutesProvider routesProvider )
    {
        this.routesProvider = routesProvider;
        this.appClassLoader = new HttpApplicationClassLoader( ( (URLClassLoader) ( Thread.currentThread().getContextClassLoader() ) ).getURLs(),
                                                              HttpApplicationClassLoader.class.getClassLoader() );
        this.httpExecutors = new DefaultEventExecutorGroup( 0, new ThreadFactory()
        {
            private final AtomicLong count = new AtomicLong( 0L );

            @Override
            public Thread newThread( Runnable runnable )
            {
                Thread thread = new Thread( runnable, "http-executor-" + count.getAndIncrement() );
                thread.setContextClassLoader( appClassLoader );
                return thread;
            }
        } );
        //preloadApplicationClasses();
        //ClassLoaders.printClassLoaders( appClassLoader );
    }

    @Override
    public ClassLoader classLoader()
    {
        return appClassLoader;
    }

    @Override
    public Routes routes()
    {
        return routesProvider.routes();
    }

    @Override
    public <T> T controllerInstance( Class<T> controllerType )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public EventExecutorGroup httpExecutors()
    {
        return httpExecutors;
    }

    @SuppressWarnings( "unchecked" )
    private void preloadApplicationClasses()
    {
        try
        {
            ClassPath jvmClassPath = new ClassPathFactory().createFromJVM();
            String[] allClassesResources = jvmClassPath.findResources( "/", new ResourceFilter()
            {
                @Override
                public boolean match( String packageName, String resourceName )
                {
                    //System.out.println( "FILTERING: " + packageName + "." + resourceName );
                    return resourceName.endsWith( ".class" ) && ( packageName.startsWith( "org/qi4j" )
                                                                  || packageName.startsWith( "com/acme/app" ) );
                }
            } );
            for( String classResource : allClassesResources )
            {
                System.out.println( "WILL PRELOAD: " + classResource );
                String classToPreload = classResource.replaceAll( "/", "." ).substring( 0, classResource.length() - ".class".length() );
                System.out.println( "    AS: " + classToPreload );
                appClassLoader.loadClass( classToPreload );
            }
        }
        catch( Exception ex )
        {
            throw new RuntimeException( "Unable to preload Application classes", ex );
        }
    }
}
