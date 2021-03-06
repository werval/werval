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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.cache.Cache;
import io.werval.api.cache.CachePlugin;
import io.werval.api.exceptions.ActivationException;
import io.werval.modules.metrics.Metrics;
import io.werval.util.Strings;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

import static java.util.Collections.EMPTY_LIST;

/**
 * Memcache Plugin.
 */
public class MemcachePlugin
    extends CachePlugin
{
    private MemcachedClient client;
    private MemcacheCache backingCache;

    public MemcachePlugin()
    {
        // Enable SLF4J Logging
        Properties systemProperties = System.getProperties();
        systemProperties.put( "net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger" );
        System.setProperties( systemProperties );
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "memcache.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

    @Override
    public Cache api()
    {
        return backingCache;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        // Gather Configuration
        Config config = application.config().atKey( "memcache" );
        Protocol protocol = Protocol.valueOf( config.string( "protocol" ).toUpperCase( Locale.US ) );
        String addresses = config.string( "addresses" );
        String username = config.has( "username" ) ? config.string( "username" ) : null;
        String password = config.has( "password" ) ? config.string( "password" ) : null;
        String authMech = config.string( "authMechanism" );

        // Create Client
        try
        {
            ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
            builder.setProtocol( protocol );
            if( !Strings.isEmpty( username ) )
            {
                builder.setAuthDescriptor(
                    new AuthDescriptor(
                        new String[]
                        {
                            authMech
                        },
                        new PlainCallbackHandler( username, password )
                    )
                );
            }
            client = new MemcachedClient( builder.build(), AddrUtil.getAddresses( addresses ) );
        }
        catch( IOException ex )
        {
            throw new ActivationException( "Unable to Activate MemcachePlugin: " + ex.getMessage(), ex );
        }

        // Create Cache Instance
        backingCache = config.bool( "metrics" )
                       ? new MemcacheCache( application.plugin( Metrics.class ), client )
                       : new MemcacheCache( client );
    }

    @Override
    public void onPassivate( Application application )
    {
        if( client != null )
        {
            client.shutdown();
            client = null;
        }
        backingCache = null;
    }
}
