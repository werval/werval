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
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.http.Headers.Names.ACCEPT;
import static org.qiweb.api.http.Headers.Names.VARY;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;
import static org.qiweb.api.mime.MimeTypes.WILDCARD_MIMETYPE;

/**
 * Content Negotiation Test.
 */
public class ContentNegotiationTest
{
    public static class Controller
    {
        public Outcome accepted()
        {
            return outcomes()
                .ok( request().acceptedMimeTypes().toString() )
                .withHeader( VARY, ACCEPT )
                .build();
        }

        public Outcome preferredEmpty()
        {
            switch( request().preferredMimeType() )
            {
                case APPLICATION_JSON:
                    return outcomes().ok( "{}" ).as( APPLICATION_JSON ).withHeader( VARY, ACCEPT ).build();
                case WILDCARD_MIMETYPE:
                default:
                    return outcomes().ok( "<p></p>" ).as( TEXT_HTML ).withHeader( VARY, ACCEPT ).build();
            }
        }

        public Outcome preferredAmong()
        {
            switch( request().preferredMimeType( TEXT_HTML, APPLICATION_JSON ) )
            {
                case WILDCARD_MIMETYPE:
                case TEXT_HTML:
                    return outcomes().ok( "<p></p>" ).as( TEXT_HTML ).withHeader( VARY, ACCEPT ).build();
                case APPLICATION_JSON:
                    return outcomes().ok( "{}" ).as( APPLICATION_JSON ).withHeader( VARY, ACCEPT ).build();
                default:
                    return outcomes().notAcceptable().build();
            }
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /accepted org.qiweb.runtime.http.ContentNegotiationTest$Controller.accepted\n"
        + "GET /preferredEmpty org.qiweb.runtime.http.ContentNegotiationTest$Controller.preferredEmpty\n"
        + "GET /preferredAmong org.qiweb.runtime.http.ContentNegotiationTest$Controller.preferredAmong\n"
    ) );

    @Test
    public void acceptedEmpty()
    {
        expect()
            .header( VARY, equalTo( ACCEPT ) )
            .body( equalTo( "[*/*]" ) )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void acceptedSingle()
    {
        given()
            .header( ACCEPT, APPLICATION_JSON )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .body( equalTo( "[application/json]" ) )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void acceptedMultipleAcceptHeaders()
    {
        given()
            .header( ACCEPT, APPLICATION_JSON )
            .header( ACCEPT, TEXT_HTML )
            .expect()
            .statusCode( 400 )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void acceptedListBasic()
    {
        given()
            .header( ACCEPT, APPLICATION_JSON + "," + TEXT_HTML )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .body( equalTo( "[application/json, text/html]" ) )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void acceptedListComplex()
    {
        given()
            .header( ACCEPT, "text/*;q=0.3, text/html;q=0.7, text/html;level=1,text/html;level=2;q=0.4, */*;q=0.5" )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .body( equalTo( "[text/html;level=1, text/html;q=0.7, */*;q=0.5, text/html;q=0.4;level=2, text/*;q=0.3]" ) )
            .when()
            .get( "/accepted" );
    }

    @Test
    public void preferredEmptyNoAccept()
    {
        expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( TEXT_HTML )
            .body( equalTo( "<p></p>" ) )
            .when()
            .get( "/preferredEmpty" );
    }

    @Test
    public void preferredEmptyAcceptJSON()
    {
        given()
            .header( ACCEPT, APPLICATION_JSON )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( APPLICATION_JSON )
            .body( equalTo( "{}" ) )
            .when()
            .get( "/preferredEmpty" );
    }

    @Test
    public void preferredEmptyComplexAccept()
    {
        given()
            .header( ACCEPT, "text/html;q=0.8,application/json;q=0.9" )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( APPLICATION_JSON )
            .body( equalTo( "{}" ) )
            .when()
            .get( "/preferredEmpty" );
    }

    @Test
    public void preferredAmongNoAccept()
    {
        expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( TEXT_HTML )
            .body( equalTo( "<p></p>" ) )
            .when()
            .get( "/preferredAmong" );
    }

    @Test
    public void preferredAmongUnacceptable()
    {
        given()
            .header( ACCEPT, "text/plain" )
            .expect()
            .statusCode( 406 )
            .when()
            .get( "/preferredAmong" );
    }

    @Test
    public void preferredAmongAcceptJSON()
    {
        given()
            .header( ACCEPT, APPLICATION_JSON )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( APPLICATION_JSON )
            .body( equalTo( "{}" ) )
            .when()
            .get( "/preferredAmong" );
    }

    @Test
    public void preferredAmongComplexAccept()
    {
        given()
            .header( ACCEPT, "text/html;q=0.8,application/json;q=0.9" )
            .expect()
            .statusCode( 200 )
            .header( VARY, equalTo( ACCEPT ) )
            .contentType( APPLICATION_JSON )
            .body( equalTo( "{}" ) )
            .when()
            .get( "/preferredAmong" );
    }
}
