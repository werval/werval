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

import io.werval.api.outcomes.Outcome;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;
import static java.util.Locale.US;
import static org.apache.http.params.CoreProtocolPNames.PROTOCOL_VERSION;
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

        public Outcome unknown()
        {
            return outcomes().status( 666 ).build();
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "\n"
        + "GET /ok org.qiweb.runtime.http.KeepAliveTest$Controller.ok\n"
        + "GET /clientError org.qiweb.runtime.http.KeepAliveTest$Controller.clientError\n"
        + "GET /serverError org.qiweb.runtime.http.KeepAliveTest$Controller.serverError\n"
        + "GET /unknown org.qiweb.runtime.http.KeepAliveTest$Controller.unknown\n"
    ) );

    @Test
    public void http11_keepalive()
    {
        expect().
            statusCode( 200 ).
            header( CONNECTION, KEEP_ALIVE ).
            when().get( "/ok" );
        expect().
            statusCode( 400 ).
            header( CONNECTION, CLOSE ).
            when().get( "/clientError" );
        expect().
            statusCode( 500 ).
            header( CONNECTION, CLOSE ).
            when().get( "/serverError" );
        expect().
            statusCode( 666 ).
            header( CONNECTION, CLOSE ).
            when().get( "/unknown" );
    }

    @Test
    public void http11_close()
    {
        given().
            header( CONNECTION, CLOSE ).
            expect().
            statusCode( 200 ).
            header( CONNECTION, CLOSE ).
            when().get( "/ok" );
        given().
            header( CONNECTION, KEEP_ALIVE ).
            expect().
            statusCode( 400 ).
            header( CONNECTION, CLOSE ).
            when().get( "/clientError" );
        given().
            header( CONNECTION, KEEP_ALIVE ).
            expect().
            statusCode( 500 ).
            header( CONNECTION, CLOSE ).
            when().get( "/serverError" );
        given().
            header( CONNECTION, KEEP_ALIVE ).
            expect().
            statusCode( 666 ).
            header( CONNECTION, CLOSE ).
            when().get( "/unknown" );
    }

    @Test
    public void http10_default()
        throws IOException
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet( QIWEB.baseHttpUrl() + "/ok" );
        get.getParams().setParameter( PROTOCOL_VERSION, new ProtocolVersion( "HTTP", 1, 0 ) );
        // Apache HttpClient is a mess, without this it force Keep-Alive on HTTP/1.0 requests ...
        get.setHeader( CONNECTION, "NOT PRESENT" );
        HttpResponse response = client.execute( get );
        assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
        assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( CLOSE ) );
    }

    @Test
    public void http10_close()
        throws IOException
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet( QIWEB.baseHttpUrl() + "/ok" );
        get.getParams().setParameter( PROTOCOL_VERSION, new ProtocolVersion( "HTTP", 1, 0 ) );
        get.setHeader( CONNECTION, CLOSE );
        HttpResponse response = client.execute( get );
        assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
        assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( CLOSE ) );
    }

    @Test
    public void http10_keepalive()
        throws IOException
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet( QIWEB.baseHttpUrl() + "/ok" );
        get.getParams().setParameter( PROTOCOL_VERSION, new ProtocolVersion( "HTTP", 1, 0 ) );
        get.setHeader( CONNECTION, KEEP_ALIVE );
        HttpResponse response = client.execute( get );
        assertThat( response.getProtocolVersion().getMinor(), is( 0 ) );
        assertThat( response.getFirstHeader( CONNECTION ).getValue().toLowerCase( US ), equalTo( KEEP_ALIVE ) );
    }
}
