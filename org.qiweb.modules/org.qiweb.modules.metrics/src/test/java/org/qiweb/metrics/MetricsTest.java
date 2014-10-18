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
package org.qiweb.metrics;

import com.codahale.metrics.MetricRegistry;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.plugin;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;
import static org.qiweb.api.mime.MimeTypes.TEXT_PLAIN;

/**
 * Metrics Test.
 */
public class MetricsTest
{
    @ClassRule
    public static QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET / org.qiweb.controllers.Default.ok\n"
        + "GET /redir org.qiweb.controllers.Default.seeOther( String url = / )\n"
        + "GET /error org.qiweb.controllers.Default.internalServerError\n"
        + "GET /unknown org.qiweb.metrics.MetricsTest$Controller.unknown\n"
        + "GET /@metrics org.qiweb.metrics.Tools.devShellIndex\n"
        + "GET /@metrics/metrics org.qiweb.metrics.Tools.metrics\n"
        + "GET /@metrics/health-checks org.qiweb.metrics.Tools.healthchecks\n"
        + "GET /@metrics/thread-dump org.qiweb.metrics.Tools.threadDump\n"
    ) );

    public static class Controller
    {
        public Outcome unknown()
        {
            plugin( Metrics.class ).metrics().counter( "MetricsTest" ).inc();
            return outcomes().status( 666 ).build();
        }
    }

    @Test
    public void devShellRoutesAndMetrics()
        throws Exception
    {
        MetricRegistry metrics = QIWEB.application().plugin( Metrics.class ).metrics();

        expect()
            .statusCode( 200 )
            .when()
            .get( "/" );

        expect()
            .statusCode( 200 )
            .contentType( TEXT_HTML )
            .body( containsString( "href=\"/@metrics/metrics" ) )
            .body( containsString( "href=\"/@metrics/health-checks" ) )
            .body( containsString( "href=\"/@metrics/thread-dump" ) )
            .when()
            .get( "/@metrics" );

        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "timers.'qiweb.http.requests'.count", is( 2 ) )
            .body( "meters.'qiweb.http.success'.count", is( 2 ) )
            .when()
            .get( "/@metrics/metrics" );

        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "'jvm.deadlocks'.healthy", is( true ) )
            .when()
            .get( "/@metrics/health-checks" );

        expect()
            .statusCode( 200 )
            .contentType( TEXT_PLAIN )
            .body( containsString( "org.qiweb.metrics.Tools.threadDump" ) )
            .when()
            .get( "/@metrics/thread-dump" );

        assertThat( metrics.timer( "qiweb.http.requests" ).getCount(), is( 5L ) );
        assertThat( metrics.meter( "qiweb.http.success" ).getCount(), is( 5L ) );
        assertThat( metrics.meter( "qiweb.http.redirections" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "qiweb.http.client-errors" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "qiweb.http.server-errors" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "qiweb.http.unknown" ).getCount(), is( 0L ) );

        expect().statusCode( 200 ).when().get( "/redir" ); // Follows the redirection to / that returns 200
        expect().statusCode( 404 ).when().get( "/not-found" );
        expect().statusCode( 500 ).when().get( "/error" );
        expect().statusCode( 666 ).when().get( "/unknown" );

        assertThat( metrics.timer( "qiweb.http.requests" ).getCount(), is( 10L ) );
        assertThat( metrics.meter( "qiweb.http.success" ).getCount(), is( 6L ) );
        assertThat( metrics.meter( "qiweb.http.redirections" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "qiweb.http.client-errors" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "qiweb.http.server-errors" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "qiweb.http.unknown" ).getCount(), is( 1L ) );

        assertThat( metrics.counter( "MetricsTest" ).getCount(), is( 1L ) );
    }
}
