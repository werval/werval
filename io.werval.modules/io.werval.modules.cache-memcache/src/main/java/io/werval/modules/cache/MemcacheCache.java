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
package io.werval.modules.cache;

import io.werval.spi.cache.CacheAdapter;
import java.util.UUID;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import io.werval.modules.metrics.Metrics;
import io.werval.modules.metrics.internal.CacheMetricsHandler;

/**
 * Memcache Cache.
 */
/* package */ class MemcacheCache
    extends CacheAdapter
{
    private final MemcachedClient client;
    // WARN Use a prefix per instance to start from a fresh cache on each Application activation
    private final String prefix = UUID.randomUUID().toString() + "_";

    /* package */ MemcacheCache( MemcachedClient client )
    {
        super();
        this.client = client;
    }

    /* package */ MemcacheCache( Metrics metrics, MemcachedClient client )
    {
        super(new CacheMetricsHandler( metrics.metrics(), "memcache", "werval-cache" ) );
        this.client = client;
    }

    @Override
    protected <T> T doGet( String key )
    {
        return (T) client.get( prefix + key, new SerializingTranscoder() );
    }

    @Override
    protected <T> void doSet( int ttlSeconds, String key, T value )
    {
        client.set( prefix + key, ttl( ttlSeconds ), value, new SerializingTranscoder() );
    }

    @Override
    protected void doRemove( String key )
    {
        client.delete( prefix + key );
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
