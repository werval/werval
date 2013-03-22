package org.qiweb.runtime;

import org.qiweb.api.QiWebApplication;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.routes.RoutesProvider;

public class QiWebApplicationInstance
    implements QiWebApplication
{

    private final RoutesProvider routesProvider;
    private ClassLoader classLoader;

    public QiWebApplicationInstance( ClassLoader classLoader, RoutesProvider routesProvider )
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
