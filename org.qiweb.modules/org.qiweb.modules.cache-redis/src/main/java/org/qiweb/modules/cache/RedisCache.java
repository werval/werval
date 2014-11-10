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
package org.qiweb.modules.cache;

import java.util.UUID;
import org.qiweb.modules.metrics.Metrics;
import org.qiweb.modules.metrics.internal.CacheMetricsListener;
import org.qiweb.spi.cache.CacheAdapter;
import org.qiweb.util.Serializables;
import redis.clients.jedis.Jedis;

import static org.qiweb.util.Charsets.UTF_8;

/**
 * Redis Cache.
 */
/* package */ class RedisCache
    extends CacheAdapter
{
    private final Jedis backingCache;
    // WARN Use a prefix per instance to start from a fresh cache on each Application activation
    private final String prefix = UUID.randomUUID().toString() + "_";

    /* package */ RedisCache( Jedis backingCache )
    {
        super();
        this.backingCache = backingCache;
    }

    /* package */ RedisCache( Metrics metrics, Jedis backingCache )
    {
        super( new CacheMetricsListener( metrics.metrics(), "redis", "qiweb-cache" ) );
        this.backingCache = backingCache;
    }

    @Override
    protected <T> T doGet( String key )
    {
        byte[] element = backingCache.get( ( prefix + key ).getBytes( UTF_8 ) );
        if( element == null )
        {
            return null;
        }
        return Serializables.fromBytes( element );
    }

    @Override
    protected <T> void doSet( int ttlSeconds, String key, T value )
    {
        backingCache.setex( ( prefix + key ).getBytes( UTF_8 ), ttl( ttlSeconds ), Serializables.toBytes( value ) );
    }

    @Override
    protected void doRemove( String key )
    {
        backingCache.del( ( prefix + key ).getBytes( UTF_8 ) );
    }

    private static int ttl( int ttlSeconds )
    {
        if( ttlSeconds == 0 )
        {
            return Integer.MAX_VALUE;
        }
        return ttlSeconds;
    }
}
