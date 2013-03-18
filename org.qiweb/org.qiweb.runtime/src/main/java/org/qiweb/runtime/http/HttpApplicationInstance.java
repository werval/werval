package org.qiweb.runtime.http;

import org.qiweb.runtime.http.routes.Routes;
import org.qiweb.runtime.http.routes.RoutesProvider;

public class HttpApplicationInstance
    implements HttpApplication
{

    private final RoutesProvider routesProvider;
    private ClassLoader classLoader;

    public HttpApplicationInstance( ClassLoader classLoader, RoutesProvider routesProvider )
    {
        this.routesProvider = routesProvider;
        this.classLoader = classLoader;
    }

    @Override
    public final ClassLoader classLoader()
    {
        return classLoader;
    }

    public final void changeClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    @Override
    public final Routes routes()
    {
        return routesProvider.routes( classLoader );
    }
}
