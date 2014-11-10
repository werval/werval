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
import org.qiweb.modules.metrics.Metrics;
import org.qiweb.modules.metrics.internal.CacheMetricsListener;
import org.qiweb.spi.cache.CacheAdapter;

/**
 * EhCache.
 */
/* package */ class EhCache
    extends CacheAdapter
{
    private final net.sf.ehcache.Ehcache backingCache;

    /* package */ EhCache( net.sf.ehcache.Ehcache backingCache )
    {
        super();
        this.backingCache = backingCache;
    }

    /* package */ EhCache( Metrics metrics, net.sf.ehcache.Ehcache backingCache )
    {
        super( new CacheMetricsListener( metrics.metrics(), "ehcache", backingCache.getName() ) );
        this.backingCache = backingCache;
    }

    @Override
    protected <T> T doGet( String key )
    {
        Element element = backingCache.get( key );
        if( element == null )
        {
            return null;
        }
        return (T) element.getObjectValue();
    }

    @Override
    protected <T> void doSet( int ttlSeconds, String key, T value )
    {
        backingCache.put( element( ttlSeconds, key, value ) );
    }

    @Override
    protected void doRemove( String key )
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
