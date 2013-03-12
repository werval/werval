package org.qiweb.http.routes;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import java.util.Collections;
import java.util.List;
import org.codeartisans.java.toolbox.Couple;
import org.junit.Test;
import org.qiweb.http.controllers.Result;
import org.qiweb.http.routes.RouteBuilder.MethodRecorder;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.functional.Iterables.count;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.skip;
import static org.qi4j.functional.Iterables.toList;
import static org.qiweb.http.routes.RouteBuilder.route;

/**
 * Assert that Routes and Route types behave correctly and that RouteBuilder is able to parse all routes definitions.
 */
public class RoutesTest
{

    public static interface MyController
    {

        Result test();

        Result another( String id, Integer slug );

        Result index();

        Result foo();

        Result bar();
    }

    @Test
    public void givenRoutesBuildFromCodeWhenToStringExpectCorrectOutput()
    {
        Route route = route( GET ).on( "/foo/:id/bar/:slug" ).
            to( MyController.class, new MethodRecorder<MyController>()
        {
            @Override
            protected void call( MyController controller )
            {
                controller.another( p( "id", String.class ), p( "slug", Integer.class ) );
            }
        } ).newInstance();

        // Java 8 - Lambda Expressions
        // Route route = route( GET ).on( "/foo/:id/bar/:slug" ).
        //     to( MyController.class, { c -> c.another( p( "id", String.class ), p( "slug", Integer.class ) ) } ).
        //     newInstance();

        assertThat( route.toString(), equalTo( "GET /foo/:id/bar/:slug org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )" ) );
    }

    /**
     * Declarative setup of route parsing tests.
     */
    @SuppressWarnings( "unchecked" )
    public static enum RoutesToTest
    {

        TRANSIENT( "GET / org.qiweb.http.routes.RoutesTest$MyController.test() transient",
                   GET, "/", MyController.class, "test" ),
        SERVICE( "GET / org.qiweb.http.routes.RoutesTest$MyController.test() service",
                 GET, "/", MyController.class, "test" ),
        SIMPLEST( "GET / org.qiweb.http.routes.RoutesTest$MyController.test()",
                  GET, "/", MyController.class, "test" ),
        TEST( "  POST    /foo/bar    org.qiweb.http.routes.RoutesTest$MyController.test()",
              POST, "/foo/bar", MyController.class, "test" ),
        ANOTHER( "GET /foo/:id/bar/:slug org.qiweb.http.routes.RoutesTest$MyController.another(String    id ,Integer slug   )",
                 GET, "/foo/:id/bar/:slug", MyController.class, "another", new RoutesToTest.Params()
        {
            @Override
            public Iterable<Couple<String, Class<?>>> params()
            {
                return iterable( new Couple<String, Class<?>>( "id", String.class ),
                                 new Couple<String, Class<?>>( "slug", Integer.class ) );
            }
        } ),
        ANOTHER_ONE( "GET /foo/:id/bar/:slug/cathedral/:id org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )",
                     GET, "/foo/:id/bar/:slug/cathedral/:id", MyController.class, "another", new RoutesToTest.Params()
        {
            @Override
            public Iterable<Couple<String, Class<?>>> params()
            {
                return iterable( new Couple<String, Class<?>>( "id", String.class ),
                                 new Couple<String, Class<?>>( "slug", Integer.class ) );
            }
        } ),
        //        TEST_NO_PARENTHESIS( "  POST    /foo/bar    org.qiweb.http.routes.RoutesTest$MyController.test",
        //                             POST, "/foo/bar", MyController.class, "test" ),
        WRONG_STRING_1( "WRONG /route", IllegalRouteException.class ),
        WRONG_STRING_2( "", IllegalRouteException.class ),
        WRONG_STRING_3( null, IllegalRouteException.class ),
        WRONG_STRING_4( "# GET / org.qiweb.http.routes.RoutesTest$MyController.test()", IllegalRouteException.class ),
        WRONG_CONTROLLER_1( "GET /foo /bar com.acme.Controller.method()", IllegalRouteException.class ),
        WRONG_CONTROLLER_2( "GET / unknown.Type.method()", IllegalRouteException.class ),
        WRONG_METHOD_1( "GET / org.qiweb.http.routes.RoutesTest$MyController.unknownMethod()", IllegalRouteException.class ),
        WRONG_METHOD_2( "GET / org.qiweb.http.routes.RoutesTest$MyController.test( WhatTheHeck param )", IllegalRouteException.class ),
        WRONG_PARAMS_1( "GET /nothing/at/all org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_2( "GET /:wrong org.qiweb.http.routes.RoutesTest$MyController.test()", IllegalRouteException.class ),
        WRONG_PARAMS_3( "GET /foo/:id/bar org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_4( "GET /foo/:id/bar/:slug/cathedral/:bazar org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_5( "GET /foo/:id/bar/:slugf org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_6( "GET /foo/:idf/bar/:slug org.qiweb.http.routes.RoutesTest$MyController.another( String id, Integer slug )", IllegalRouteException.class ),
        WRONG_PARAMS_99( "", IllegalRouteException.class );
        private String routeString;
        private HttpMethod httpMethod;
        private String path;
        private Class<?> controllerType;
        private String controllerMethod;
        private Iterable<Couple<String, Class<?>>> pathParams;
        private Class<? extends Exception> expectedException;

        private RoutesToTest( String routeString, Class<? extends Exception> expectedException )
        {
            this.routeString = routeString;
            this.expectedException = expectedException;
        }

        private RoutesToTest( String routeString, HttpMethod httpMethod, String path, Class<?> controllerType, String controllerMethod )
        {
            this.routeString = routeString;
            this.httpMethod = httpMethod;
            this.path = path;
            this.controllerType = controllerType;
            this.controllerMethod = controllerMethod;
            this.pathParams = Collections.emptyList();
        }

        private RoutesToTest( String routeString, HttpMethod httpMethod, String path, Class<?> controllerType, String controllerMethod, RoutesToTest.Params params )
        {
            this( routeString, httpMethod, path, controllerType, controllerMethod );
            this.pathParams = params.params();
        }

        public static interface Params
        {

            Iterable<Couple<String, Class<?>>> params();
        }
    }

    @Test
    public void givenUnderTestRoutesWhenParsingExpectCorrectResult()
        throws Exception
    {
        for( RoutesToTest refRoute : RoutesToTest.values() )
        {
            System.out.println( "Parsing route: " + refRoute.routeString );
            try
            {
                Route route = RouteBuilder.parseRoute( refRoute.routeString );
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
        Routes routes = RouteBuilder.parseRoutes( "\n" + RoutesToTest.ANOTHER.routeString + "\n\n \n# ignore me\n  # me too  \n" + RoutesToTest.TEST.routeString + "\n" );

        assertThat( count( routes ), is( 2L ) );

        Route one = first( routes );
        Route two = first( skip( 1, routes ) );

        assertRoute( one, RoutesToTest.ANOTHER );
        assertRoute( two, RoutesToTest.TEST );
    }

    @Test
    public void givenRoutesWhenMatchingExpectCorrectRoutes()
    {
        Route index = RouteBuilder.parseRoute( "GET / " + MyController.class.getName() + ".index()" );
        Route foo = RouteBuilder.parseRoute( "GET /foo " + MyController.class.getName() + ".foo()" );
        Route bar = RouteBuilder.parseRoute( "GET /bar " + MyController.class.getName() + ".bar()" );
        Route another = RouteBuilder.parseRoute( "GET /foo/:id/bar/:slug " + MyController.class.getName() + ".another(String id,Integer slug)" );

        Routes routes = RouteBuilder.routes( index, foo, bar, another );

        assertThat( routes.route( new DefaultHttpRequest( HTTP_1_1, GET, "/" ) ), equalTo( index ) );
        assertThat( routes.route( new DefaultHttpRequest( HTTP_1_1, GET, "/foo" ) ), equalTo( foo ) );
        assertThat( routes.route( new DefaultHttpRequest( HTTP_1_1, GET, "/bar" ) ), equalTo( bar ) );
        assertThat( routes.route( new DefaultHttpRequest( HTTP_1_1, GET, "/foo/1234567890/bar/42" ) ), equalTo( another ) );
    }

    private void assertRoute( Route route, RoutesToTest refRoute )
    {
        assertThat( "HTTP Method", route.httpMethod(), equalTo( refRoute.httpMethod ) );
        assertThat( "URI/Path", route.path(), equalTo( refRoute.path ) );
        assertThat( "Controller Type", route.controllerType().getName(), equalTo( refRoute.controllerType.getName() ) );
        assertThat( "Controller Method", route.controllerMethodName(), equalTo( refRoute.controllerMethod ) );
        assertThat( "Parameters Count", count( route.controllerParams() ), equalTo( count( refRoute.pathParams ) ) );

        List<Couple<String, Class<?>>> routeParameters = toList( route.controllerParams() );
        List<Couple<String, Class<?>>> refRouteParameters = toList( refRoute.pathParams );
        for( int idx = 0; idx < routeParameters.size(); idx++ )
        {
            Couple<String, Class<?>> routeParam = routeParameters.get( idx );
            Couple<String, Class<?>> refRouteParam = refRouteParameters.get( idx );
            assertThat( "Parameter Name", routeParam.left(), equalTo( refRouteParam.left() ) );
            assertThat( "Parameter Type", routeParam.right().getName(), equalTo( refRouteParam.right().getName() ) );
        }
    }
}
