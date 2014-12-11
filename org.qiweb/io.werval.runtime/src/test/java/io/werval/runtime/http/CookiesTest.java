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
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.builders;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.context.CurrentContext.response;

public class CookiesTest
{
    public static class Controller
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

        public Outcome builderCookie()
        {
            response().cookies().set(
                builders().newCookieBuilder()
                .name( "empty" )
                .build()
            );
            response().cookies().set(
                builders().newCookieBuilder()
                .name( "valued" )
                .value( "value" )
                .build()
            );
            return outcomes().ok().build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /set/:name/:value io.werval.runtime.http.CookiesTest$Controller.setCookie( String name, String value )\n"
        + "GET /remove/:name io.werval.runtime.http.CookiesTest$Controller.removeCookie( String name )\n"
        + "GET /mirror io.werval.runtime.http.CookiesTest$Controller.mirrorCookies\n"
        + "GET /builder io.werval.runtime.http.CookiesTest$Controller.builderCookie"
    ) );

    @Test
    public void setCookie()
    {
        expect()
            .statusCode( 200 )
            .cookie( "foo", "bar" )
            .when()
            .get( "/set/foo/bar" );
    }

    @Test
    public void removeCookie()
    {
        expect()
            .statusCode( 200 )
            .cookie( "foo", "" )
            .when()
            .get( "/remove/foo" );
    }

    @Test
    public void mirrorCookies()
    {
        given()
            .cookie( "bazar", "cathedral" )
            .cookie( "binz", "bridge" )
            .expect()
            .statusCode( 200 )
            .cookie( "bazar", "cathedral" )
            .cookie( "binz", "bridge" )
            .when()
            .get( "/mirror" );
    }

    @Test
    public void builderCookie()
    {
        expect()
            .statusCode( 200 )
            .cookie( "empty", "" )
            .cookie( "valued", "value" )
            .when()
            .get( "/builder" );
    }
}
