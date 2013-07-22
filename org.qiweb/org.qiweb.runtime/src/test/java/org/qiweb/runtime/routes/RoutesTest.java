package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.routes.ControllerParams.ControllerParam;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.http.CookiesInstance;
import org.qiweb.runtime.http.HeadersInstance;
import org.qiweb.runtime.http.RequestHeaderInstance;
import org.qiweb.runtime.http.QueryStringInstance;
import org.qiweb.runtime.routes.RouteBuilder.MethodRecorder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.functional.Iterables.count;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.skip;
import static org.qiweb.runtime.routes.RouteBuilder.route;

/**
 * Assert that Routes and Route types behave correctly and that RouteBuilder is able to parse all routes definitions.
 */
public class RoutesTest
{

    @Test
    public void givenRoutesBuildFromCodeWhenToStringExpectCorrectOutput()
    {
        Route route = route( "GET" ).on( "/foo/:id/bar/:slug" ).
            to( FakeController.class, new MethodRecorder<FakeController>()
        {
            @Override
            protected void call( FakeController controller )
            {
                controller.another( p( "id", String.class ), p( "slug", Integer.class ) );
            }
        } ).modifiedBy( "service", "foo" ).newInstance();

        // Java 8 - Lambda Expressions
        // Route route = route( GET ).on( "/foo/:id/bar/:slug" ).
        //     to( FakeController.class, { c -> c.another( p( "id", String.class ), p( "slug", Integer.class ) ) } ).
        //     modifiedBy( "service", "foo" ).newInstance();

        assertThat( route.toString(), equalTo( "GET /foo/:id/bar/:slug com.acme.app.FakeController.another( String id, Integer slug ) service foo" ) );
    }

    /**
     * Declarative setup of route parsing tests.
     */
    @SuppressWarnings( "unchecked" )
    public static enum RoutesToTest
    {

        TRANSIENT( "GET / com.acme.app.FakeController.test() transient",
                   "GET", "/", FakeController.class, "test", Arrays.asList( "transient" ) ),
        SERVICE( "GET / com.acme.app.FakeController.test() service",
                 "GET", "/", FakeController.class, "test", Arrays.asList( "service" ) ),
        SIMPLEST( "GET / com.acme.app.FakeController.test()",
                  "GET", "/", FakeController.class, "test" ),
        TEST( "  POST    /foo/bar    com.acme.app.FakeController.test()",
              "POST", "/foo/bar", FakeController.class, "test" ),
        ANOTHER( "GET /foo/:id/bar/:slug com.acme.app.FakeController.another(String    id ,Integer slug   )",
                 "GET", "/foo/:id/bar/:slug", FakeController.class, "another", new RoutesToTest.Params()
        {
            @Override
            public Map<String, Class<?>> params()
            {
                Map<String, Class<?>> params = new LinkedHashMap<>();
                params.put( "id", String.class );
                params.put( "slug", Integer.class );
                return params;
            }
        } ),
        ANOTHER_ONE( "GET /foo/bar/:slug/cathedral/:id com.acme.app.FakeController.another( String id, Integer slug )",
                     "GET", "/foo/bar/:slug/cathedral/:id", FakeController.class, "another", new RoutesToTest.Params()
        {
            @Override
            public Map<String, Class<?>> params()
            {
                Map<String, Class<?>> params = new LinkedHashMap<>();
                params.put( "id", String.class );
                params.put( "slug", Integer.class );
                return params;
            }
        } ),
        WILDCARDS_1( "GET /static/*path com.acme.app.FakeController.wild( String path )",
                     "GET", "/static/*path", FakeController.class, "wild", new RoutesToTest.Params()
        {
            @Override
            public Map<String, Class<?>> params()
            {
                Map<String, Class<?>> params = new LinkedHashMap<>();
                params.put( "path", String.class );
                return params;
            }
        } ),
        WILDCARDS_2( "GET /d/*path/:slug com.acme.app.FakeController.another( String path, Integer slug )",
                     "GET", "/d/*path/:slug", FakeController.class, "another", new RoutesToTest.Params()
        {
            @Override
            public Map<String, Class<?>> params()
            {
                Map<String, Class<?>> params = new LinkedHashMap<>();
                params.put( "path", String.class );
                params.put( "slug", Integer.class );
                return params;
            }
        } ),
        NO_PARENTHESIS_1( "  POST    /foo/bar    com.acme.app.FakeController.test",
                          "POST", "/foo/bar", FakeController.class, "test" ),
        NO_PARENTHESIS_2( "  POST    /foo/bar    com.acme.app.FakeController.test transient",
                          "POST", "/foo/bar", FakeController.class, "test", Arrays.asList( "transient" ) ),
        WRONG_STRING_1( "WRONG /route", IllegalRouteException.class ),
        WRONG_STRING_2( "", IllegalRouteException.class ),
        WRONG_STRING_3( null, IllegalRouteException.class ),
        WRONG_STRING_4( "# GET / com.acme.app.FakeController.test()", IllegalRouteException.class ),
        WRONG_STRING_5( "GET foo/bar com.acme.app.FakeController.test()", IllegalRouteException.class ),
        WRONG_CONTROLLER_1( "GET /foo /bar com.acme.Controller.method()", IllegalRouteException.class ),
        WRONG_CONTROLLER_2( "GET / unknown.Type.method()", IllegalRouteException.class ),
        WRONG_METHOD_1( "GET / com.acme.app.FakeController.unknownMethod()", IllegalRouteException.class ),
        WRONG_METHOD_2( "GET / com.acme.app.FakeController.test( WhatTheHeck param )", IllegalRouteException.class ),
        WRONG_METHOD_3( "GET / com.acme.app.FakeController.noOutcome", IllegalRouteException.class ),
        WRONG_PARAMS_1( "GET /nothing/at/all com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_2( "GET /:wrong com.acme.app.FakeController.test()", IllegalRouteException.class ),
        WRONG_PARAMS_3( "GET /foo/:id/bar com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_4( "GET /foo/:id/bar/:slug/cathedral/:bazar com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_5( "GET /foo/:id/bar/:slugf com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_6( "GET /foo/:idf/bar/:slug com.acme.app.FakeController.another( java.lang.String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_7( "GET /a/*path com.acme.app.FakeController.wild( path )", IllegalRouteException.class ),
        WRONG_PARAMS_8( "GET /a/*path/:id/:slug com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_9( "GET /a/:id com.acme.app.FakeController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_99( "", IllegalRouteException.class );
        private String routeString;
        private String httpMethod;
        private String path;
        private Class<?> controllerType;
        private String controllerMethod;
        private Map<String, Class<?>> pathParams;
        private List<String> modifiers;
        private Class<? extends Exception> expectedException;

        private RoutesToTest( String routeString, Class<? extends Exception> expectedException )
        {
            this.routeString = routeString;
            this.expectedException = expectedException;
        }

        private RoutesToTest( String routeString, String httpMethod, String path, Class<?> controllerType, String controllerMethod )
        {
            this.routeString = routeString;
            this.httpMethod = httpMethod;
            this.path = path;
            this.controllerType = controllerType;
            this.controllerMethod = controllerMethod;
            this.pathParams = Collections.emptyMap();
            this.modifiers = Collections.emptyList();
        }

        private RoutesToTest( String routeString, String httpMethod, String path, Class<?> controllerType, String controllerMethod, List<String> modifiers )
        {
            this( routeString, httpMethod, path, controllerType, controllerMethod );
            this.modifiers = modifiers;
        }

        private RoutesToTest( String routeString, String httpMethod, String path, Class<?> controllerType, String controllerMethod, RoutesToTest.Params params )
        {
            this( routeString, httpMethod, path, controllerType, controllerMethod );
            this.pathParams = params.params();
        }

        public static interface Params
        {

            Map<String, Class<?>> params();
        }
    }

    @Test
    public void givenUnderTestRoutesWhenParsingExpectCorrectResult()
        throws Exception
    {
        Application app = new ApplicationInstance( new RoutesParserProvider() );
        for( RoutesToTest refRoute : RoutesToTest.values() )
        {
            System.out.println( "Parsing route: " + refRoute.routeString );
            try
            {
                Route route = RouteBuilder.parseRoute( app, refRoute.routeString );
                System.out.println( "Parsed  route: " + route );
                assertRoute( route, refRoute );
            }
            catch( Exception ex )
            {
                if( refRoute.expectedException == null || !refRoute.expectedException.isAssignableFrom( ex.getClass() ) )
                {
                    throw ex;
                }
            }
        }
    }

    @Test
    public void givenMultipleRoutesStringWhenParsingExpectCorrectRoutes()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider(
            "\n" + RoutesToTest.ANOTHER.routeString + "\n\n \n# ignore me\n  # me too  \n" + RoutesToTest.TEST.routeString + "\n" ) );

        assertThat( count( app.routes() ), is( 2L ) );

        Route one = first( app.routes() );
        Route two = first( skip( 1, app.routes() ) );

        assertRoute( one, RoutesToTest.ANOTHER );
        assertRoute( two, RoutesToTest.TEST );
    }

    @Test
    public void givenRoutesWhenMatchingExpectCorrectRoutes()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider() );
        Route index = RouteBuilder.parseRoute( app, "GET / " + FakeController.class.getName() + ".index()" );
        Route foo = RouteBuilder.parseRoute( app, "GET /foo " + FakeController.class.getName() + ".foo()" );
        Route bar = RouteBuilder.parseRoute( app, "GET /bar " + FakeController.class.getName() + ".bar()" );
        Route another = RouteBuilder.parseRoute( app, "GET /foo/:id/bar/:slug " + FakeController.class.getName() + ".another(String id,Integer slug)" );

        Routes routes = RouteBuilder.routes( index, foo, bar, another );

        assertThat( routes.route( reqHeadForGet( "/" ) ), equalTo( index ) );
        assertThat( routes.route( reqHeadForGet( "/foo" ) ), equalTo( foo ) );
        assertThat( routes.route( reqHeadForGet( "/bar" ) ), equalTo( bar ) );
        assertThat( routes.route( reqHeadForGet( "/foo/1234567890/bar/42" ) ), equalTo( another ) );
    }

    private RequestHeader reqHeadForGet( String path )
    {
        return new RequestHeaderInstance( "identity", "HTTP/1.1",
                                          "GET", "http://localhost" + path, path,
                                          new QueryStringInstance(), new HeadersInstance(), new CookiesInstance() );
    }

    private void assertRoute( Route route, RoutesToTest refRoute )
    {
        assertThat( "HTTP Method", route.httpMethod(), equalTo( refRoute.httpMethod ) );
        assertThat( "URI/Path", route.path(), equalTo( refRoute.path ) );
        assertThat( "Controller Type", route.controllerType().getName(), equalTo( refRoute.controllerType.getName() ) );
        assertThat( "Controller Method", route.controllerMethodName(), equalTo( refRoute.controllerMethod ) );
        assertThat( "Parameters Count", count( ( (RouteInstance) route ).controllerParams().names() ), equalTo( count( refRoute.pathParams.keySet() ) ) );
        assertThat( "Modifiers Count", count( route.modifiers() ), equalTo( count( refRoute.modifiers ) ) );

        ControllerParamsInstance routeParameters = (ControllerParamsInstance) ( (RouteInstance) route ).controllerParams();
        Map<String, Class<?>> refRouteParameters = new LinkedHashMap<>( refRoute.pathParams );
        for( Entry<String, ControllerParam> routeEntry : routeParameters.asMap().entrySet() )
        {
            String routeParamName = routeEntry.getKey();
            assertThat( "Parameter " + routeParamName, routeEntry.getValue().type().getName(), equalTo( refRouteParameters.get( routeParamName ).getName() ) );
        }

        List<String> routeModifiers = new ArrayList<>( route.modifiers() );
        List<String> refRouteModifiers = new ArrayList<>( refRoute.modifiers );
        for( int idx = 0; idx < routeModifiers.size(); idx++ )
        {
            String routeModifier = routeModifiers.get( idx );
            assertThat( "Modifier " + routeModifier, routeModifier, equalTo( refRouteModifiers.get( idx ) ) );
        }
    }
}
