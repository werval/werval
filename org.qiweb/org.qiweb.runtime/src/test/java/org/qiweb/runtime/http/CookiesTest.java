/**
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

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.context.CurrentContext.response;

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

    }

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET /set/:name/:value org.qiweb.runtime.http.CookiesTest$Controller.setCookie( String name, String value )\n"
        + "GET /remove/:name org.qiweb.runtime.http.CookiesTest$Controller.removeCookie( String name )\n"
        + "GET /mirror org.qiweb.runtime.http.CookiesTest$Controller.mirrorCookies" ) );

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
