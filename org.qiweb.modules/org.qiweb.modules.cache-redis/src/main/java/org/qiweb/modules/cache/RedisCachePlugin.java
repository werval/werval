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

import org.qiweb.api.Application;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.spi.cache.CachePlugin;
import redis.clients.jedis.Jedis;

/**
 * EhCache Plugin.
 */
public class RedisCachePlugin
    extends CachePlugin
{
    private RedisCache redisCache;
    private Jedis jedis;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        jedis = new Jedis(
            application.config().string( "redis.host" ),
            application.config().intNumber( "redis.port" )
        );
        redisCache = new RedisCache( jedis );
    }

    @Override
    public void onPassivate( Application application )
    {
        if( jedis != null )
        {
            jedis.close();
            jedis = null;
        }
        redisCache = null;
    }

    @Override
    public Cache api()
    {
        return redisCache;
    }
}
