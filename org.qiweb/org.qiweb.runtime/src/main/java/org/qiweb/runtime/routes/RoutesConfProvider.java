/**
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import org.qiweb.api.Application;
import org.qiweb.api.routes.Routes;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;

/**
 * Routes Provider using 'routes.conf' from the Application classpath root.
 */
public class RoutesConfProvider
    implements RoutesProvider
{

    private final String routesResourceName;

    public RoutesConfProvider()
    {
        this( "routes.conf" );
    }

    public RoutesConfProvider( String routesResourceName )
    {
        this.routesResourceName = routesResourceName;
    }

    @Override
    public Routes routes( Application application )
    {
        URL routesUrl = application.classLoader().getResource( routesResourceName );
        if( routesUrl == null )
        {
            return RouteBuilder.parseRoutes( application, Strings.EMPTY );
        }
        try( InputStream input = routesUrl.openStream() )
        {
            Scanner scanner = new Scanner( input, application.defaultCharset().name() ).useDelimiter( "\\A" );
            String routes = scanner.hasNext() ? scanner.next() : "";
            return RouteBuilder.parseRoutes( application, routes );
        }
        catch( IOException ex )
        {
            throw new QiWebRuntimeException( "Unable to load routes.conf from the classpath", ex );
        }
    }
}
