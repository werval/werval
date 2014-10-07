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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import org.qiweb.api.cache.Cache;
import org.qiweb.util.Couple;
import org.qiweb.util.Numbers;

/**
 * In-Memory Cache backed by a {@link ConcurrentHashMap}.
 *
 * This is the default Cache Extension used if your Application do not declare any Cache Plugin.
 * <p>
 * Expiration is applied on access only.
 * This may not suit your usage pattern.
 * <p>
 * See the the EhCache and Memcache based Cache Plugins for alternatives.
 */
/* package */ class MapCache
    implements Cache
{
    /* package */ final ConcurrentMap<String, Couple<Long, Object>> map = new ConcurrentHashMap<>();

    @Override
    public <T> T get( String key )
    {
        Couple<Long, Object> result = map.computeIfPresent(
            key,
            new BiFunction<String, Couple<Long, Object>, Couple<Long, Object>>()
            {
                @Override
                public Couple<Long, Object> apply( String k, Couple<Long, Object> entry )
                {
                    if( System.currentTimeMillis() > entry.left() )
                    {
                        return null;
                    }
                    return entry;
                }
            }
        );
        if( result != null )
        {
            return (T) result.right();
        }
        return null;
    }

    @Override
    public <T> T getOrSetDefault( String key, final int ttlSeconds, final T defaultValue )
    {
        Couple<Long, Object> result = map.compute(
            key,
            new BiFunction<String, Couple<Long, Object>, Couple<Long, Object>>()
            {
                @Override
                public Couple<Long, Object> apply( String k, Couple<Long, Object> entry )
                {
                    long now = System.currentTimeMillis();
                    if( entry == null || now > entry.left() )
                    {
                        return Couple.of( expiration( now, ttlSeconds ), (Object) defaultValue );
                    }
                    return entry;
                }
            }
        );
        if( result != null )
        {
            return (T) result.right();
        }
        return null;
    }

    @Override
    public <T> void set( int ttlSeconds, String key, T value )
    {
        map.put( key, Couple.of( expiration( System.currentTimeMillis(), ttlSeconds ), (Object) value ) );
    }

    @Override
    public void remove( String key )
    {
        map.remove( key );
    }

    private long expiration( long now, int ttlSeconds )
    {
        if( ttlSeconds == 0 )
        {
            return Long.MAX_VALUE;
        }
        return Numbers.safeLongValueOfSum( now, Numbers.safeLongValueOfMultiply( ttlSeconds, 1000 ) );
    }
}
