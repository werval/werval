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
import java.util.Objects;
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
    /**
     * Helper to preserve config location and overrides between reloads.
     * <p>
     * See {@link ConfigInstance#location()}.
     */
    public static final class ConfigLocation
    {
        private final String configResource;
        private final File configFile;
        private final URL configUrl;
        private final Map<String, Object> overrides;

        private ConfigLocation()
        {
            this( null, null, null, null );
        }

        private ConfigLocation( String configResource )
        {
            this( configResource, null, null, null );
        }

        private ConfigLocation( File configFile )
        {
            this( null, configFile, null, null );
        }

        private ConfigLocation( URL configUrl )
        {
            this( null, null, configUrl, null );
        }

        private ConfigLocation( String configResource, File configFile, URL configUrl, Map<String, Object> overrides )
        {
            this.configResource = configResource;
            this.configFile = configFile;
            this.configUrl = configUrl;
            this.overrides = overrides;
        }

        public String toStringShort()
        {
            StringBuilder sb = new StringBuilder();
            boolean previous = false;
            if( configResource != null )
            {
                sb.append( "resource:" ).append( configResource );
                previous = true;
            }
            if( configFile != null )
            {
                if( previous )
                {
                    sb.append( ", " );
                }
                sb.append( "file:" ).append( configFile.getAbsolutePath() );
                previous = true;
            }
            if( configUrl != null )
            {
                if( previous )
                {
                    sb.append( ", " );
                }
                sb.append( "url:" ).append( configUrl.toString() );
                previous = true;
            }
            if( overrides != null )
            {
                if( previous )
                {
                    sb.append( ", " );
                }
                sb.append( "overrides:" ).append( overrides.keySet().toString() );
            }
            return sb.toString();
        }

        @Override
        public String toString()
        {
            return "ConfigLocation{" + toStringShort() + '}';
        }
    }

    private final ConfigLocation location;
    private com.typesafe.config.Config config;

    /**
     * Create a new Config instance.
     *
     * @param loader ClassLoader
     */
    public ConfigInstance( ClassLoader loader )
    {
        this( loader, new ConfigLocation() );
    }

    /**
     * Create a new Config instance.
     *
     * @param loader         ClassLoader
     * @param configResource Configuration resource name
     */
    public ConfigInstance( ClassLoader loader, String configResource )
    {
        this( loader, new ConfigLocation( configResource ) );
    }

    /**
     * Create a new Config instance.
     *
     * @param loader     ClassLoader
     * @param configFile Configuration file
     */
    public ConfigInstance( ClassLoader loader, File configFile )
    {
        this( loader, new ConfigLocation( configFile ) );
    }

    /**
     * Create a new Config instance.
     *
     * @param loader    ClassLoader
     * @param configUrl Configuration URL
     */
    public ConfigInstance( ClassLoader loader, URL configUrl )
    {
        this( loader, new ConfigLocation( configUrl ) );
    }

    /**
     * Create a new Config instance.
     * <p>
     * Only one of {@literal configResource}, {@literal configFile} or {@literal configUrl} should be non-null.
     *
     * @param loader         ClassLoader
     * @param configResource Configuration resource name
     * @param configFile     Configuration file
     * @param configUrl      Configuration URL
     * @param overrides      Configuration properties overrides, may be null
     */
    @Reflectively.Invoked( by = "DevShell" )
    public ConfigInstance(
        ClassLoader loader,
        String configResource,
        File configFile,
        URL configUrl,
        Map<String, Object> overrides
    )
    {
        this( loader, new ConfigLocation( configResource, configFile, configUrl, overrides ) );
    }

    public ConfigInstance( ClassLoader loader, ConfigLocation location )
    {
        // Gather eventually set previous system properties for config location
        String previousConfigResource = System.getProperty( "config.resource" );
        String previousConfigFile = System.getProperty( "config.file" );
        String previousConfigURL = System.getProperty( "config.url" );
        Map<String, String> overridesPrevious = new HashMap<>();
        try
        {
            // Eventually set config.resource
            if( location.configResource != null )
            {
                System.setProperty( "config.resource", location.configResource );
            }
            // Eventually set config.file
            if( location.configFile != null )
            {
                System.setProperty( "config.file", location.configFile.getAbsolutePath() );
            }
            // Eventually set config.url
            if( location.configUrl != null )
            {
                System.setProperty( "config.url", location.configUrl.toExternalForm() );
            }
            // Eventually set configuration properties overrides
            if( location.overrides != null )
            {
                location.overrides.forEach(
                    (key, val) ->
                    {
                        overridesPrevious.put( key, System.getProperty( key ) );
                        System.setProperty( key, Objects.toString( val ) );
                    }
                );
            }
            // Hold a reference to the location, for reload purpose
            this.location = location;
            // Invalidate TypeSafe Config caches (system properties)
            com.typesafe.config.ConfigFactory.invalidateCaches();
            // Effectively load configuration
            this.config = com.typesafe.config.ConfigFactory.load( loader );
        }
        finally
        {
            // Restore config.resource
            if( previousConfigResource == null )
            {
                System.clearProperty( "config.resource" );
            }
            else
            {
                System.setProperty( "config.resource", previousConfigResource );
            }
            // Restore config.file
            if( previousConfigFile == null )
            {
                System.clearProperty( "config.file" );
            }
            else
            {
                System.setProperty( "config.file", previousConfigFile );
            }
            // Restore config.url
            if( previousConfigURL == null )
            {
                System.clearProperty( "config.url" );
            }
            else
            {
                System.setProperty( "config.url", previousConfigURL );
            }
            // Restore overrides
            overridesPrevious.forEach(
                (key, val) ->
                {
                    if( val == null )
                    {
                        System.clearProperty( key );
                    }
                    else
                    {
                        System.setProperty( key, val );
                    }
                }
            );
        }
    }

    /**
     * Internal CTOR.
     * <p>
     * See {@link #location()}, {@link #object(java.lang.String)} and {@link #array(java.lang.String)}.
     *
     * @param config   TypeSafe Config
     * @param location Config location and overrides
     */
    private ConfigInstance( com.typesafe.config.Config config, ConfigLocation location )
    {
        this.location = location;
        this.config = config;
    }

    /**
     * Config Location Helper.
     * <p>
     * Used internally by {@link ApplicationInstance} to preserve config location and overrides between reloads.
     * See {@link #ConfigInstance(com.typesafe.config.Config, org.qiweb.runtime.ConfigInstance.ConfigLocation)}.
     *
     * @return Config Location Helper
     */
    /* package */ ConfigLocation location()
    {
        return location;
    }

    @Override
    public Config object( String key )
    {
        return new ConfigInstance( config.getConfig( key ), location );
    }

    @Override
    public List<Config> array( String key )
    {
        List<Config> configs = new ArrayList<>();
        config.getConfigList( key ).forEach( conf -> configs.add( new ConfigInstance( conf, location ) ) );
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
