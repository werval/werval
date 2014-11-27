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
import org.qiweb.util.Couple;
import org.qiweb.util.Numbers;

/**
 * In-Memory Cache backed by a {@link ConcurrentHashMap}.
 * <p>
 * This is the default Cache Extension used if your Application do not declare any Cache Plugin.
 * <p>
 * Expiration is applied on access only.
 * This may not suit your usage pattern.
 * <p>
 * No metrics gathered.
 * <p>
 * See the the EhCache, Memcache and Redis based Cache Plugins for alternatives.
 */
public class MapCache
    extends CacheAdapter
{
    /* package */ final ConcurrentMap<String, Couple<Long, Object>> map = new ConcurrentHashMap<>();

    public MapCache()
    {
        super();
    }

    @Override
    protected <T> T doGet( String key )
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
    protected <T> void doSet( int ttlSeconds, String key, T value )
    {
        map.put( key, Couple.of( expiration( System.currentTimeMillis(), ttlSeconds ), (Object) value ) );
    }

    @Override
    protected void doRemove( String key )
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
