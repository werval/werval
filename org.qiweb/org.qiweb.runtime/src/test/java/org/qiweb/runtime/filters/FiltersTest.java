package org.qiweb.runtime.filters;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;

public class FiltersTest
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
    {

        @FilterWith( Filter.class )
        public Outcome filtered()
        {
            return org.qiweb.api.context.CurrentContext.outcomes().ok().build();
        }

    }

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule(
        new RoutesParserProvider( "GET / org.qiweb.runtime.filters.FiltersTest$Controller.filtered" ) );

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
