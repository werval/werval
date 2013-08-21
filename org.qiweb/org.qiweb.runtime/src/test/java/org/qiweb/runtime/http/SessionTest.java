package org.qiweb.runtime.http;

import java.util.Collections;
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
import org.junit.Before;
import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Session;
import org.qiweb.runtime.http.CookiesInstance.CookieInstance;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;

public class SessionTest
    extends AbstractQiWebTest
{

    public static class Controller
        extends org.qiweb.api.controllers.Controller
    {

        public Outcome show()
        {
            return outcomes().ok( session().asMap().toString() ).build();
        }

        public Outcome set( String name, String value )
        {
            session().set( name, value );
            return show();
        }

        public Outcome clear()
        {
            session().clear();
            return show();
        }
    }
    private String sessionCookieName;

    @Override
    protected String routesString()
    {
        return "GET /set/:name/:value org.qiweb.runtime.http.SessionTest$Controller.set( String name, String value )\n"
               + "GET /clear org.qiweb.runtime.http.SessionTest$Controller.clear\n"
               + "GET /show org.qiweb.runtime.http.SessionTest$Controller.show";
    }

    @Before
    public void beforeSessionTest()
    {
        sessionCookieName = application().config().string( APP_SESSION_COOKIE_NAME );
    }

    @Test
    public void testSetSession()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "set/foo/bar" ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        Header[] setCookieHeaders = response.getHeaders( "Set-Cookie" );
        assertTrue( setCookieHeaders.length > 0 );
        String sessionCookie = null;
        for( Header setCookieHeader : setCookieHeaders )
        {
            if( setCookieHeader.getValue().startsWith( sessionCookieName + "=" ) )
            {
                sessionCookie = setCookieHeader.getValue().substring( ( sessionCookieName + "=" ).length() );
                sessionCookie = sessionCookie.substring( 0, sessionCookie.indexOf( ";" ) );
            }
        }
        assertThat( sessionCookie, notNullValue() );
        Session session = new SessionInstance( application().config(), application().crypto(), new CookieInstance( sessionCookieName, "/", null, false, sessionCookie, true ) );
        assertThat( session, notNullValue() );
        assertThat( session.get( "foo" ), equalTo( "bar" ) );
    }

    @Test
    public void testValidSession()
        throws Exception
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        DefaultHttpClient client = newHttpClientInstance();
        CookieStore clientCookieStore = new BasicCookieStore();
        BasicClientCookie sessionCookie = new BasicClientCookie( sessionCookieName, signedSession );
        sessionCookie.setVersion( 0 );
        sessionCookie.setDomain( "127.0.0.1" );
        sessionCookie.setPath( "/" );
        clientCookieStore.addCookie( sessionCookie );

        HttpContext clientContext = new BasicHttpContext();
        clientContext.setAttribute( ClientContext.COOKIE_STORE, clientCookieStore );

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "show" ), clientContext );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( "{foo=bar}" ) );
    }

    @Test
    public void testInvalidSession()
        throws Exception
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();

        // Invalidate Session Data
        signedSession = signedSession.substring( 1 );

        DefaultHttpClient client = newHttpClientInstance();
        CookieStore clientCookieStore = new BasicCookieStore();
        BasicClientCookie sessionCookie = new BasicClientCookie( sessionCookieName, signedSession );
        sessionCookie.setVersion( 0 );
        sessionCookie.setDomain( "127.0.0.1" );
        sessionCookie.setPath( "/" );
        clientCookieStore.addCookie( sessionCookie );

        HttpContext clientContext = new BasicHttpContext();
        clientContext.setAttribute( ClientContext.COOKIE_STORE, clientCookieStore );

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "show" ), clientContext );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( "{}" ) );
    }

    @Test
    public void testClearSession()
        throws Exception
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        DefaultHttpClient client = newHttpClientInstance();
        CookieStore clientCookieStore = new BasicCookieStore();
        BasicClientCookie sessionCookie = new BasicClientCookie( sessionCookieName, signedSession );
        sessionCookie.setVersion( 0 );
        sessionCookie.setDomain( "127.0.0.1" );
        sessionCookie.setPath( "/" );
        clientCookieStore.addCookie( sessionCookie );

        HttpContext clientContext = new BasicHttpContext();
        clientContext.setAttribute( ClientContext.COOKIE_STORE, clientCookieStore );

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "clear" ), clientContext );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( "{}" ) );
    }
}
