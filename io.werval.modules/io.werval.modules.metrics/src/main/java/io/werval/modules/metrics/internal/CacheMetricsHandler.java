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
package io.werval.modules.metrics.internal;

import java.util.function.Function;

import io.werval.spi.cache.CacheAdapter.CacheEvent;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Cache Metrics Handler.
 * <p>
 * Suited to give to {@link CacheAdapter} appropriate constructor in {@link Cache} implementations.
 */
public class CacheMetricsHandler
    implements Function<CacheEvent, CacheEvent.Closeable>
{
    private static final String METRICS_PREFIX = "io.werval.modules.cache";
    private final MetricRegistry metrics;
    private final String hitsName;
    private final String missesName;
    private final String getsName;
    private final String setsName;
    private final String removesName;

    public CacheMetricsHandler( MetricRegistry metrics, String implName, String cacheName )
    {
        this.metrics = metrics;
        this.hitsName = name( METRICS_PREFIX, implName, cacheName, "hits" );
        this.missesName = name( METRICS_PREFIX, implName, cacheName, "misses" );
        this.getsName = name( METRICS_PREFIX, implName, cacheName, "gets" );
        this.setsName = name( METRICS_PREFIX, implName, cacheName, "sets" );
        this.removesName = name( METRICS_PREFIX, implName, cacheName, "removes" );
    }

    @Override
    public CacheEvent.Closeable apply( CacheEvent event )
    {
        switch( event )
        {
            case HIT:
                metrics.meter( hitsName ).mark();
                break;
            case MISS:
                metrics.meter( missesName ).mark();
                break;
            case GET:
                Timer.Context getTimer = metrics.timer( getsName ).time();
                return () -> getTimer.close();
            case SET:
                Timer.Context setTimer = metrics.timer( setsName ).time();
                return () -> setTimer.close();
            case REMOVE:
                Timer.Context removeTimer = metrics.timer( removesName ).time();
                return () -> removeTimer.close();
            default:
                break;
        }
        return CacheEvent.NOOP_CLOSEABLE;
    }
}
