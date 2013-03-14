package org.qiweb.http.routes;

import io.netty.handler.codec.http.HttpRequest;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Instance of Routes.
 */
/* package */ class RoutesInstance
    extends ArrayList<Route>
    implements Routes
{

    private static final long serialVersionUID = 1L;

    @Override
    public Route route( HttpRequest httpRequest )
    {
        for( Route route : this )
        {
            if( route.satisfiedBy( httpRequest ) )
            {
                return route;
            }
        }
        throw new RouteNotFoundException( httpRequest.getMethod(), httpRequest.getUri() );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Route> it = iterator();
        while( it.hasNext() )
        {
            Route route = it.next();
            sb.append( route.toString() );
            if( it.hasNext() )
            {
                sb.append( "\n" );
            }
        }
        return sb.toString();
    }
}