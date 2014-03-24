/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.api.routes;

import java.util.List;
import org.qiweb.api.http.Method;
import org.qiweb.api.routes.internal.RouteBuilderContext;

/**
 * Fluent builder to create new Route instances.
 *
 * Routes can be parsed from a {@literal String} or created using this very builder.
 * The toString() method of {@literal Route} instances created using this builder output a {@literal String} that is
 * parseable by this builder.
 * <p>
 * Let's see some routes:
 * <pre>
 * GET / controllers.Acme.index()
 * GET /login controllers.Acme.loginForm()
 * POST /login controllers.Acme.login()
 * POST /subscribe controllers.Acme.subscribe()
 * GET /:nickname controllers.Acme.home( String nickname )
 * </pre>
 * Here is a complete example using the API:
 * <pre>
 * import org.qiweb.api.routes.Route;
 * import org.qiweb.api.routes.RoutesBuilder;
 *
 * import static org.qiweb.api.routes.RoutesBuilder.p;
 *
 * RouteBuilder builder = ... ; //
 * Route route = builder.route( "GET" )
 *      .on( "/foo/:slug/bar/:id" )
 *      .to( MyController.class, c -&gt;  c.another( p( "id", String.class ), p( "slug", Integer.class, 2 ) ) )
 *      .build();
 *
 * System.out.println( route.toString() );
 * </pre>
 * The last line would output:
 * <pre>GET /foo/bar/:id f.q.n.MyController.another( String id, Integer slug = 2 )</pre>
 */
public interface RouteBuilder
{
    /**
     * Start a RouteDeclaration.
     *
     * @return Empty RouteDeclaration
     */
    RouteDeclaration route();

    /**
     * Start a RouteDeclaration for a HTTP method.
     *
     * @param httpMethod HTTP method
     *
     * @return RouteDeclaration using the given HTTP method
     */
    RouteDeclaration route( String httpMethod );

    /**
     * Start a RouteDeclaration for a HTTP method.
     *
     * @param httpMethod HTTP method
     *
     * @return RouteDeclaration using the given HTTP method
     */
    RouteDeclaration route( Method httpMethod );

    /**
     * Parse Routes from Strings.
     *
     * @return Route Parser
     */
    RouteParser parse();

    /**
     * Route Declaration.
     */
    interface RouteDeclaration
    {
        /**
         * Set Route method.
         *
         * @param httpMethod HTTP method
         *
         * @return a new RouteDeclaration using the given HTTP method
         */
        RouteDeclaration method( String httpMethod );

        /**
         * Set Route path.
         *
         * @param path Route path
         *
         * @return a new RouteDeclaration using the given path
         */
        RouteDeclaration on( String path );

        /**
         * Set Route controller type, method and parameters.
         *
         * @param <T>            Parameterized controller type
         * @param controllerType the controller type
         * @param callRecorder   a closure for the controller type to record a method invocation
         *
         * @return a new RouteDeclaration using the given controller method
         */
        <T> RouteDeclaration to( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

        /**
         * Set Route modifiers.
         *
         * @param modifiers Modifiers
         *
         * @return a new RouteDeclaration using the given modifiers
         */
        RouteDeclaration modifiedBy( String... modifiers );

        /**
         * Create a new Route instance.
         *
         * @return a new Route instance.
         */
        Route build();
    }

    /**
     * Route Parser.
     */
    interface RouteParser
    {
        /**
         * Parse a textual definition of multiple routes, one per line.
         *
         * Ignore lines that begins with a # or are empty.
         *
         * @param routesString Routes as String
         *
         * @return Parsed Routes
         *
         * @throws org.qiweb.api.exceptions.IllegalRouteException when one of the textual route definitions is invalid
         */
        List<Route> routes( String routesString );

        /**
         * Parse a single textual route definition to a Route instance.
         *
         * @param routeString a textual route definition
         *
         * @return a new Route instance
         *
         * @throws org.qiweb.api.exceptions.IllegalRouteException when the textual route definition is invalid
         */
        Route route( String routeString );
    }

    /**
     * Record a new method parameter.
     *
     * @param <T>  the parameterized parameter type
     * @param name the parameter name
     * @param type the parmeter type
     *
     * @return an ignored default value
     */
    static <T> T p( String name, Class<T> type )
    {
        RouteBuilderContext.recordMethodParameter( name, type );
        return null;
    }

    /**
     * Record a new method parameter with forced value.
     *
     * @param <T>         the parameterized parameter type
     * @param name        the parameter name
     * @param type        the parmeter type
     * @param forcedValue the forced value
     *
     * @return the forced value
     */
    static <T> T p( String name, Class<T> type, T forcedValue )
    {
        RouteBuilderContext.recordMethodParameter( name, type, forcedValue );
        return forcedValue;
    }
}
