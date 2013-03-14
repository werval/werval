package org.qiweb.http.routes;

public class RoutesParserProvider
    implements RoutesProvider
{

    private String routesString;

    public RoutesParserProvider()
    {
    }

    public RoutesParserProvider( String routesString )
    {
        changeRoutes( routesString );
    }

    public final void changeRoutes( String routesString )
    {
        this.routesString = routesString;
    }

    @Override
    public Routes routes()
    {
        System.out.println( "===> PARSING ROUTES <===" );
        return RouteBuilder.parseRoutes( routesString );
    }
}
