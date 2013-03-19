package org.qiweb.runtime.http.server;

import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qiweb.runtime.http.HttpApplicationInstance;
import org.qiweb.runtime.http.routes.RoutesParserProvider;
import org.qiweb.runtime.http.routes.RoutesProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RouterTest
{

    private static final String LISTEN_ADDRESS = "127.0.0.1";
    private static final int LISTEN_PORT = 23023;
    private static final String BASE_URL = "http://127.0.0.1:23023/";
    private HttpServerInstance httpServer;

    public static void main( String[] args )
        throws Exception
    {
        RouterTest test = new RouterTest();
        test.beforeRouterTests();
        try
        {
            Thread.sleep( Long.MAX_VALUE );
        }
        finally
        {
            test.afterRouterTests();
        }
    }

    @Before
    public void beforeRouterTests()
        throws Exception
    {
        RoutesProvider routesProvider = new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.index()\n"
            + "GET /foo com.acme.app.FakeControllerInstance.foo()\n"
            + "GET /bar com.acme.app.FakeControllerInstance.bar()\n"
            + "GET /:id/:slug com.acme.app.FakeControllerInstance.another( String id, Integer slug )" );
        httpServer = new HttpServerInstance(
            "router-test",
            LISTEN_ADDRESS, LISTEN_PORT,
            new HttpApplicationInstance( getClass().getClassLoader(), routesProvider ) );
        httpServer.activateService();
    }

    @After
    public void afterRouterTests()
        throws Exception
    {
        httpServer.passivateService();
    }

    @Test
    public void testRoutes()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();

        HttpResponse response = client.execute( new HttpGet( BASE_URL ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpPost( BASE_URL ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );

        response = client.execute( new HttpGet( BASE_URL + "/foo" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpGet( BASE_URL + "/bar" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpGet( BASE_URL + "/bazar" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );

        response = client.execute( new HttpGet( BASE_URL + "/azertyuiop/1234" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        // Thread.sleep( Long.MAX_VALUE );
    }

    private HttpClient newHttpClientInstance()
    {
        DefaultHttpClient client = new DefaultHttpClient();
        client.setHttpRequestRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) );
        return client;
    }

    private void soutResponse( HttpResponse response )
        throws IOException
    {
        System.out.println( "RESPONSE STATUS:  " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() );
        System.out.println( "RESPONSE HEADERS: " + Arrays.toString( response.getAllHeaders() ) );
        System.out.println( "RESPONSE BODY:    " + EntityUtils.toString( response.getEntity() ) );
    }
}
