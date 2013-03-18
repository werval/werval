package org.qiweb.runtime.http;

import org.qiweb.runtime.http.routes.Routes;
import org.qiweb.runtime.http.routes.RoutesProvider;

public class HttpApplicationInstance
    implements HttpApplication
{

    private final RoutesProvider routesProvider;
    private final ClassLoader classLoader;

    public HttpApplicationInstance( ClassLoader classLoader, RoutesProvider routesProvider )
    {
        this.routesProvider = routesProvider;
        this.classLoader = classLoader;
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
}
