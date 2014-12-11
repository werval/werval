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

import io.werval.api.Application;
import io.werval.api.cache.Cache;
import io.werval.api.cache.CachePlugin;
import io.werval.api.exceptions.ActivationException;

/**
 * Cache Plugin backed by a HashMap.
 *
 * @navcomposed 1 - 1 MapCache
 */
public class MapCachePlugin
    extends CachePlugin
{
    private MapCache cache;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        cache = new MapCache();
    }

    @Override
    public void onPassivate( Application application )
    {
        cache.map.clear();
        cache = null;
    }

    @Override
    public Cache api()
    {
        return cache;
    }
}
