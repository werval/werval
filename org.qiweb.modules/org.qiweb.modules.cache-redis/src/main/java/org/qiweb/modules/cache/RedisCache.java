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
import org.qiweb.api.cache.Cache;
import org.qiweb.api.util.Serializables;
import redis.clients.jedis.Jedis;

import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Redis Cache.
 */
/* package */ class RedisCache
    implements Cache
{
    private final Jedis backingCache;
    // WARN Use a prefix per instance to start from a fresh cache on each Application activation
    private final String prefix = UUID.randomUUID().toString() + "_";

    /* package */ RedisCache( Jedis backingCache )
    {
        this.backingCache = backingCache;
    }

    @Override
    public boolean has( String key )
    {
        return backingCache.exists( ( prefix + key ).getBytes( UTF_8 ) );
    }

    @Override
    public <T> T get( String key )
    {
        byte[] element = backingCache.get( ( prefix + key ).getBytes( UTF_8 ) );
        if( element == null )
        {
            return null;
        }
        return Serializables.fromBytes( element );
    }

    @Override
    public <T> T getOrSetDefault( String key, int ttlSeconds, T defaultValue )
    {
        byte[] keyBytes = ( prefix + key ).getBytes( UTF_8 );
        byte[] element = backingCache.get( keyBytes );
        if( element == null )
        {
            backingCache.setex( keyBytes, ttl( ttlSeconds ), Serializables.toBytes( defaultValue ) );
            return defaultValue;
        }
        return Serializables.fromBytes( element );
    }

    @Override
    public <T> void set( int ttlSeconds, String key, T value )
    {
        backingCache.setex( ( prefix + key ).getBytes( UTF_8 ), ttl( ttlSeconds ), Serializables.toBytes( value ) );
    }

    @Override
    public void remove( String key )
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
