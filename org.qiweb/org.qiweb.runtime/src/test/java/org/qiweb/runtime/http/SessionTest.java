/*
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
package org.qiweb.runtime.http;

import java.util.Collections;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.Session;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.http.CookiesInstance.CookieInstance;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.session;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;

public class SessionTest
{
    public static class Controller
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

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /set/:name/:value org.qiweb.runtime.http.SessionTest$Controller.set( String name, String value )\n"
        + "GET /clear org.qiweb.runtime.http.SessionTest$Controller.clear\n"
        + "GET /show org.qiweb.runtime.http.SessionTest$Controller.show" ) );

    private String sessionCookieName;

    @Before
    public void beforeSessionTest()
    {
        sessionCookieName = QIWEB.application().config().string( APP_SESSION_COOKIE_NAME );
    }

    @Test
    public void testSetSession()
    {
        String cookieValue = expect().when().get( "/set/foo/bar" ).cookie( sessionCookieName );
        Cookie sessionCookie = new CookieInstance( sessionCookieName, "/", null, false, cookieValue, true );
        Session session = new SessionInstance( QIWEB.application().config(), QIWEB.application().crypto(), sessionCookie );
        assertThat( session.get( "foo" ), equalTo( "bar" ) );
    }

    @Test
    public void testValidSessionAssured()
    {
        String signedSession = new SessionInstance( QIWEB.application().config(),
                                                    QIWEB.application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{foo=bar}" ) ).
            when().get( "/show" );
    }

    @Test
    public void testInvalidSessionAssured()
    {
        String signedSession = new SessionInstance( QIWEB.application().config(),
                                                    QIWEB.application().crypto(),
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
        String signedSession = new SessionInstance( QIWEB.application().config(),
                                                    QIWEB.application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{}" ) ).
            when().get( "/clear" );
    }
}
