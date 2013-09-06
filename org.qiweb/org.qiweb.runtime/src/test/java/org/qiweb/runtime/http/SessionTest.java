package org.qiweb.runtime.http;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.Session;
import org.qiweb.runtime.http.CookiesInstance.CookieInstance;
import org.qiweb.test.AbstractQiWebTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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
    {
        String cookieValue = expect().when().get( "/set/foo/bar" ).cookie( sessionCookieName );
        Cookie sessionCookie = new CookieInstance( sessionCookieName, "/", null, false, cookieValue, true );
        Session session = new SessionInstance( application().config(), application().crypto(), sessionCookie );
        assertThat( session.get( "foo" ), equalTo( "bar" ) );
    }

    @Test
    public void testValidSessionAssured()
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{foo=bar}" ) ).
            when().get( "/show" );
    }

    @Test
    public void testInvalidSessionAssured()
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        // Invalidate Session Data
        signedSession = signedSession.substring( 1 );
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{}" ) ).
            when().get( "/show" );
    }

    @Test
    public void testClearSessionAssured()
    {
        String signedSession = new SessionInstance( application().config(),
                                                    application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{}" ) ).
            when().get( "/clear" );
    }
}
