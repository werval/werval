package org.qiweb.runtime.filters;

import org.junit.Test;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.test.QiWebTest;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;

public class FiltersTest
    extends QiWebTest
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
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider( "GET / org.qiweb.runtime.filters.FiltersTest$Controller.filtered" );
    }

    @Test
    public void testFilters()
        throws Exception
    {
        expect().
            statusCode( 200 ).
            header( "X-QiWeb-Filtered", equalTo( "true" ) ).
            when().
            get( "/" );
    }
}
