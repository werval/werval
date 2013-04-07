package org.qiweb.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DefaultTest
    extends AbstractQiWebTest
{

    @Override
    protected String routesString()
    {
        return "GET /notFound org.qiweb.controller.Default.notFound\n"
               + "GET /internalServerError org.qiweb.controller.Default.internalServerError\n"
               + "GET /notImplemented org.qiweb.controller.Default.notImplemented";
    }

    @Test
    public void givenNotFoundRouteWhenRequestingExpectNotFound()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "notFound" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );
    }

    @Test
    public void givenInternalServerErrorRouteWhenRequestingExpectInternalServerError()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "internalServerError" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 500 ) );
    }

    @Test
    public void givenNotImplementedRouteWhenRequestingExpectNotImplemented()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "notImplemented" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 501 ) );
    }
}
