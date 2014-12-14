/*
 * Copyright (c) 2014 the original author or authors
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

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.http.Headers.Names.ACCEPT_LANGUAGE;
import static io.werval.api.http.Headers.Names.VARY;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Language Negotiation Test.
 */
public class LanguageNegotiationTest
{
    public static class Controller
    {
        public Outcome accepted()
        {
            return outcomes().ok( request().acceptedLangs().toString() ).build();
        }

        public Outcome preferred()
        {
            return outcomes()
                .ok( request().preferredLang().toString() )
                .withHeader( VARY, ACCEPT_LANGUAGE )
                .build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /accepted io.werval.runtime.http.LanguageNegotiationTest$Controller.accepted\n"
        + "GET /preferred io.werval.runtime.http.LanguageNegotiationTest$Controller.preferred"
    ) );

    @Test
    public void acceptedLangs()
    {
        expect()
            .body( equalTo( "[]" ) )
            .when()
            .get( "/accepted" );

        given()
            .header( ACCEPT_LANGUAGE, "da ,fr ;q=0.9, en;q=0.7, en-gb; q=0.8" )
            .expect()
            .body( equalTo( "[da, fr, en-gb, en]" ) )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void preferred()
    {
        expect()
            .header( VARY, equalTo( ACCEPT_LANGUAGE ) )
            .body( equalTo( "fr" ) )
            .when()
            .get( "/preferred" );

        given()
            .header( ACCEPT_LANGUAGE, "da ,fr ;q=0.9, en;q=0.7, en-gb; q=0.8" )
            .expect()
            .header( VARY, equalTo( ACCEPT_LANGUAGE ) )
            .body( equalTo( "da" ) )
            .when()
            .get( "/preferred" );
    }
}
