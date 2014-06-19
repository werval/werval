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
package org.qiweb.runtime;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import org.qiweb.api.Config;
import org.qiweb.api.Mode;
import org.qiweb.runtime.context.ContextExecutor;
import org.qiweb.runtime.util.ForkJoinPoolNamedThreadFactory;
import org.qiweb.runtime.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_MAP;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.qiweb.runtime.ConfigKeys.APP_EXECUTORS;
import static org.qiweb.runtime.ConfigKeys.APP_EXECUTORS_DEFAULT;
import static org.qiweb.runtime.ConfigKeys.APP_EXECUTORS_SHUTDOWN_TIMEOUT;

/**
 * Application Executors.
 */
/* package */ class ApplicationExecutors
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplicationExecutors.class );
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final Mode mode;
    private final Config config;
    private String defaultExecutor;
    private String defaultExecutorThreadNamePrefix;
    private Long shutdownTimeoutMillis;
    private Map<String, ExecutorService> executors = EMPTY_MAP;

    /* package */ ApplicationExecutors( Mode mode, Config config )
    {
        this.mode = mode;
        this.config = config;
    }

    /* package */ void activate()
    {
        this.defaultExecutor = config.string( APP_EXECUTORS_DEFAULT );
        this.defaultExecutorThreadNamePrefix = config.string(
            APP_EXECUTORS + "." + defaultExecutor + ".thread_name_prefix"
        );
        this.shutdownTimeoutMillis = config.milliseconds( APP_EXECUTORS_SHUTDOWN_TIMEOUT );
        Config executorsConfig = config.object( APP_EXECUTORS );
        this.executors = new HashMap<>( executorsConfig.subKeys().size() - 1 );
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
            // Exception handling
            // TODO: UncaughtExceptionHandler for Global.onExecutionError(...) & error recording
            // Development mode
            if( defaultExecutor.equals( executorName ) && mode == Mode.DEV )
            {
                executors.put(
                    executorName,
                    new ContextExecutor( newSingleThreadExecutor( new NamedThreadFactory( namePrefix ) ) )
                );
                continue;
            }
            // Create executor
            switch( type )
            {
                case "fork-join":
                    executors.put(
                        executorName,
                        new ContextExecutor(
                            new ForkJoinPool(
                                count,
                                new ForkJoinPoolNamedThreadFactory( namePrefix ),
                                null,
                                false
                            )
                        )
                    );
                    break;
                case "thread-pool":
                default:
                    executors.put(
                        executorName,
                        new ContextExecutor( newFixedThreadPool( count, new NamedThreadFactory( namePrefix ) ) )
                    );
            }
        }
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
    }

    /* package */ ExecutorService defaultExecutor()
    {
        return executors.get( defaultExecutor );
    }

    /* package */ boolean inDefaultExecutor()
    {
        return Thread.currentThread().getName().startsWith( defaultExecutorThreadNamePrefix );
    }
}
