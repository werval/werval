package org.qiweb.runtime.http;

import io.netty.channel.DefaultEventExecutorGroup;
import io.netty.channel.EventExecutorGroup;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.runtime.http.routes.Routes;
import org.qiweb.runtime.http.routes.RoutesProvider;

public class HttpApplicationInstance
    implements HttpApplication
{

    private final RoutesProvider routesProvider;
    private final ClassLoader classLoader;
    private final EventExecutorGroup httpExecutors;

    public HttpApplicationInstance( ClassLoader classLoader, RoutesProvider routesProvider )
    {
        this.routesProvider = routesProvider;
        this.classLoader = classLoader;
        this.httpExecutors = new DefaultEventExecutorGroup( 0, new ThreadFactory()
        {
            private final AtomicLong count = new AtomicLong( 0L );

            @Override
            public Thread newThread( Runnable runnable )
            {
                Thread thread = new Thread( runnable, "http-executor-" + count.getAndIncrement() );

                // This should be unecessary if application libraries stop relying on the TCCL
                // See http://njbartlett.name/2012/10/23/dreaded-thread-context-classloader.html
                // thread.setContextClassLoader( classLoader() );

                return thread;
            }
        } );
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    @Override
    public Routes routes()
    {
        return routesProvider.routes( classLoader );
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
}
