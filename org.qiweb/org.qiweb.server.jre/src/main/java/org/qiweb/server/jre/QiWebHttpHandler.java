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
package org.qiweb.server.jre;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.util.InputStreamByteSource;
import org.qiweb.api.util.InputStreams;
import org.qiweb.runtime.http.CookiesInstance;
import org.qiweb.runtime.outcomes.ChunkedInputOutcome;
import org.qiweb.runtime.outcomes.InputStreamOutcome;
import org.qiweb.runtime.outcomes.SimpleOutcome;
import org.qiweb.spi.server.HttpServerHelper;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;

public class QiWebHttpHandler
    implements HttpHandler
{
    private static final int HTTP_BUF_SIZE = 16_384;
    private final ApplicationSPI app;
    private final DevShellSPI devSpi;
    private final HttpServerHelper helper = new HttpServerHelper();

    public QiWebHttpHandler( ApplicationSPI app, DevShellSPI devSpi )
    {
        this.app = app;
        this.devSpi = devSpi;
    }

    @Override
    public void handle( HttpExchange exchange )
        throws IOException
    {
        // New request
        String requestIdentity = helper.generateNewRequestIdentity();

        // In development mode, rebuild application source if needed
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            devSpi.rebuild();
        }

        // Parse request
        Request request = request( requestIdentity, exchange );

        // Handle Request
        Outcome outcome = app.handleRequest( request );

        // Write Outcome
        writeOutcome( outcome, exchange );

        // Done!
        app.onHttpRequestComplete( request );
    }

    private Request request( String requestIdentity, HttpExchange exchange )
    {
        return app.httpBuilders().newRequestBuilder()
            .identifiedBy( requestIdentity )
            .remoteSocketAddress( exchange.getRemoteAddress().toString() )
            .version( ProtocolVersion.valueOf( exchange.getProtocol() ) )
            .method( exchange.getRequestMethod() )
            .uri( exchange.getRequestURI().toString() )
            .headers( exchange.getRequestHeaders() )
            .cookies( new CookiesInstance() ) // TODO
            .bodyBytes( new InputStreamByteSource( exchange.getRequestBody(), HTTP_BUF_SIZE ) )
            .build();
    }

    private void writeOutcome( Outcome outcome, HttpExchange exchange )
        throws IOException
    {
        // Headers
        Headers headers = exchange.getResponseHeaders();
        headers.putAll( outcome.responseHeader().headers().allValues() );

        // Cookies
        outcome.responseHeader().cookies(); // TODO

        // Body
        InputStream bodyStream = null;
        int chunkSize = HTTP_BUF_SIZE;
        final long responseLength;
        if( outcome instanceof ChunkedInputOutcome )
        {
            ChunkedInputOutcome chunked = (ChunkedInputOutcome) outcome;
            bodyStream = chunked.inputStream();
            chunkSize = chunked.chunkSize();
            responseLength = 0;
        }
        else if( outcome instanceof InputStreamOutcome )
        {
            InputStreamOutcome input = (InputStreamOutcome) outcome;
            bodyStream = input.bodyInputStream();
            responseLength = input.contentLength();
        }
        else if( outcome instanceof SimpleOutcome )
        {
            SimpleOutcome simple = (SimpleOutcome) outcome;
            bodyStream = simple.body().asStream();
            responseLength = 0;
        }
        else
        {
            responseLength = -1;
        }

        // Status and Content-Length
        exchange.sendResponseHeaders( outcome.responseHeader().status().code(), responseLength );

        // Body if any
        if( bodyStream != null )
        {
            InputStreams.transferTo( bodyStream, exchange.getResponseBody(), chunkSize );
        }
    }
}