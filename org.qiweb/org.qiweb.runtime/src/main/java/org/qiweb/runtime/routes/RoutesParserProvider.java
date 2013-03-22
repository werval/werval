package org.qiweb.runtime.routes;

import org.qiweb.api.routes.Routes;

public class RoutesParserProvider
    implements RoutesProvider
{

    private final String routesString;

    public RoutesParserProvider()
    {
        this( "" ); // NO ROUTES
    }

    public RoutesParserProvider( String routesString )
    {
        this.routesString = routesString;
    }

    @Override
    public Routes routes()
    {
        System.out.println( "===> PARSING ROUTES <===" );
        return RouteBuilder.parseRoutes( routesString );
    }

    @Override
    public Routes routes( ClassLoader loader )
    {
        System.out.println( "===> PARSING ROUTES using " + loader + " <===" );
        return RouteBuilder.parseRoutes( loader, routesString );
    }
}
