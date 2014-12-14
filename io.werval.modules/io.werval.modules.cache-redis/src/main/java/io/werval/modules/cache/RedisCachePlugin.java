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

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.cache.Cache;
import io.werval.api.cache.CachePlugin;
import io.werval.api.exceptions.ActivationException;
import java.util.Arrays;
import java.util.List;
import io.werval.modules.metrics.Metrics;
import redis.clients.jedis.Jedis;

import static java.util.Collections.EMPTY_LIST;

/**
 * EhCache Plugin.
 */
public class RedisCachePlugin
    extends CachePlugin
{
    private RedisCache redisCache;
    private Jedis jedis;

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "redis.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

    @Override
    public Cache api()
    {
        return redisCache;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        jedis = new Jedis(
            application.config().string( "redis.host" ),
            application.config().intNumber( "redis.port" )
        );
        redisCache = application.config().bool( "redis.metrics" )
                     ? new RedisCache( application.plugin( Metrics.class ), jedis )
                     : new RedisCache( jedis );
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
}
