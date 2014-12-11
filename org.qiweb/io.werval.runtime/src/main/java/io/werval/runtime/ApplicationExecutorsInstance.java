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
package io.werval.runtime;

import io.werval.api.ApplicationExecutors;
import io.werval.api.Config;
import io.werval.api.Global;
import io.werval.runtime.context.ContextExecutor;
import io.werval.runtime.util.ForkJoinPoolNamedThreadFactory;
import io.werval.runtime.util.NamedThreadFactory;
import io.werval.spi.ApplicationSPI;
import io.werval.util.Couple;
import io.werval.util.Strings;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.runtime.ConfigKeys.APP_EXECUTORS;
import static io.werval.runtime.ConfigKeys.APP_EXECUTORS_DEFAULT;
import static io.werval.runtime.ConfigKeys.APP_EXECUTORS_SHUTDOWN_TIMEOUT;
import static java.util.Collections.EMPTY_MAP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Application Executors.
 */
/* package */ class ApplicationExecutorsInstance
    implements ApplicationExecutors
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplicationExecutorsInstance.class );
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * A Fixed ThreadPool that handle uncaught exceptions.
     *
     * See http://stackoverflow.com/questions/1838923/why-is-uncaughtexceptionhandler-not-called-by-executorservice
     */
    private static class UncaughtExceptionHandlerThreadPool
        extends ThreadPoolExecutor
    {
        private final ApplicationSPI application;

        public UncaughtExceptionHandlerThreadPool(
            ApplicationSPI application,
            int size,
            ThreadFactory threadFactory
        )
        {
            super( size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory );
            this.application = application;
        }

        @Override
        protected void afterExecute( Runnable runnable, Throwable cause )
        {
            if( cause != null )
            {
                handleUncaughtException( application, application.global(), cause );
            }
        }
    }

    private static final class UncaughtExceptionHandler
        implements Thread.UncaughtExceptionHandler
    {
        private final ApplicationSPI application;

        private UncaughtExceptionHandler( ApplicationSPI application )
        {
            this.application = application;
        }

        @Override
        public void uncaughtException( Thread thread, Throwable cause )
        {
            handleUncaughtException( application, application.global(), cause );
        }
    }

    private static void handleUncaughtException( ApplicationSPI application, Global global, Throwable cause )
    {
        // Clean-up stacktrace
        Throwable rootCause = ErrorHandling.cleanUpStackTrace( application, LOG, cause );
        ApplicationExecutors executors = application.executors();
        try
        {
            // Notify Global
            if( executors.inDefaultExecutor() )
            {
                global.onApplicationError( application, rootCause );
            }
            else
            {
                executors.runAsync( () -> global.onApplicationError( application, rootCause ) ).join();
            }
        }
        catch( Exception ex )
        {
            // Add as suppressed and replay Global default behaviour. This serve as a fault barrier
            rootCause.addSuppressed( ex );
            if( executors.inDefaultExecutor() )
            {
                new Global().onApplicationError( application, rootCause );
            }
            else
            {
                executors.runAsync( () -> new Global().onApplicationError( application, rootCause ) ).join();
            }
        }
        // Record error
        application.errors().record(
            null,
            "Uncaught Exception: " + rootCause.getClass() + ": " + rootCause.getMessage(),
            rootCause
        );
    }

    private final ApplicationSPI application;
    private String defaultExecutor;
    private String defaultExecutorThreadNamePrefix;
    private Long shutdownTimeoutMillis;
    private Map<String, ExecutorService> executors = EMPTY_MAP;
    private Map<String, String> executorsThreadNamePrefixes = EMPTY_MAP;
    private String summary;

    /* package */ ApplicationExecutorsInstance( ApplicationSPI application )
    {
        this.application = application;
    }

    /* package */ void activate()
    {
        this.defaultExecutor = application.config().string( APP_EXECUTORS_DEFAULT );
        this.defaultExecutorThreadNamePrefix = application.config().string(
            APP_EXECUTORS + "." + defaultExecutor + ".thread_name_prefix"
        );
        this.shutdownTimeoutMillis = application.config().milliseconds( APP_EXECUTORS_SHUTDOWN_TIMEOUT );
        Config executorsConfig = application.config().object( APP_EXECUTORS );
        int executorsCount = executorsConfig.subKeys().size();
        this.executors = new HashMap<>( executorsCount - 1 );
        this.executorsThreadNamePrefixes = new HashMap<>( executorsCount );
        Map<String, Couple<String, Integer>> summaryData = new HashMap<>( executorsCount );
        for( String executorName : executorsConfig.subKeys() )
        {
            // Skip the default executor and shutdown config entries
            if( "default".equals( executorName ) || "shutdown".equals( executorName ) )
            {
                continue;
            }
            // Load configuration
            String typeKey = executorName + ".type";
            String countKey = executorName + ".count";
            String namePrefixKey = executorName + ".thread_name_prefix";
            String type = executorsConfig.has( typeKey )
                          ? executorsConfig.string( typeKey )
                          : "thread-pool";
            int count = executorsConfig.has( countKey )
                        ? executorsConfig.intNumber( countKey )
                        : DEFAULT_POOL_SIZE;
            String namePrefix = executorsConfig.has( namePrefixKey )
                                ? executorsConfig.string( namePrefixKey )
                                : executorName + "_thread";

            // Override default executor in development mode
            // if( defaultExecutor.equals( executorName ) && application.mode() == Mode.DEV )
            // {
            //     // TODO Investigate how we could limit concurrency in development mode!
            //     type = "thread-pool";
            //     count = 1;
            // }

            // Create executor
            ExecutorService executor;
            switch( type )
            {
                case "fork-join":
                    executor = new ContextExecutor(
                        new ForkJoinPool(
                            count,
                            new ForkJoinPoolNamedThreadFactory( namePrefix ),
                            new UncaughtExceptionHandler( application ),
                            false
                        )
                    );
                    executors.put( executorName, executor );
                    break;
                case "thread-pool":
                default:
                    executor = new ContextExecutor(
                        new UncaughtExceptionHandlerThreadPool(
                            application,
                            count,
                            new NamedThreadFactory( namePrefix )
                        )
                    );
                    executors.put( executorName, executor );
            }
            executorsThreadNamePrefixes.put( executorName, namePrefix );
            summaryData.put( executorName, Couple.of( type, count ) );
        }

        // Generate summary
        StringBuilder summaryBuilder = new StringBuilder();
        Couple<String, Integer> defaultData = summaryData.get( defaultExecutor );
        summaryBuilder.append( defaultExecutor ).append( " (default): " )
            .append( defaultData.left() ).append( "[" ).append( defaultData.right() ).append( "]\n" );
        for( Map.Entry<String, Couple<String, Integer>> entry : summaryData.entrySet() )
        {
            String name = entry.getKey();
            if( !defaultExecutor.equals( name ) )
            {
                Couple<String, Integer> data = entry.getValue();
                summaryBuilder.append( name ).append( ": " )
                    .append( data.left() ).append( "[" ).append( data.right() ).append( "]\n" );
            }
        }
        summary = summaryBuilder.toString();
    }

    /* package */ void passivate()
    {
        // Default executor first
        defaultExecutor().shutdown();

        // All other executors
        for( Map.Entry<String, ExecutorService> executor : executors.entrySet() )
        {
            // Skip default
            if( defaultExecutor.equals( executor.getKey() ) )
            {
                continue;
            }
            executor.getValue().shutdown();
        }

        // Await for default executor
        Map<String, List<Runnable>> notRun = new LinkedHashMap<>( executors.size() );
        try
        {
            defaultExecutor().awaitTermination( shutdownTimeoutMillis, MILLISECONDS );
        }
        catch( InterruptedException ex )
        {
            List<Runnable> failed = defaultExecutor().shutdownNow();
            if( !failed.isEmpty() )
            {
                notRun.put( defaultExecutor, failed );
            }
        }

        // Shutdown all other executors now
        for( Map.Entry<String, ExecutorService> executor : executors.entrySet() )
        {
            // Skip default
            if( defaultExecutor.equals( executor.getKey() ) )
            {
                continue;
            }
            List<Runnable> failed = executor.getValue().shutdownNow();
            if( !failed.isEmpty() )
            {
                notRun.put( defaultExecutor, failed );
            }
        }

        // Eventually log the tasks that were awaiting execution
        if( !notRun.isEmpty() )
        {
            LOG.warn(
                "Some Application Executors failed to complete tasks before shutdown timeout: {} - {}",
                notRun, "Watch out! Zombie threads!"
            );
        }

        // Cleanup
        defaultExecutor = null;
        shutdownTimeoutMillis = null;
        executors = EMPTY_MAP;
        executorsThreadNamePrefixes = EMPTY_MAP;
        summary = null;
    }

    @Override
    public ExecutorService defaultExecutor()
    {
        return executors.get( defaultExecutor );
    }

    @Override
    public boolean inDefaultExecutor()
    {
        return Thread.currentThread().getName().startsWith( defaultExecutorThreadNamePrefix );
    }

    @Override
    public ExecutorService executor( String executorName )
    {
        ensureNotEmpty( "Application Executor Name", executorName );
        return executors.get( executorName );
    }

    @Override
    public boolean inExecutor( String executorName )
    {
        ensureNotEmpty( "Application Executor Name", executorName );
        String executorThreadNamePrefix = executorsThreadNamePrefixes.get( executorName );
        return Strings.hasText( executorThreadNamePrefix )
               && Thread.currentThread().getName().startsWith( executorThreadNamePrefix );
    }

    @Override
    public String toString()
    {
        return summary;
    }
}
