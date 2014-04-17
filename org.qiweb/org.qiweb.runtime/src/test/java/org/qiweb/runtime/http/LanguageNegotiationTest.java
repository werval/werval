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
package org.qiweb.runtime.http;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.http.Headers.Names.ACCEPT_LANGUAGE;
import static org.qiweb.api.http.Headers.Names.VARY;

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
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /accepted org.qiweb.runtime.http.LanguageNegotiationTest$Controller.accepted\n"
        + "GET /preferred org.qiweb.runtime.http.LanguageNegotiationTest$Controller.preferred"
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
