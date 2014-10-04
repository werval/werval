/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.qiweb.api.Config;
import org.qiweb.util.Reflectively;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.qiweb.util.Charsets.UTF_8;

/**
 * Config Instance backed by TypeSafe Config.
 */
@Reflectively.Loaded( by = "DevShell" )
public class ConfigInstance
    implements Config
{
    private com.typesafe.config.Config config;

    public ConfigInstance()
    {
        this( ConfigInstance.class.getClassLoader() );
    }

    @Reflectively.Invoked( by = "DevShell" )
    public ConfigInstance( ClassLoader loader )
    {
        config = com.typesafe.config.ConfigFactory.load( loader );
    }

    public ConfigInstance( ClassLoader loader, String configurationResourceName )
    {
        config = com.typesafe.config.ConfigFactory.load( loader, configurationResourceName );
    }

    private ConfigInstance( com.typesafe.config.Config config )
    {
        this.config = config;
    }

    @Override
    public Config object( String key )
    {
        return new ConfigInstance( config.getConfig( key ) );
    }

    @Override
    public List<Config> array( String key )
    {
        List<Config> configs = new ArrayList<>();
        config.getConfigList( key ).forEach( conf -> configs.add( new ConfigInstance( conf ) ) );
        return configs;
    }

    @Override
    public Set<String> subKeys()
    {
        return config.root().keySet();
    }

    @Override
    public boolean has( String key )
    {
        return config.hasPath( key );
    }

    @Override
    public Boolean bool( String key )
    {
        return config.getBoolean( key );
    }

    @Override
    public Integer intNumber( String key )
    {
        return config.getInt( key );
    }

    @Override
    public Double doubleNumber( String key )
    {
        return config.getDouble( key );
    }

    @Override
    public String string( String key )
    {
        return config.getString( key );
    }

    @Override
    public List<String> stringList( String key )
    {
        return config.getStringList( key );
    }

    @Override
    public Map<String, String> stringMap( String key )
    {
        Set<Entry<String, com.typesafe.config.ConfigValue>> entrySet = config.getObject( key ).entrySet();
        Map<String, String> entries = new HashMap<>( entrySet.size() );
        for( Entry<String, com.typesafe.config.ConfigValue> entry : entrySet )
        {
            entries.put( entry.getKey(), entry.getValue().unwrapped().toString() );
        }
        return entries;
    }

    @Override
    public char[] chars( String key )
    {
        return config.getString( key ).toCharArray();
    }

    @Override
    public byte[] utf8Bytes( String key )
    {
        return config.getString( key ).getBytes( UTF_8 );
    }

    @Override
    public Charset charset( String key )
    {
        return Charset.forName( config.getString( key ) );
    }

    @Override
    public URL url( String key )
    {
        String urlString = config.getString( key );
        try
        {
            return new URL( urlString );
        }
        catch( MalformedURLException ex )
        {
            throw new com.typesafe.config.ConfigException.WrongType(
                config.origin(),
                "Malformed URL in config, key: " + key + ", value: " + urlString,
                ex
            );
        }
    }

    @Override
    public File file( String key )
    {
        return new File( config.getString( key ) );
    }

    @Override
    public Long seconds( String key )
    {
        return config.getDuration( key, SECONDS );
    }

    @Override
    public Long milliseconds( String key )
    {
        return config.getDuration( key, MILLISECONDS );
    }

    @Override
    public String toString()
    {
        return config.root().render( com.typesafe.config.ConfigRenderOptions.concise() );
    }

    public final void refresh()
    {
        config = com.typesafe.config.ConfigFactory.load();
    }
}
