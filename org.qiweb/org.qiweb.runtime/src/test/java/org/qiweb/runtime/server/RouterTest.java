package org.qiweb.runtime.server;

import org.junit.Test;
import org.qiweb.test.AbstractQiWebTest;

import static com.jayway.restassured.RestAssured.expect;

public class RouterTest
    extends AbstractQiWebTest
{

    @Override
    protected String routesString()
    {
        return "GET / com.acme.app.FakeControllerInstance.index()\n"
               + "GET /foo com.acme.app.FakeControllerInstance.foo()\n"
               + "GET /bar com.acme.app.FakeControllerInstance.bar()\n"
               + "GET /:id/:slug com.acme.app.FakeControllerInstance.another( String id, Integer slug )";
    }

    @Test
    public void testRoutes()
        throws Exception
    {
        expect().
            statusCode( 200 ).
            when().
            get( BASE_URL );
        expect().
            statusCode( 404 ).
            when().
            post( BASE_URL );
        expect().
            statusCode( 200 ).
            when().
            get( BASE_URL + "foo" );
        expect().
            statusCode( 200 ).
            when().
            get( BASE_URL + "bar" );
        expect().
            statusCode( 404 ).
            when().
            get( BASE_URL + "bazar" );
        expect().
            statusCode( 200 ).
            when().
            get( BASE_URL + "azertyuiop/1234" );
    }
}
