package org.qiweb.runtime.filters;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class FiltersTest
    extends AbstractQiWebTest
{

    public static class Filter
        implements org.qiweb.api.filters.Filter
    {

        @Override
        public Outcome filter( FilterChain chain, Context context )
        {
            context.response().headers().with( "X-QiWeb-Filtered", "true" );
            return chain.next( context );
        }
    }

    public static class Controller
        extends org.qiweb.api.controllers.Controller
    {

        @FilterWith( Filter.class )
        public Outcome filtered()
        {
            return outcomes().ok().build();
        }
    }

    @Override
    protected String routesString()
    {
        return "GET / org.qiweb.runtime.filters.FiltersTest$Controller.filtered";
    }

    @Test
    public void testFilters()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        HttpResponse response = client.execute( new HttpGet( BASE_URL ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( response.getHeaders( "X-QiWeb-Filtered" )[0].getValue(), equalTo( "true" ) );
    }
}
