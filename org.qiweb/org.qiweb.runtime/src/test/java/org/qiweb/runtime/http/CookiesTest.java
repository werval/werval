package org.qiweb.runtime.http;

import java.util.Arrays;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CookiesTest
    extends AbstractQiWebTest
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
    protected String routesString()
    {
        return "GET /set/:name/:value org.qiweb.runtime.http.CookiesTest$Controller.setCookie( String name, String value )\n"
               + "GET /remove/:name org.qiweb.runtime.http.CookiesTest$Controller.removeCookie( String name )\n"
               + "GET /mirror org.qiweb.runtime.http.CookiesTest$Controller.mirrorCookies";
    }

    @Test
    public void testSetCookie()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "set/foo/bar" ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        Header[] setCookieHeaders = response.getHeaders( "Set-Cookie" );
        System.out.println( Arrays.toString( setCookieHeaders ) );
        assertTrue( setCookieHeaders.length > 0 );
        String setFooCookie = null;
        for( Header setCookieHeader : setCookieHeaders )
        {
            if( setCookieHeader.getValue().startsWith( "foo=" ) )
            {
                setFooCookie = setCookieHeader.getValue();
            }
        }
        assertThat( setFooCookie, notNullValue() );
        assertThat( setFooCookie.split( ";" )[0].trim().split( "=" )[1], equalTo( "bar" ) );
    }

    @Test
    public void testRemoveCookie()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "remove/foo" ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        Header[] setCookieHeaders = response.getHeaders( "Set-Cookie" );
        System.out.println( Arrays.toString( setCookieHeaders ) );
        assertTrue( setCookieHeaders.length > 0 );
        String setFooCookie = null;
        for( Header setCookieHeader : setCookieHeaders )
        {
            if( setCookieHeader.getValue().startsWith( "foo=" ) )
            {
                setFooCookie = setCookieHeader.getValue();
            }
        }
        assertThat( setFooCookie, notNullValue() );
        assertThat( setFooCookie.split( ";" )[0].trim().split( "=" ).length, equalTo( 1 ) );
    }

    @Test
    public void testMirrorCookies()
        throws Exception
    {
        DefaultHttpClient client = newHttpClientInstance();
        CookieStore clientCookieStore = new BasicCookieStore();
        BasicClientCookie bazarCookie = new BasicClientCookie( "bazar", "cathedral" );
        bazarCookie.setVersion( 0 );
        bazarCookie.setDomain( "127.0.0.1" );
        bazarCookie.setPath( "/" );
        clientCookieStore.addCookie( bazarCookie );

        HttpContext clientContext = new BasicHttpContext();
        clientContext.setAttribute( ClientContext.COOKIE_STORE, clientCookieStore );

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "mirror" ), clientContext );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        Header[] setCookieHeaders = response.getHeaders( "Set-Cookie" );
        System.out.println( Arrays.toString( setCookieHeaders ) );
        assertTrue( setCookieHeaders.length > 0 );
        String setBazarCookie = null;
        for( Header setCookieHeader : setCookieHeaders )
        {
            if( setCookieHeader.getValue().startsWith( "bazar=" ) )
            {
                setBazarCookie = setCookieHeader.getValue();
            }
        }
        assertThat( setBazarCookie, notNullValue() );
        assertThat( setBazarCookie.split( ";" )[0].trim().split( "=" )[1], equalTo( "cathedral" ) );
    }
}
