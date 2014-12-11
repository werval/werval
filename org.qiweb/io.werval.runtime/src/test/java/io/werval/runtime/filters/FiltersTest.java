/*
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
package io.werval.runtime.filters;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;

public class FiltersTest
{
    public static class Filter
        implements io.werval.api.filters.Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
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
            return io.werval.api.context.CurrentContext.outcomes().ok().build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule(
        new RoutesParserProvider( "GET / io.werval.runtime.filters.FiltersTest$Controller.filtered" )
    );

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
