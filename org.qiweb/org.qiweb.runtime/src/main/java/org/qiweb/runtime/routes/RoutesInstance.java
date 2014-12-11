/*
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.runtime.routes;

import io.werval.api.exceptions.RouteNotFoundException;
import io.werval.api.http.RequestHeader;
import io.werval.api.routes.Route;
import io.werval.api.routes.Routes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.qiweb.runtime.util.Iterables;

import static io.werval.util.Strings.NEWLINE;

/**
 * Instance of Routes.
 */
public class RoutesInstance
    extends ArrayList<Route>
    implements Routes
{
    private static final long serialVersionUID = 1L;

    public RoutesInstance( Route... routes )
    {
        this( Arrays.asList( routes ) );
    }

    public RoutesInstance( Iterable<Route> routes )
    {
        super();
        Iterables.addAll( this, routes );
    }

    @Override
    public Route route( RequestHeader requestHeader )
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
        int methodPadLen = 0, pathPadLen = 0;
        for( Route route : this )
        {
            if( route.httpMethod().name().length() > methodPadLen )
            {
                methodPadLen = route.httpMethod().name().length();
            }
            if( route.path().length() > pathPadLen )
            {
                pathPadLen = route.path().length();
            }
        }
        StringBuilder sb = new StringBuilder();
        for( Iterator<Route> it = iterator(); it.hasNext(); )
        {
            RouteInstance route = (RouteInstance) it.next();
            sb.append( route.toString( methodPadLen, pathPadLen, null ) );
            if( it.hasNext() )
            {
                sb.append( NEWLINE );
            }
        }
        return sb.toString();
    }
}
