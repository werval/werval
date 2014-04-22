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

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.util.Numbers;

/**
 * Memcache Cache.
 */
/* package */ class MemcacheCache
    implements Cache
{
    private final MemcachedClient client;
    // WARN Use a prefix per instance to start from a fresh cache on each Application activation
    private final String prefix = UUID.randomUUID().toString() + "_";

    /* package */ MemcacheCache( MemcachedClient client )
    {
        this.client = client;
    }

    @Override
    public boolean has( String key )
    {
        return client.get( prefix + key ) != null;
    }

    @Override
    public <T> T get( String key )
    {
        return (T) client.get( prefix + key, new SerializingTranscoder() );
    }

    @Override
    public <T> Optional<T> getOptional( String key )
    {
        return Optional.ofNullable( (T) client.get( prefix + key, new SerializingTranscoder() ) );
    }

    @Override
    public <T> T getOrSetDefault( String key, T defaultValue )
    {
        return getOrSetDefault( key, Duration.ofMillis( Long.MAX_VALUE ), defaultValue );
    }

    @Override
    public <T> T getOrSetDefault( String key, Duration ttl, T defaultValue )
    {
        Object value = client.get( prefix + key, new SerializingTranscoder() );
        if( value == null )
        {
            client.set( prefix + key, Numbers.safeIntValueOf( ttl.getSeconds() ), defaultValue, new SerializingTranscoder() );
            return defaultValue;
        }
        return (T) value;
    }

    @Override
    public <T> void set( String key, T value )
    {
        client.set( prefix + key, Integer.MAX_VALUE, value, new SerializingTranscoder() );
    }

    @Override
    public <T> void set( int ttlSeconds, String key, T value )
    {
        client.set( prefix + key, ttlSeconds, value, new SerializingTranscoder() );
    }

    @Override
    public <T> void set( Duration ttl, String key, T value )
    {
        client.set( prefix + key, Numbers.safeIntValueOf( ttl.getSeconds() ), value, new SerializingTranscoder() );
    }

    @Override
    public void remove( String key )
    {
        client.delete( prefix + key );
    }
}
