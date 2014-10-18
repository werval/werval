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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.SharedHealthCheckRegistries;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.qiweb.api.Application;
import org.qiweb.api.Mode;
import org.qiweb.api.Plugin;
import org.qiweb.api.events.ConnectionEvent;
import org.qiweb.api.events.Event;
import org.qiweb.api.events.HttpEvent;
import org.qiweb.api.events.Registration;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static org.qiweb.api.Mode.DEV;
import static org.qiweb.api.http.Method.GET;

/**
 * Metrics Plugin.
 */
public class MetricsPlugin
    implements Plugin<Metrics>
{
    private static final String HTTP_METRIC_NAME_PREFIX = "qiweb.http";
    private Map<String, Timer.Context> requestTimers = EMPTY_MAP;
    private List<Reporter> reporters = EMPTY_LIST;
    private Metrics api;
    private Registration eventRegistration;

    @Override
    public Class<Metrics> apiType()
    {
        return Metrics.class;
    }

    @Override
    public Metrics api()
    {
        return api;
    }

    @Override
    public List<Route> firstRoutes( Mode mode, RouteBuilder builder )
    {
        if( mode == DEV )
        {
            return asList(
                builder.route( GET ).on( "/@metrics" ).to( Tools.class, c -> c.devShellIndex() ).build(),
                builder.route( GET ).on( "/@metrics/metrics" ).to( Tools.class, c -> c.metrics() ).build(),
                builder.route( GET ).on( "/@metrics/health-checks" ).to( Tools.class, c -> c.healthchecks() ).build(),
                builder.route( GET ).on( "/@metrics/thread-dump" ).to( Tools.class, c -> c.threadDump() ).build()
            );
        }
        return EMPTY_LIST;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        requestTimers = new ConcurrentHashMap<>();
        reporters = new ArrayList<>();

        MetricRegistry metrics = new MetricRegistry();
        HealthCheckRegistry healthChecks = new HealthCheckRegistry();

        // JVM Meters
        metrics.register( "jvm.bufferpools", new BufferPoolMetricSet( ManagementFactory.getPlatformMBeanServer() ) );
        metrics.register( "jvm.threadstates", new ThreadStatesGaugeSet() );
        metrics.register( "jvm.classloading", new ClassLoadingGaugeSet() );
        metrics.register( "jvm.garbagecollection", new GarbageCollectorMetricSet() );
        metrics.register( "jvm.memory", new MemoryUsageGaugeSet() );
        metrics.register( "jvm.filedescriptors.ratio", new FileDescriptorRatioGauge() );

        // JVM HealthChecks
        healthChecks.register( "jvm.deadlocks", new ThreadDeadlockHealthCheck() );

        // JMX Report
        JmxReporter jmx = JmxReporter.forRegistry( metrics )
            .convertRatesTo( TimeUnit.SECONDS )
            .convertDurationsTo( TimeUnit.MILLISECONDS )
            .build();
        jmx.start();
        reporters.add( jmx );

        // Console Report
        ConsoleReporter console = ConsoleReporter.forRegistry( metrics )
            .convertRatesTo( TimeUnit.SECONDS )
            .convertDurationsTo( TimeUnit.MILLISECONDS )
            .build();
        console.start( 1, TimeUnit.MINUTES );
        reporters.add( console );

        // SLF4J Report
        final Slf4jReporter slf4j = Slf4jReporter.forRegistry( metrics )
            .outputTo( LoggerFactory.getLogger( "org.qiweb.metrics" ) )
            .withLoggingLevel( Slf4jReporter.LoggingLevel.INFO )
            .markWith( MarkerFactory.getMarker( "metrics" ) )
            .convertRatesTo( TimeUnit.SECONDS )
            .convertDurationsTo( TimeUnit.MILLISECONDS )
            .build();
        slf4j.start( 1, TimeUnit.HOURS );
        reporters.add( slf4j );

        // CSV Report
        File csvReportDir = new File( "metrics" );
        csvReportDir.mkdirs();
        final CsvReporter reporter = CsvReporter.forRegistry( metrics )
            .formatFor( Locale.US )
            .convertRatesTo( TimeUnit.SECONDS )
            .convertDurationsTo( TimeUnit.MILLISECONDS )
            .build( csvReportDir );
        reporter.start( 1, TimeUnit.DAYS );

        eventRegistration = application.events().registerListener( this::handleEvent );

        api = new Metrics( metrics, healthChecks );
    }

    @Override
    public void onPassivate( Application application )
    {
        requestTimers.values().forEach( t -> t.stop() );
        requestTimers = EMPTY_MAP;
        reporters.forEach(
            r ->
            {
                if( r instanceof ScheduledReporter )
                {
                    ( (ScheduledReporter) r ).stop();
                }
                else if( r instanceof JmxReporter )
                {
                    ( (JmxReporter) r ).stop();
                }
            }
        );
        reporters = EMPTY_LIST;
        api = null;
        eventRegistration.unregister();
        SharedMetricRegistries.clear();
        SharedHealthCheckRegistries.clear();
    }

    private void handleEvent( Event e )
    {
        if( e instanceof ConnectionEvent.Opened )
        {
            // Increment open-connections Counter
            api.metrics().counter( name( HTTP_METRIC_NAME_PREFIX, "open-connections" ) ).inc();
        }
        else if( e instanceof ConnectionEvent.Closed )
        {
            // Decrement open-connections Counter
            api.metrics().counter( name( HTTP_METRIC_NAME_PREFIX, "open-connections" ) ).dec();
        }
        else if( e instanceof HttpEvent.RequestReceived )
        {
            // Start requests Timer
            requestTimers.put(
                ( (HttpEvent.RequestReceived) e ).identity(),
                api.metrics().timer( name( HTTP_METRIC_NAME_PREFIX, "requests" ) ).time()
            );
        }
        else if( e instanceof HttpEvent.ResponseSent )
        {
            // Stop requests Timer
            Optional.ofNullable( requestTimers.remove( ( (HttpEvent.ResponseSent) e ).identity() ) )
                .ifPresent( t -> t.close() );

            // Mark appropriate response status class Meter
            switch( ( (HttpEvent.ResponseSent) e ).status().statusClass() )
            {
                case CLIENT_ERROR:
                    api.metrics().meter( name( HTTP_METRIC_NAME_PREFIX, "client-errors" ) ).mark();
                    break;
                case SERVER_ERROR:
                    api.metrics().meter( name( HTTP_METRIC_NAME_PREFIX, "server-errors" ) ).mark();
                    break;
                case REDIRECTION:
                    api.metrics().meter( name( HTTP_METRIC_NAME_PREFIX, "redirections" ) ).mark();
                    break;
                case SUCCESS:
                    api.metrics().meter( name( HTTP_METRIC_NAME_PREFIX, "success" ) ).mark();
                    break;
                case INFORMATIONAL:
                case UNKNOWN:
                default:
                    api.metrics().meter( name( HTTP_METRIC_NAME_PREFIX, "unknown" ) ).mark();
            }
        }
    }
}
