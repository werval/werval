/*
 * Copyright (c) 2014 the original author or authors
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
package io.werval.filters;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;
import org.qiweb.test.util.Slf4jRule;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LogIfSlowTest.
 */
public class LogIfSlowTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /quick io.werval.filters.LogIfSlowTest$Controller.quick\n"
        + "GET /slow io.werval.filters.LogIfSlowTest$Controller.slow\n"
    ) );

    @LogIfSlow
    public static class Controller
    {
        public Outcome quick()
        {
            return outcomes().ok().build();
        }

        public Outcome slow()
            throws Exception
        {
            Thread.sleep( 1_000 );
            return outcomes().ok().build();
        }
    }

    @Rule
    public Slf4jRule slf4j = new Slf4jRule()
    {
        {
            record( Level.WARN );
            recordForType( LogIfSlow.class );
        }
    };

    @Test
    public void quick()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/quick" );
        assertFalse( slf4j.contains( "Slow" ) );
    }

    @Test
    public void slow()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/slow" );
        assertTrue( slf4j.contains( "Slow" ) );
        assertTrue( slf4j.contains( "GET /slow" ) );
    }
}
