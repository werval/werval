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
package org.qiweb.test;

import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.qiweb.api.Application;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServerInstance;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * Base QiWeb Test.
 */
public abstract class AbstractQiWebTest
{

    protected static final String BASE_URL = "http://127.0.0.1:23023/";
    private HttpServerInstance httpServer;
    private ApplicationInstance app;

    /**
     * Activate HttpServer.
     */
    @Before
    public final void beforeEachTest()
    {
        RoutesProvider routesProvider = new RoutesParserProvider( routesString() );
        app = new ApplicationInstance( routesProvider );
        httpServer = new HttpServerInstance( "qiweb-test", app );
        httpServer.activate();
    }

    /**
     * Passivate HttpServer.
     */
    @After
    public final void afterEachTest()
    {
        httpServer.passivate();
        httpServer = null;
        app = null;
    }

    protected abstract String routesString();

    /**
     * @return Application
     */
    protected final Application application()
    {
        return app;
    }

    /**
     * @return New HttpClient instance
     */
    protected final DefaultHttpClient newHttpClientInstance()
    {
        DefaultHttpClient client = new DefaultHttpClient( new BasicClientConnectionManager() );
        client.setHttpRequestRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) );
        return client;
    }

    protected final void soutResponseHead( HttpResponse response )
    {
        System.out.println( "RESPONSE STATUS:  " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() );
        System.out.println( "RESPONSE HEADERS: " + Arrays.toString( response.getAllHeaders() ) );
    }

    protected final String responseBodyAsString( HttpResponse response )
        throws IOException
    {
        return EntityUtils.toString( response.getEntity(), UTF_8 );
    }
}
