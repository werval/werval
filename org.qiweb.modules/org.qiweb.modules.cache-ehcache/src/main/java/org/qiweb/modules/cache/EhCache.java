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

import net.sf.ehcache.Element;
import org.qiweb.api.cache.Cache;

/**
 * EhCache.
 */
/* package */ class EhCache
    implements Cache
{
    private final net.sf.ehcache.Cache backingCache;

    /* package */ EhCache( net.sf.ehcache.Cache backingCache )
    {
        this.backingCache = backingCache;
    }

    @Override
    public <T> T get( String key )
    {
        Element element = backingCache.get( key );
        if( element == null )
        {
            return null;
        }
        return (T) element.getObjectValue();
    }

    @Override
    public <T> T getOrSetDefault( String key, int ttlSeconds, T defaultValue )
    {
        Element element = backingCache.get( key );
        if( element == null )
        {
            backingCache.put( element( ttlSeconds, key, defaultValue ) );
            return defaultValue;
        }
        return (T) element.getObjectValue();
    }

    @Override
    public <T> void set( int ttlSeconds, String key, T value )
    {
        backingCache.put( element( ttlSeconds, key, value ) );
    }

    @Override
    public void remove( String key )
    {
        backingCache.remove( key );
    }

    private Element element( int ttlSeconds, String key, Object value )
    {
        Element element = new Element( key, value );
        if( ttlSeconds == 0 )
        {
            element.setEternal( true );
        }
        else
        {
            element.setTimeToLive( ttlSeconds );
        }
        return element;
    }
}
