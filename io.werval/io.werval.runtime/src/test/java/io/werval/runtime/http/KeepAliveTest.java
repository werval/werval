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
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;
import static java.util.Locale.US;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Assert correct Kepp-Alive behaviour.
 */
public class KeepAliveTest
{
    public static class Controller
    {
        public Outcome ok()
        {
            return outcomes().ok().build();
        }

        public Outcome clientError()
        {
            return outcomes().badRequest().build();
        }

        public Outcome serverError()
        {
            return outcomes().internalServerError().build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "\n"
        + "GET /ok io.werval.runtime.http.KeepAliveTest$Controller.ok\n"
        + "GET /clientError io.werval.runtime.http.KeepAliveTest$Controller.clientError\n"
        + "GET /serverError io.werval.runtime.http.KeepAliveTest$Controller.serverError\n"
    ) );

    @Test
    public void http11_keepalive()
    {
        when().get( "/ok" )
            .then().statusCode( 200 )
            .and().header( CONNECTION, KEEP_ALIVE );
        when().get( "/clientError" )
            .then().statusCode( 400 )
            .and().header( CONNECTION, CLOSE );
        when().get( "/serverError" )
            .then().statusCode( 500 )
            .and().header( CONNECTION, CLOSE );
    }

    @Test
    public void http11_close()
    {
        given().header( CONNECTION, CLOSE )
            .when().get( "/ok" )
            .then().statusCode( 200 )
            .and().header( CONNECTION, CLOSE );
        given().header( CONNECTION, KEEP_ALIVE )
            .when().get( "/clientError" )
            .then().statusCode( 400 )
            .and().header( CONNECTION, CLOSE );
        given().header( CONNECTION, KEEP_ALIVE )
            .when().get( "/serverError" )
            .then().statusCode( 500 )
            .and().header( CONNECTION, CLOSE );
    }

    @Test
    public void http10_default()
        throws IOException
    {
        try( CloseableHttpClient client = HttpClientBuilder.create().build() )
        {
            HttpGet get = new HttpGet( WERVAL.baseHttpUrl() + "/ok" );
            get.setProtocolVersion( new ProtocolVersion( "HTTP", 1, 0 ) );
            // Apache HttpClient is a mess, without this it force Keep-Alive on HTTP/1.0 requests ...
            get.setHeader( CONNECTION, "NOT PRESENT" );
            HttpResponse response = client.execute( get );
            assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
            assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( CLOSE ) );
        }
    }

    @Test
    public void http10_close()
        throws IOException
    {
        try( CloseableHttpClient client = HttpClientBuilder.create().build() )
        {
            HttpGet get = new HttpGet( WERVAL.baseHttpUrl() + "/ok" );
            get.setProtocolVersion( new ProtocolVersion( "HTTP", 1, 0 ) );
            get.setHeader( CONNECTION, CLOSE );
            HttpResponse response = client.execute( get );
            assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
            assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( CLOSE ) );
        }
    }

    @Test
    public void http10_keepalive()
        throws IOException
    {
        try( CloseableHttpClient client = HttpClientBuilder.create().build() )
        {
            HttpGet get = new HttpGet( WERVAL.baseHttpUrl() + "/ok" );
            get.setProtocolVersion( new ProtocolVersion( "HTTP", 1, 0 ) );
            get.setHeader( CONNECTION, KEEP_ALIVE );
            HttpResponse response = client.execute( get );
            assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
            assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( KEEP_ALIVE ) );
        }
    }
}
