package org.qiweb.controller;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MetaInfResourcesTest
    extends AbstractQiWebTest
{

    @Override
    protected String routesString()
    {
        return "GET /*path org.qiweb.controller.MetaInfResources.resource( String path )";
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
}
