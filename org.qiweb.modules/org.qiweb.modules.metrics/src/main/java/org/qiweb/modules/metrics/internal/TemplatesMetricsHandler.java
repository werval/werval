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
package org.qiweb.modules.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Templates Metrics Handler.
 */
public interface TemplatesMetricsHandler
{
    /**
     * Closeable.
     */
    interface Closeable
        extends AutoCloseable
    {
        @Override
        void close();
    }

    Closeable namedRenderTimer( String templateName );

    Closeable inlineRenderTimer();

    TemplatesMetricsHandler NOOP = new Impl.NoopHandler();

    /**
     * Implementation.
     */
    static final class Impl
        implements TemplatesMetricsHandler
    {
        private static final class NoopCloseable
            implements Closeable
        {
            @Override
            public void close()
            {
                // NOOP
            }
        }

        private static final Closeable NOOP_CLOSEABLE = new NoopCloseable();

        private static final class NoopHandler
            implements TemplatesMetricsHandler
        {
            @Override
            public Closeable namedRenderTimer( String templateName )
            {
                return NOOP_CLOSEABLE;
            }

            @Override
            public Closeable inlineRenderTimer()
            {
                return NOOP_CLOSEABLE;
            }
        }
        private final MetricRegistry metrics;
        private final String namedRenderTimerName;
        private final String inlineRenderTimerName;

        public Impl( MetricRegistry metrics, String implName )
        {
            this.metrics = metrics;
            this.namedRenderTimerName = name( "org.qiweb.modules.templates", implName, "named" );
            this.inlineRenderTimerName = name( "org.qiweb.modules.templates", implName, "inline" );
        }

        @Override
        public Closeable namedRenderTimer( String templateName )
        {
            Timer.Context renderTimer = metrics.timer( namedRenderTimerName ).time();
            Timer.Context renderNameTimer = metrics.timer( name( namedRenderTimerName, templateName ) ).time();
            return () ->
            {
                renderTimer.close();
                renderNameTimer.close();
            };
        }

        @Override
        public Closeable inlineRenderTimer()
        {
            Timer.Context renderTimer = metrics.timer( inlineRenderTimerName ).time();
            return () -> renderTimer.close();
        }
    }
}
