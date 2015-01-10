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
package io.werval.modules.metrics;

import com.codahale.metrics.MetricRegistry;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static io.werval.api.mime.MimeTypes.TEXT_HTML;
import static io.werval.api.mime.MimeTypes.TEXT_PLAIN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Metrics Test.
 */
public class MetricsTest
{
    @ClassRule
    public static WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET / io.werval.controllers.Default.ok\n"
        + "GET /redir io.werval.controllers.Default.seeOther( String url = / )\n"
        + "GET /error io.werval.controllers.Default.internalServerError\n"
        + "GET /unknown io.werval.modules.metrics.MetricsTest$Controller.unknown\n"
        + "GET /@metrics io.werval.modules.metrics.Tools.devShellIndex\n"
        + "GET /@metrics/metrics io.werval.modules.metrics.Tools.metrics\n"
        + "GET /@metrics/health-checks io.werval.modules.metrics.Tools.healthchecks\n"
        + "GET /@metrics/thread-dump io.werval.modules.metrics.Tools.threadDump\n"
    ) );

    @Counter( name = "unknown-counter" )
    @Meter( name = "unknown-meter" )
    public static class Controller
    {
        @Timer( name = "unknown-timer" )
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
        MetricRegistry metrics = WERVAL.application().plugin( Metrics.class ).metrics();

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
            .body( "timers.'io.werval.http.requests'.count", is( 2 ) )
            .body( "meters.'io.werval.http.success'.count", is( 2 ) )
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
            .body( containsString( "io.werval.modules.metrics.Tools.threadDump" ) )
            .when()
            .get( "/@metrics/thread-dump" );

        await( "io.werval.http.requests == 5" ).until(
            () -> metrics.timer( "io.werval.http.requests" ).getCount() == 5L
        );
        assertThat( metrics.meter( "io.werval.http.success" ).getCount(), is( 5L ) );
        assertThat( metrics.meter( "io.werval.http.redirections" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "io.werval.http.client-errors" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "io.werval.http.server-errors" ).getCount(), is( 0L ) );
        assertThat( metrics.meter( "io.werval.http.unknown" ).getCount(), is( 0L ) );

        expect().statusCode( 200 ).when().get( "/redir" ); // Follows the redirection to / that returns 200
        expect().statusCode( 404 ).when().get( "/not-found" );
        expect().statusCode( 500 ).when().get( "/error" );
        // Wait for https://code.google.com/p/rest-assured/issues/detail?id=382 to be fixed
        // expect().statusCode( 666 ).when().get( "/unknown" );

        await( "io.werval.http.requests == 9" ).until(
            () -> metrics.timer( "io.werval.http.requests" ).getCount() == 9L // 10L
        );
        assertThat( metrics.meter( "io.werval.http.success" ).getCount(), is( 6L ) );
        assertThat( metrics.meter( "io.werval.http.redirections" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "io.werval.http.client-errors" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "io.werval.http.server-errors" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "io.werval.http.unknown" ).getCount(), is( 0L ) ); // 1L

        // assertThat( metrics.counter( "unknown-counter" ).getCount(), is( 1L ) );
        // assertThat( metrics.meter( "unknown-meter" ).getCount(), is( 1L ) );
        // assertThat( metrics.timer( "unknown-timer" ).getCount(), is( 1L ) );
        // assertThat( metrics.counter( "MetricsTest" ).getCount(), is( 1L ) );

        // JMX
        MBeanServer jmx = ManagementFactory.getPlatformMBeanServer();
        assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.requests" ), "Count" ), is( 9L ) ); // 10L
        assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.success" ), "Count" ), is( 6L ) );
        assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.redirections" ), "Count" ), is( 1L ) );
        assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.client-errors" ), "Count" ), is( 1L ) );
        assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.server-errors" ), "Count" ), is( 1L ) );
        // assertThat( jmx.getAttribute( new ObjectName( "metrics:name=io.werval.http.unknown" ), "Count" ), is( 1L ) );
        // assertThat( jmx.getAttribute( new ObjectName( "metrics:name=unknown-counter" ), "Count" ), is( 1L ) );
        // assertThat( jmx.getAttribute( new ObjectName( "metrics:name=unknown-meter" ), "Count" ), is( 1L ) );
        // assertThat( jmx.getAttribute( new ObjectName( "metrics:name=unknown-timer" ), "Count" ), is( 1L ) );
        // assertThat( jmx.getAttribute( new ObjectName( "metrics:name=MetricsTest" ), "Count" ), is( 1L ) );
    }
}
