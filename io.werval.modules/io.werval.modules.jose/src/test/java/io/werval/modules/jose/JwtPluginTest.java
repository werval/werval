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
package io.werval.modules.jose;

import com.fasterxml.jackson.databind.JsonNode;
import io.werval.api.outcomes.Outcome;
import io.werval.modules.jose.filters.RequireSubject;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.metaData;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.http.Status.OK_CODE;
import static io.werval.api.http.Status.UNAUTHORIZED_CODE;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_JSON;
import static io.werval.modules.json.JSON.json;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * JwtPlugin Test.
 */
public class JwtPluginTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "POST /login io.werval.modules.jose.JwtPluginTest$Controller.login\n"
        + "POST /renew JWT.renew\n"
        + "GET /authenticated io.werval.modules.jose.JwtPluginTest$Controller.authenticated\n"
    ) );

    public static class Controller
    {
        public Outcome login()
        {
            JsonNode body = json().fromJSON( request().body().asBytes() );
            String email = body.get( "email" ).asText();
            String password = body.get( "password" ).asText();
            if( "admin@example.com".equals( email ) && "admin-password".equals( password ) )
            {
                String token = plugin( JWT.class ).tokenForClaims( singletonMap( JWT.CLAIM_SUBJECT, email ) );
                return outcomes().ok()
                    .withHeader( application().config().string( JWT.HTTP_HEADER_CONFIG_KEY ), token )
                    .build();
            }
            return outcomes().unauthorized().build();
        }

        @RequireSubject
        public Outcome authenticated()
        {
            Map<String, Object> claims = metaData().get( Map.class, JWT.CLAIMS_METADATA_KEY );
            if( !"admin@example.com".equals( claims.get( JWT.CLAIM_SUBJECT ) ) )
            {
                return outcomes().unauthorized().build();
            }
            return outcomes().ok().build();
        }
    }

    @Test
    public void api()
    {
        JWT jwt = WERVAL.application().plugin( JWT.class );
        String token = jwt.tokenForClaims( singletonMap( JWT.CLAIM_SUBJECT, "someone@example.com" ) );
        Map<String, Object> parsed = jwt.claimsOfToken( token );
        assertThat( parsed.get( JWT.CLAIM_SUBJECT ), equalTo( "someone@example.com" ) );
    }

    @Test
    public void http()
        throws InterruptedException
    {
        String tokenHeaderName = WERVAL.application().config().string( JWT.HTTP_HEADER_CONFIG_KEY );
        JWT jwt = WERVAL.application().plugin( JWT.class );

        // Unauthorized access to authenticated resource
        when().get( "/authenticated" )
            .then().statusCode( UNAUTHORIZED_CODE );

        // Login
        String token = given().body( "{\"email\":\"admin@example.com\",\"password\":\"admin-password\"}" )
            .contentType( APPLICATION_JSON )
            .when().post( "/login" )
            .then().statusCode( OK_CODE )
            .header( tokenHeaderName, notNullValue() )
            .log().all()
            .extract().header( tokenHeaderName );

        // Authorized access to authenticated resource
        given().header( tokenHeaderName, token )
            .when().get( "/authenticated" )
            .then().statusCode( OK_CODE );

        // Gather time related claims from token
        ZoneId utc = ZoneId.of( "UTC" );
        Map<String, Object> claims = jwt.claimsOfToken( token );
        ZonedDateTime iat = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_ISSUED_AT ) ), utc
        );
        ZonedDateTime nbf = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_NOT_BEFORE ) ), utc
        );
        ZonedDateTime exp = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_EXPIRATION ) ), utc
        );

        // Wait at least one second before renewal so new dates will be different
        Thread.sleep( 1200 );

        // Renew token
        String renewed = given().header( tokenHeaderName, token )
            .when().post( "/renew" )
            .then().statusCode( OK_CODE )
            .header( tokenHeaderName, notNullValue() )
            .log().all()
            .extract().header( tokenHeaderName );

        // Gather time related claims from renewed token
        claims = jwt.claimsOfToken( renewed );
        ZonedDateTime renewedIat = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_ISSUED_AT ) ), utc
        );
        ZonedDateTime renewedNbf = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_NOT_BEFORE ) ), utc
        );
        ZonedDateTime renewedExp = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond( (Long) claims.get( JWT.CLAIM_EXPIRATION ) ), utc
        );

        // Assert renewed token time related claims are greater than the ones in the original token
        assertTrue( renewedIat.isAfter( iat ) );
        assertTrue( renewedNbf.isAfter( nbf ) );
        assertTrue( renewedExp.isAfter( exp ) );
    }
}
