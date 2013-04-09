package org.qiweb.runtime.routes;

import org.qiweb.api.Application;
import org.qiweb.api.routes.Routes;

public class RoutesParserProvider
    implements RoutesProvider
{

    private final String routesString;

    /**
     * Empty routes provider.
     */
    public RoutesParserProvider()
    {
        this( "" );
    }

    public RoutesParserProvider( String routesString )
    {
        this.routesString = routesString;
    }

    @Override
    public Routes routes( Application application )
    {
        return RouteBuilder.parseRoutes( application, routesString );
    }
}
