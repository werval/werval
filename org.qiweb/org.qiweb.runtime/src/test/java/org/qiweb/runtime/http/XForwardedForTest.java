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
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.http.Headers.Names.X_FORWARDED_FOR;

/**
 * Assert default {@link RequestHeader#remoteAddress()} behaviour.
 */
@SuppressWarnings( "PublicInnerClass" )
public class XForwardedForTest
{

    public static class Controller
    {

        public Outcome remoteAddress()
        {
            return outcomes().ok( request().remoteAddress() ).build();
        }

    }

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET / org.qiweb.runtime.http.XForwardedForTest$Controller.remoteAddress" ) );

    @Test
    public void withoutXForwardedFor()
    {
        expect().body( equalTo( "127.0.0.1" ) ).
            when().get( "/" );
    }

    @Test
    public void withXForwardedForAndTrustedProxy()
    {
        given().header( X_FORWARDED_FOR, "8.8.8.8, 127.0.0.1" ).
            expect().body( equalTo( "8.8.8.8" ) ).
            when().get( "/" );
    }

    @Test
    public void withTwoXForwardedFor()
    {
        given().header( X_FORWARDED_FOR, "Whatever", "Anything" ).
            expect().statusCode( 400 ).
            when().get( "/" );
    }

    @Test
    public void withXForwardedForAndNoProxy()
    {
        given().header( X_FORWARDED_FOR, "8.8.8.8" ).
            expect().statusCode( 400 ).
            when().get( "/" );
    }

    @Test
    public void withXForwardedForAndUntrustedProxy()
    {
        given().header( X_FORWARDED_FOR, "8.8.8.8, 8.8.8.8" ).
            expect().statusCode( 400 ).
            when().get( "/" );
    }

    @Test
    public void withXForwardedForAndMixedProxies()
    {
        given().header( X_FORWARDED_FOR, "8.8.8.8, 127.0.0.1, 23.23.23.23" ).
            expect().statusCode( 400 ).
            when().get( "/" );
    }

}
