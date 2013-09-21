package org.qiweb.runtime.http;

import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.test.QiWebTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.expect;

public class CookiesTest
    extends QiWebTest
{

    public static class Controller
        extends org.qiweb.api.controllers.Controller
    {

        public Outcome setCookie( String name, String value )
        {
            response().cookies().set( name, value );
            return outcomes().ok().build();
        }

        public Outcome removeCookie( String name )
        {
            response().cookies().invalidate( name );
            return outcomes().ok().build();
        }

        public Outcome mirrorCookies()
        {
            for( Cookie cookie : request().cookies() )
            {
                response().cookies().set( cookie.name(), cookie.value() );
            }
            return outcomes().ok().build();
        }
    }

    @Override
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider(
            "GET /set/:name/:value org.qiweb.runtime.http.CookiesTest$Controller.setCookie( String name, String value )\n"
            + "GET /remove/:name org.qiweb.runtime.http.CookiesTest$Controller.removeCookie( String name )\n"
            + "GET /mirror org.qiweb.runtime.http.CookiesTest$Controller.mirrorCookies" );
    }

    @Test
    public void testSetCookie()
    {
        expect().cookie( "foo", "bar" ).
            when().get( "/set/foo/bar" );
    }

    @Test
    public void testRemoveCookie()
    {
        expect().cookie( "foo", "" ).
            when().get( "/remove/foo" );
    }

    @Test
    public void testMirrorCookies()
    {
        given().cookie( "bazar", "cathedral" ).
            expect().cookie( "bazar", "cathedral" ).
            when().get( "/mirror" );
    }
}
