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
package org.qiweb.runtime.server;

import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.qiweb.api.Error;
import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.runtime.TestGlobal;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qiweb.runtime.http.HttpConstants.QIWEB_HEADER_REQUEST_ID;

/**
 * Assert that Application errors triggers the right code paths.
 */
public class OnErrorTest
    extends AbstractQiWebTest
{

    public static class Ctrl
    {

        public Outcome success()
        {
            return Controller.outcomes().ok().build();
        }

        public Outcome internalServerError()
        {
            return Controller.outcomes().internalServerError().build();
        }

        public Outcome exception()
            throws IOException
        {
            throw new IOException( "Wow an exception!", new RuntimeException( "This is a crash" ) );
        }
    }

    @Override
    protected String routesString()
    {
        return ""
               + "GET /success org.qiweb.runtime.server.OnErrorTest$Ctrl.success\n"
               + "GET /internalServerError org.qiweb.runtime.server.OnErrorTest$Ctrl.internalServerError\n"
               + "GET /exception org.qiweb.runtime.server.OnErrorTest$Ctrl.exception\n";
    }

    @Test
    public void testSuccess()
        throws IOException
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "success" ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( application().errors().count(), is( 0 ) );
    }

    @Test
    public void testInternalServerError()
        throws IOException
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "internalServerError" ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 500 ) );
        assertThat( application().errors().count(), is( 0 ) );
    }

    @Test
    public void testException()
        throws IOException
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "exception" ) );

        assertThat( response.getStatusLine().getStatusCode(), is( 500 ) );

        String requestId = response.getLastHeader( QIWEB_HEADER_REQUEST_ID ).getValue();
        assertThat( requestId, notNullValue() );

        assertThat( application().errors().count(), is( 1 ) );

        List<Error> requestErrors = application().errors().ofRequest( requestId );
        assertThat( requestErrors.size(), is( 1 ) );

        Error requestError = requestErrors.get( 0 );
        assertThat( requestError.cause().getClass().getName(), equalTo( IOException.class.getName() ) );
        assertThat( requestError.cause().getMessage(), equalTo( "Wow an exception!" ) );
        assertThat( requestError.cause().getCause().getClass().getName(), equalTo( RuntimeException.class.getName() ) );
        assertThat( requestError.cause().getCause().getMessage(), equalTo( "This is a crash" ) );
    }

    @Test
    public void testGlobalOnHttpRequestError()
        throws IOException
    {
        TestGlobal testGlobal = TestGlobal.ofApplication( application() );
        assertThat( testGlobal.httpRequestErrorCount, is( 0 ) );

        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + "exception" ) );

        assertThat( response.getStatusLine().getStatusCode(), is( 500 ) );

        assertThat( testGlobal.httpRequestErrorCount, is( 1 ) );
        assertThat( testGlobal.lastError.cause().getClass().getName(), equalTo( IOException.class.getName() ) );
        assertThat( testGlobal.lastError.cause().getMessage(), equalTo( "Wow an exception!" ) );
        assertThat( testGlobal.lastError.cause().getCause().getClass().getName(), equalTo( RuntimeException.class.getName() ) );
        assertThat( testGlobal.lastError.cause().getCause().getMessage(), equalTo( "This is a crash" ) );
    }
}
