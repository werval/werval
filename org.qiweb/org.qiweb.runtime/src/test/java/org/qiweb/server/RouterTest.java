package org.qiweb.server;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;

public class RouterTest
{

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET / com.acme.app.FakeControllerInstance.index()\n"
        + "GET /foo com.acme.app.FakeControllerInstance.foo()\n"
        + "GET /bar com.acme.app.FakeControllerInstance.bar()\n"
        + "GET /:id/:slug com.acme.app.FakeControllerInstance.another( String id, Integer slug )" ) );

    @Test
    public void testRoutes()
        throws Exception
    {
        expect().
            statusCode( 200 ).
            when().
            get( "/" );
        expect().
            statusCode( 404 ).
            when().
            post( "/" );
        expect().
            statusCode( 200 ).
            when().
            get( "/foo" );
        expect().
            statusCode( 200 ).
            when().
            get( "/bar" );
        expect().
            statusCode( 404 ).
            when().
            get( "/bazar" );
        expect().
            statusCode( 200 ).
            when().
            get( "/azertyuiop/1234" );
    }

}
