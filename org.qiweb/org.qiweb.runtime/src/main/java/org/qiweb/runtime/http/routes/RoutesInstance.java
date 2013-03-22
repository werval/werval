package org.qiweb.runtime.http.routes;

import java.util.ArrayList;
import java.util.Iterator;
import org.qiweb.api.http.HttpRequestHeader;

/**
 * Instance of Routes.
 */
/* package */ class RoutesInstance
    extends ArrayList<Route>
    implements Routes
{

    private static final long serialVersionUID = 1L;

    @Override
    public Route route( HttpRequestHeader requestHeader )
    {
        for( Route route : this )
        {
            if( route.satisfiedBy( requestHeader ) )
            {
                return route;
            }
        }
        throw new RouteNotFoundException( requestHeader.method(), requestHeader.uri() );
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