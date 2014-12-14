/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.runtime.http;

import io.werval.api.http.Cookies.Cookie;
import io.werval.api.http.Session;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.http.CookiesInstance.CookieInstance;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.util.Collections;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.session;
import static io.werval.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /set/:name/:value io.werval.runtime.http.SessionTest$Controller.set( String name, String value )\n"
        + "GET /clear io.werval.runtime.http.SessionTest$Controller.clear\n"
        + "GET /show io.werval.runtime.http.SessionTest$Controller.show"
    ) );

    private String sessionCookieName;

    @Before
    public void beforeSessionTest()
    {
        sessionCookieName = WERVAL.application().config().string( APP_SESSION_COOKIE_NAME );
    }

    @Test
    public void testSetSession()
    {
        String cookieValue = expect().when().get( "/set/foo/bar" ).cookie( sessionCookieName );
        Cookie sessionCookie = new CookieInstance(
            0,
            sessionCookieName, cookieValue,
            "/", null,
            Long.MIN_VALUE,
            false, true,
            null, null
        );
        Session session = new SessionInstance( WERVAL.application().config(), WERVAL.application().crypto(), sessionCookie );
        assertThat( session.get( "foo" ), equalTo( "bar" ) );
    }

    @Test
    public void testValidSessionAssured()
    {
        String signedSession = new SessionInstance( WERVAL.application().config(),
                                                    WERVAL.application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{foo=bar}" ) ).
            when().get( "/show" );
    }

    @Test
    public void testInvalidSessionAssured()
    {
        String signedSession = new SessionInstance( WERVAL.application().config(),
                                                    WERVAL.application().crypto(),
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
        String signedSession = new SessionInstance( WERVAL.application().config(),
                                                    WERVAL.application().crypto(),
                                                    Collections.singletonMap( "foo", "bar" ) ).signedCookie().value();
        given().cookie( sessionCookieName, signedSession ).
            expect().body( equalTo( "{}" ) ).
            when().get( "/clear" );
    }
}
