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
package org.qiweb.runtime.dev;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import org.qiweb.api.Application;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.runtime.routes.RouteBuilder;
import org.qiweb.runtime.routes.RoutesProvider;

import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Development Mode Routes Provider.
 * <p>
 *     Use 'routes.conf' from the Application classpath root and add Documentation at /@doc plus various introspection
 *     resources.
 * </p>
 */
public class DevShellRoutesProvider
    implements RoutesProvider
{
    private static final String DEVSHELL_ROUTES =
                                "\n"
                                + "GET /@doc org.qiweb.lib.controllers.Default.seeOther( String url = '/@doc/index.html' )\n"
                                + "GET /@doc/api org.qiweb.lib.controllers.Default.seeOther( String url = '/@doc/api/index.html' )\n"
                                + "GET /@doc/*path org.qiweb.lib.controllers.ClasspathResources.resource( String basepath = 'org/qiweb/doc/html/', String path )\n"
                                + "GET /@config org.qiweb.lib.controllers.Introspect.config\n"
                                + "GET /@version org.qiweb.lib.controllers.Introspect.version\n";

    @Override
    public Routes routes( Application application )
    {
        URL routesUrl = application.classLoader().getResource( "routes.conf" );
        if( routesUrl == null )
        {
            return RouteBuilder.parseRoutes( application, DEVSHELL_ROUTES );
        }
        try( InputStream input = routesUrl.openStream() )
        {
            Scanner scanner = new Scanner( input, UTF_8.name() ).useDelimiter( "\\A" );
            String routes = scanner.hasNext() ? scanner.next() : "";
            return RouteBuilder.parseRoutes( application, DEVSHELL_ROUTES + routes );
        }
        catch( IOException ex )
        {
            throw new QiWebRuntimeException( "Unable to load routes.conf from the classpath", ex );
        }
    }
}
