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
package org.qiweb.spi.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.util.Couple;
import org.qiweb.api.util.Numbers;

/**
 * In-Memory Cache backed by a HashMap.
 *
 * Expiration is applied on access only. This may not suit your usage pattern.
 * Look at the EhCache and Memcache based Cache Plugins for alternatives.
 */
/* package */ class MapCache
    implements Cache
{
    /* package */ final Map<String, Couple<Long, Object>> map = new HashMap<>();

    @Override
    public boolean has( String key )
    {
        return getOrExpires( key ) != null;
    }

    @Override
    public <T> T get( String key )
    {
        return (T) getOrExpires( key );
    }

    @Override
    public <T> T getOrSetDefault( String key, T defaultValue )
    {
        return getOrSetDefault( key, Duration.ofMillis( Long.MAX_VALUE ), defaultValue );
    }

    @Override
    public <T> T getOrSetDefault( String key, Duration ttl, T defaultValue )
    {
        long now = System.currentTimeMillis();
        if( !map.containsKey( key ) )
        {
            long expiration = Numbers.safeLongValueOfSum( now, ttl.toMillis() );
            map.put( key, Couple.of( expiration, (Object) defaultValue ) );
            return defaultValue;
        }
        Couple<Long, Object> entry = map.get( key );
        if( now > entry.left() )
        {
            long expiration = Numbers.safeLongValueOfSum( now, ttl.toMillis() );
            map.put( key, Couple.of( expiration, (Object) defaultValue ) );
            return defaultValue;
        }
        return (T) entry.right();
    }

    @Override
    public <T> Optional<T> getOptional( String key )
    {
        return Optional.ofNullable( (T) getOrExpires( key ) );
    }

    @Override
    public <T> void set( String key, T value )
    {
        map.put( key, Couple.of( Long.MAX_VALUE, (Object) value ) );
    }

    @Override
    public <T> void set( int ttlSeconds, String key, T value )
    {
        long expiration = Numbers.safeLongValueOfSum( System.currentTimeMillis() + ( ttlSeconds * 1000 ) );
        map.put( key, Couple.of( expiration, (Object) value ) );
    }

    @Override
    public <T> void set( Duration ttl, String key, T value )
    {
        long expiration = Numbers.safeLongValueOfSum( System.currentTimeMillis() + ttl.toMillis() );
        map.put( key, Couple.of( expiration, (Object) value ) );
    }

    @Override
    public void remove( String key )
    {
        map.remove( key );
    }

    private Object getOrExpires( String key )
    {
        if( !map.containsKey( key ) )
        {
            return null;
        }
        Couple<Long, Object> entry = map.get( key );
        if( System.currentTimeMillis() > entry.left() )
        {
            map.remove( key );
            return null;
        }
        return entry.right();
    }
}
