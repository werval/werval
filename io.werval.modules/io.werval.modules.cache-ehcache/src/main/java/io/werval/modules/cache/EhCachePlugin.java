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

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.cache.Cache;
import io.werval.api.cache.CachePlugin;
import io.werval.api.exceptions.ActivationException;
import io.werval.modules.metrics.Metrics;

import net.sf.ehcache.CacheManager;

import static java.util.Collections.EMPTY_LIST;

/**
 * EhCache Plugin.
 */
public class EhCachePlugin
    extends CachePlugin
{
    private EhCache ehcache;
    private CacheManager manager;

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "ehcache.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

    @Override
    public Cache api()
    {
        return ehcache;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config().atKey( "ehcache" );
        String configResourceName = config.string( "configResource" );
        URL configResourceURL = application.classLoader().getResource( configResourceName );
        CacheManager cacheManager = CacheManager.create( configResourceURL );
        net.sf.ehcache.Ehcache backingCache = cacheManager.getCache( "werval-cache" );
        this.manager = cacheManager;
        this.ehcache = config.bool( "metrics" )
                       ? new EhCache( application.plugin( Metrics.class ), backingCache )
                       : new EhCache( backingCache );
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
}
