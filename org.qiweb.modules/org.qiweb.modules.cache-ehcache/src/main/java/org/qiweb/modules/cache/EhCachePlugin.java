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

import java.net.URL;
import net.sf.ehcache.CacheManager;
import org.qiweb.api.Application;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.spi.cache.CachePlugin;

/**
 * EhCache Plugin.
 */
public class EhCachePlugin
    extends CachePlugin
{
    private EhCache ehcache;
    private CacheManager manager;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        String configResourceName = application.config().string( "ehcache.configResource" );
        URL configResourceURL = application.classLoader().getResource( configResourceName );
        CacheManager cacheManager = CacheManager.create( configResourceURL );
        net.sf.ehcache.Cache backingCache = cacheManager.getCache( "qiweb-cache" );
        this.manager = cacheManager;
        this.ehcache = new EhCache( backingCache );
    }

    @Override
    public void onPassivate( Application application )
    {
        if( manager != null )
        {
            manager.shutdown();
            manager = null;
        }
        ehcache = null;
    }

    @Override
    public Cache api()
    {
        return ehcache;
    }
}
