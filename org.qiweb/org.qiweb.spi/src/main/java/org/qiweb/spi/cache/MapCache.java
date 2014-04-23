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

import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.util.Couple;
import org.qiweb.api.util.Numbers;

/**
 * In-Memory Cache backed by a HashMap.
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
    /* package */ final Map<String, Couple<Long, Object>> map = new HashMap<>();

    @Override
    public <T> T get( String key )
    {
        return (T) getOrExpires( key );
    }

    @Override
    public <T> T getOrSetDefault( String key, int ttlSeconds, T defaultValue )
    {
        long now = System.currentTimeMillis();
        if( !map.containsKey( key ) )
        {
            map.put( key, Couple.of( expiration( now, ttlSeconds ), (Object) defaultValue ) );
            return defaultValue;
        }
        Couple<Long, Object> entry = map.get( key );
        if( now > entry.left() )
        {
            map.put( key, Couple.of( expiration( now, ttlSeconds ), (Object) defaultValue ) );
            return defaultValue;
        }
        return (T) entry.right();
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
