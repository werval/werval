package org.qiweb.controller;

import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServerInstance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MetaInfResourcesTest
{

    private static final String BASE_URL = "http://127.0.0.1:23023/";
    private HttpServerInstance httpServer;

    @Before
    public void beforeRouterTests()
        throws Exception
    {
        RoutesProvider routesProvider = new RoutesParserProvider( "GET /*path org.qiweb.controller.MetaInfResources.resource( String path )" );
        Application app = new ApplicationInstance( routesProvider );
        httpServer = new HttpServerInstance( "meta-inf-resources-test", app );
        httpServer.activateService();
    }

    @After
    public void afterRouterTests()
        throws Exception
    {
        httpServer.passivateService();
    }

    @Test
    public void givenNonExistentResourceWhenRequestingExpectNotFound()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "qiweb/donotexists.yet" ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );
    }

    @Test
    public void givenResourceSmallerThanOneChunkWhenRequestingExpectCorrectResult()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "qiweb/666B" ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );
        assertThat( response.getLastHeader( "Transfer-Encoding" ).getValue(), equalTo( "chunked" ) );
        assertThat( response.getLastHeader( "Content-Type" ).getValue(), equalTo( "application/octet-stream" ) );
        assertThat( EntityUtils.toByteArray( response.getEntity() ).length, equalTo( 666 ) );
    }

    @Test
    public void givenResourceSpanningSeveralCompleteChunksWhenRequestingExpectCorrectResult()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "qiweb/32KB" ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );
        assertThat( response.getLastHeader( "Transfer-Encoding" ).getValue(), equalTo( "chunked" ) );
        assertThat( response.getLastHeader( "Content-Type" ).getValue(), equalTo( "application/octet-stream" ) );
        assertThat( EntityUtils.toByteArray( response.getEntity() ).length, equalTo( 32768 ) );
    }

    @Test
    public void givenResourceSpanningOneChunkAndABitMoreWhenRequestingExpectCorrectResult()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();

        HttpResponse response = client.execute( new HttpGet( BASE_URL + "qiweb/8858B" ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );
        assertThat( response.getLastHeader( "Transfer-Encoding" ).getValue(), equalTo( "chunked" ) );
        assertThat( response.getLastHeader( "Content-Type" ).getValue(), equalTo( "application/octet-stream" ) );
        assertThat( EntityUtils.toByteArray( response.getEntity() ).length, equalTo( 8858 ) );
    }

    @Test
    public void givenDirectoryTraversalAttemptsWhenProcessingExpectBadRequest()
        throws Exception
    {
        // Simple directory traversal

        assertDirectoryTraversalAttemptFailed( "qiweb/../../../shadow" );
        assertDirectoryTraversalAttemptFailed( "../shadow" );

        // URI encoded directory traversal

        assertDirectoryTraversalAttemptFailed( "%2e%2e%2fshadow" );
        assertDirectoryTraversalAttemptFailed( "%2e%2e%5cshadow" );
        assertDirectoryTraversalAttemptFailed( "%2e%2e/shadow" );
        assertDirectoryTraversalAttemptFailed( "%2e./shadow" );
        assertDirectoryTraversalAttemptFailed( ".%2e/shadow" );
        assertDirectoryTraversalAttemptFailed( "..%2fshadow" );
        assertDirectoryTraversalAttemptFailed( "..%5cshadow" );

        // Unicode / UTF-8 encoded directory traversal

        assertDirectoryTraversalAttemptFailed( "\u002e\u002e\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "\u002e.\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( ".\u002e\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "..\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "\u002e\u002e/shadow" );
    }

    private void assertDirectoryTraversalAttemptFailed( String path )
        throws IOException
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL + path ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 400 ) );
    }

    private HttpClient newHttpClientInstance()
    {
        DefaultHttpClient client = new DefaultHttpClient( new BasicClientConnectionManager() );
        client.setHttpRequestRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) );
        return client;
    }

    private void soutResponseHead( HttpResponse response )
        throws IOException
    {
        System.out.println( "RESPONSE STATUS:  " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() );
        System.out.println( "RESPONSE HEADERS: " + Arrays.toString( response.getAllHeaders() ) );
    }
}
