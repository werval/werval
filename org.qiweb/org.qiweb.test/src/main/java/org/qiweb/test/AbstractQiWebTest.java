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

public abstract class AbstractQiWebTest
{

    protected static final String BASE_URL = "http://127.0.0.1:23023/";
    private HttpServerInstance httpServer;
    private ApplicationInstance app;

    @Before
    public final void beforeEachTest()
    {
        RoutesProvider routesProvider = new RoutesParserProvider( routesString() );
        app = new ApplicationInstance( routesProvider );
        httpServer = new HttpServerInstance( "meta-inf-resources-test", app );
        httpServer.activate();
    }

    @After
    public final void afterEachTest()
    {
        httpServer.passivate();
        httpServer = null;
        app = null;
    }

    protected abstract String routesString();

    protected final Application application()
    {
        return app;
    }

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
