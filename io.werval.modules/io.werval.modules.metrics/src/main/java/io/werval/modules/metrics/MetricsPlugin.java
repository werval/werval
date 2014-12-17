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
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Mode;
import io.werval.api.Plugin;
import io.werval.api.events.ConnectionEvent;
import io.werval.api.events.Event;
import io.werval.api.events.HttpEvent;
import io.werval.api.events.Registration;
import io.werval.api.exceptions.ActivationException;
import io.werval.api.routes.Route;
import io.werval.api.routes.RouteBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import io.werval.modules.json.JSON;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import static io.werval.api.Mode.DEV;
import static io.werval.api.http.Method.GET;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Metrics Plugin.
 */
public class MetricsPlugin
    implements Plugin<Metrics>
{
    /**
     * Event Listener used for Connection and Http metrics.
     */
    private static final class EventListener
        implements Consumer<Event>
    {
        private final MetricRegistry metrics;
        private final Map<String, Timer.Context> requestTimers;
        private final boolean connections;
        private final boolean requests;
        private final boolean success;
        private final boolean redirections;
        private final boolean clientErrors;
        private final boolean serverErrors;
        private final boolean unknown;

        private EventListener(
            MetricRegistry metrics, Map<String, Timer.Context> requestTimers,
            boolean connections, boolean requests,
            boolean success, boolean redirections, boolean clientErrors, boolean serverErrors, boolean unknown
        )
        {
            this.metrics = metrics;
            this.requestTimers = requestTimers;
            this.connections = connections;
            this.requests = requests;
            this.success = success;
            this.redirections = redirections;
            this.clientErrors = clientErrors;
            this.serverErrors = serverErrors;
            this.unknown = unknown;
        }

        @Override
        public void accept( Event e )
        {
            if( connections && e instanceof ConnectionEvent.Opened )
            {
                // Increment open-connections Counter
                metrics.counter( "io.werval.http.open-connections" ).inc();
            }
            else if( connections && e instanceof ConnectionEvent.Closed )
            {
                // Decrement open-connections Counter
                metrics.counter( "io.werval.http.open-connections" ).dec();
            }
            else if( requests && e instanceof HttpEvent.RequestReceived )
            {
                // Start requests Timer
                requestTimers.put(
                    ( (HttpEvent.RequestReceived) e ).identity(),
                    metrics.timer( "io.werval.http.requests" ).time()
                );
            }
            else if( e instanceof HttpEvent.ResponseSent )
            {
                if( requests )
                {
                    // Stop requests Timer
                    Optional.ofNullable( requestTimers.remove( ( (HttpEvent.ResponseSent) e ).identity() ) )
                        .ifPresent( t -> t.close() );
                }

                // Mark appropriate response status class Meter
                switch( ( (HttpEvent.ResponseSent) e ).status().statusClass() )
                {
                    case SUCCESS:
                        if( success )
                        {
                            metrics.meter( "io.werval.http.success" ).mark();
                        }
                        break;
                    case REDIRECTION:
                        if( redirections )
                        {
                            metrics.meter( "io.werval.http.redirections" ).mark();
                        }
                        break;
                    case CLIENT_ERROR:
                        if( clientErrors )
                        {
                            metrics.meter( "io.werval.http.client-errors" ).mark();
                        }
                        break;
                    case SERVER_ERROR:
                        if( serverErrors )
                        {
                            metrics.meter( "io.werval.http.server-errors" ).mark();
                        }
                        break;
                    case INFORMATIONAL:
                    case UNKNOWN:
                    default:
                        if( unknown )
                        {
                            metrics.meter( "io.werval.http.unknown" ).mark();
                        }
                }
            }
        }
    }

    private Map<String, Timer.Context> requestTimers;
    private List<Reporter> reporters;
    private Metrics api;
    private Registration eventRegistration;

    @Override
    public Class<Metrics> apiType()
    {
        return Metrics.class;
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        return Arrays.asList( JSON.class );
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
        application.plugin( JSON.class ).mapper()
            .registerModule( new MetricsModule( SECONDS, MILLISECONDS, true ) )
            .registerModule( new HealthCheckModule() );
        MetricRegistry metrics = new MetricRegistry();
        HealthCheckRegistry healthChecks = new HealthCheckRegistry();

        registerMetrics( application, metrics );
        registerMetricsReporters( application, metrics );
        registerHealthChecks( application, healthChecks );

        api = new Metrics( metrics, healthChecks );
    }

    @Override
    public void onPassivate( Application application )
    {
        requestTimers.values().forEach( t -> t.stop() );
        requestTimers = null;
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
        reporters = null;
        api = null;
        eventRegistration.unregister();
        eventRegistration = null;
        SharedMetricRegistries.clear();
        SharedHealthCheckRegistries.clear();
    }

    private void registerMetrics( Application application, MetricRegistry metrics )
    {
        Config config = application.config().atKey( "metrics" );

        // JVM Meters
        if( config.bool( "jvm.bufferpools.enabled" ) )
        {
            metrics.register( "jvm.bufferpools", new BufferPoolMetricSet( getPlatformMBeanServer() ) );
        }
        if( config.bool( "jvm.threadstates.enabled" ) )
        {
            metrics.register( "jvm.threadstates", new ThreadStatesGaugeSet() );
        }
        if( config.bool( "jvm.classloading.enabled" ) )
        {
            metrics.register( "jvm.classloading", new ClassLoadingGaugeSet() );
        }
        if( config.bool( "jvm.garbagecollection.enabled" ) )
        {
            metrics.register( "jvm.garbagecollection", new GarbageCollectorMetricSet() );
        }
        if( config.bool( "jvm.memory.enabled" ) )
        {
            metrics.register( "jvm.memory", new MemoryUsageGaugeSet() );
        }
        if( config.bool( "jvm.filedescriptors.enabled" ) )
        {
            metrics.register( "jvm.filedescriptors.ratio", new FileDescriptorRatioGauge() );
        }

        // Connection & HTTP Metrics
        requestTimers = new ConcurrentHashMap<>();
        eventRegistration = application.events().registerListener(
            new EventListener(
                metrics,
                requestTimers,
                config.bool( "http.connections.enabled" ),
                config.bool( "http.requests.enabled" ),
                config.bool( "http.success.enabled" ),
                config.bool( "http.redirections.enabled" ),
                config.bool( "http.client_errors.enabled" ),
                config.bool( "http.server_errors.enabled" ),
                config.bool( "http.unknown.enabled" )
            )
        );
    }

    private void registerMetricsReporters( Application application, MetricRegistry metrics )
    {
        Config config = application.config().atKey( "metrics" );

        reporters = new ArrayList<>();

        // JMX Reporter
        if( config.bool( "reports.jmx.enabled" ) )
        {
            JmxReporter jmx = JmxReporter.forRegistry( metrics )
                .convertRatesTo( TimeUnit.SECONDS )
                .convertDurationsTo( TimeUnit.MILLISECONDS )
                .build();
            jmx.start();
            reporters.add( jmx );
        }

        // Console Reporter
        if( config.bool( "reports.console.enabled" ) )
        {
            ConsoleReporter console = ConsoleReporter.forRegistry( metrics )
                .convertRatesTo( TimeUnit.SECONDS )
                .convertDurationsTo( TimeUnit.MILLISECONDS )
                .build();
            console.start( config.seconds( "reports.console.periodicity" ), TimeUnit.SECONDS );
            reporters.add( console );
        }

        // SLF4J Reporter
        if( config.bool( "reports.slf4j.enabled" ) )
        {
            final Slf4jReporter slf4j = Slf4jReporter.forRegistry( metrics )
                .outputTo( LoggerFactory.getLogger( config.string( "reports.slf4j.logger" ) ) )
                .withLoggingLevel(
                    Slf4jReporter.LoggingLevel.valueOf( config.string( "reports.slf4j.level" ).toUpperCase( US ) )
                )
                .markWith( MarkerFactory.getMarker( "metrics" ) )
                .convertRatesTo( TimeUnit.SECONDS )
                .convertDurationsTo( TimeUnit.MILLISECONDS )
                .build();
            slf4j.start( config.seconds( "reports.slf4j.periodicity" ), TimeUnit.SECONDS );
            reporters.add( slf4j );
        }

        // CSV Reporter
        if( config.bool( "reports.csv.enabled" ) )
        {
            File csvReportDir = new File( config.string( "reports.csv.directory" ) );
            csvReportDir.mkdirs();
            final CsvReporter csv = CsvReporter.forRegistry( metrics )
                .formatFor( Locale.forLanguageTag( config.string( "reports.csv.locale" ) ) )
                .convertRatesTo( TimeUnit.SECONDS )
                .convertDurationsTo( TimeUnit.MILLISECONDS )
                .build( csvReportDir );
            csv.start( config.seconds( "reports.csv.periodicity" ), TimeUnit.SECONDS );
            reporters.add( csv );
        }
    }

    private void registerHealthChecks( Application application, HealthCheckRegistry healthChecks )
    {
        Config config = application.config().atKey( "metrics" );

        // JVM HealthChecks
        if( config.bool( "healthchecks.deadlocks.enabled" ) )
        {
            healthChecks.register( "jvm.deadlocks", new ThreadDeadlockHealthCheck() );
        }
    }
}
