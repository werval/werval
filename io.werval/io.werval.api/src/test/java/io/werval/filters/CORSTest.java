/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.filters;

import io.werval.api.Config;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_METHODS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_EXPOSE_HEADERS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_REQUEST_HEADERS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_REQUEST_METHOD;
import static io.werval.api.http.Headers.Names.ORIGIN;
import static io.werval.api.http.Status.BAD_REQUEST_CODE;
import static io.werval.api.http.Status.NO_CONTENT_CODE;
import static io.werval.api.http.Status.OK_CODE;
import static io.werval.api.http.Status.UNAUTHORIZED_CODE;
import static io.werval.util.Strings.join;
import static org.hamcrest.Matchers.nullValue;

/**
 * CORS Test.
 */
public class CORSTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "OPTIONS /custom CORS.preflight( "
        + " String origin = 'http://example.com',"
        + " String methods = 'GET',"
        + " String headers = 'User-Agent',"
        + " Boolean creds = true "
        + ")\n"
        + "OPTIONS /*path CORS.preflight( String path )\n"
        + "GET /global io.werval.filters.CORSTest$Controller.global\n"
        + "GET /custom io.werval.filters.CORSTest$Controller.custom\n"
    ) );

    public static class Controller
    {
        @CORS
        public Outcome global()
        {
            return outcomes().ok().build();
        }

        @CORS(
             allowOrigin = "http://example.com",
             allowCredentials = true,
             exposeHeaders = "X-Custom-Exposed-Header"
        )
        public Outcome custom()
        {
            return outcomes().ok().build();
        }
    }

    @Test
    public void global()
    {
        Config config = WERVAL.application().config();
        String allowOrigin = join( config.stringList( "werval.controllers.cors.allow_origin" ), ", " );
        String allowMethods = join( config.stringList( "werval.controllers.cors.allow_methods" ), ", " );
        String allowHeaders = join( config.stringList( "werval.controllers.cors.allow_headers" ), ", " );
        String exposeHeaders = join( config.stringList( "werval.filters.cors.expose_headers" ), ", " );
        boolean allowCredentials = config.bool( "werval.controllers.cors.allow_credentials" )
                                   && !"*".equals( allowOrigin );
        given()
            .header( ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_REQUEST_METHOD, "PUT" )
            .expect()
            .statusCode( NO_CONTENT_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin )
            .header( ACCESS_CONTROL_ALLOW_METHODS, allowMethods )
            .header( ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf( allowCredentials ) )
            .when()
            .options( "/global" );
        given()
            .header( ORIGIN, "http://example.com" )
            .expect()
            .statusCode( OK_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf( allowCredentials ) )
            .header( ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders )
            .when()
            .get( "/global" );
    }

    @Test
    public void custom()
    {
        given()
            .header( ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_REQUEST_METHOD, "GET" )
            .expect()
            .statusCode( NO_CONTENT_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_ALLOW_METHODS, "GET" )
            .header( ACCESS_CONTROL_ALLOW_HEADERS, "User-Agent" )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, "true" )
            .when()
            .options( "/custom" );
        given()
            .header( ORIGIN, "http://example.com" )
            .expect()
            .statusCode( OK_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, "true" )
            .header( ACCESS_CONTROL_EXPOSE_HEADERS, "X-Custom-Exposed-Header" )
            .when()
            .get( "/custom" );
    }

    @Test
    public void preflightMissingCorsHeaders()
    {
        expect()
            .statusCode( BAD_REQUEST_CODE )
            .when()
            .options( "/global" );
        given()
            .header( ORIGIN, "http://example.com" )
            .expect()
            .statusCode( BAD_REQUEST_CODE )
            .when()
            .options( "/global" );
        given()
            .header( ACCESS_CONTROL_REQUEST_METHOD, "GET" )
            .expect()
            .statusCode( BAD_REQUEST_CODE )
            .when()
            .options( "/global" );
    }

    @Test
    public void preflightUnauthorizedOrigin()
    {
        given()
            .header( ORIGIN, "http://example.org" )
            .header( ACCESS_CONTROL_REQUEST_METHOD, "GET" )
            .expect()
            .statusCode( UNAUTHORIZED_CODE )
            .when()
            .options( "/custom" );
    }

    @Test
    public void preflightUnauthorizedMethod()
    {
        given()
            .header( ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_REQUEST_METHOD, "PATCH" )
            .expect()
            .statusCode( UNAUTHORIZED_CODE )
            .when()
            .options( "/custom" );
    }

    @Test
    public void preflightUnauthorizedHeader()
    {
        given()
            .header( ORIGIN, "http://example.com" )
            .header( ACCESS_CONTROL_REQUEST_METHOD, "GET" )
            .header( ACCESS_CONTROL_REQUEST_HEADERS, "Any-Header" )
            .expect()
            .statusCode( UNAUTHORIZED_CODE )
            .when()
            .options( "/custom" );
    }

    @Test
    public void requestNonCors()
    {
        expect()
            .statusCode( OK_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, nullValue() )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, nullValue() )
            .header( ACCESS_CONTROL_EXPOSE_HEADERS, nullValue() )
            .when()
            .get( "/global" );
        expect()
            .statusCode( OK_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, nullValue() )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, nullValue() )
            .header( ACCESS_CONTROL_EXPOSE_HEADERS, nullValue() )
            .when()
            .get( "/custom" );
    }

    @Test
    public void requestUnauthorizedOrigin()
    {
        given()
            .header( ORIGIN, "http://example.org" )
            .expect()
            .statusCode( OK_CODE )
            .header( ACCESS_CONTROL_ALLOW_ORIGIN, nullValue() )
            .header( ACCESS_CONTROL_ALLOW_CREDENTIALS, nullValue() )
            .header( ACCESS_CONTROL_EXPOSE_HEADERS, nullValue() )
            .when()
            .get( "/custom" );
    }
}
