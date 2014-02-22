/**
 * Copyright (c) 2013 the original author or authors
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.exceptions.PassivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Plugins Instance.
 * <p>Manage Plugins lifecycle and provide lookup for {@link ApplicationInstance}.</p>
 */
/* package */ class PluginsInstance
{
    private static final Logger LOG = LoggerFactory.getLogger( PluginsInstance.class );
    private volatile boolean activated = false;
    private final List<String> pluginsFQCNs;
    private final Iterable<Plugin<?>> extraPlugins;
    private Set<Plugin<?>> activePlugins = Collections.emptySet();

    /* package */ PluginsInstance( Config config, Iterable<Plugin<?>> extraPlugins )
    {
        this.pluginsFQCNs = config.stringList( "app.plugins" );
        this.extraPlugins = extraPlugins;
    }

    /* package */ void onActivate( ApplicationInstance application )
    {
        Set<Plugin<?>> activatedPlugins = new LinkedHashSet<>( pluginsFQCNs.size() );
        for( String pluginFQCN : pluginsFQCNs )
        {
            try
            {
                Class<?> pluginClass = application.classLoader().loadClass( pluginFQCN );
                Plugin<?> plugin = (Plugin<?>) application.global().getPluginInstance( application, pluginClass );
                if( plugin.enabled() )
                {
                    plugin.onActivate( application );
                    activatedPlugins.add( plugin );
                }
            }
            catch( ClassNotFoundException ex )
            {
                throw new ActivationException( "Unable to activate a plugin: " + ex.getMessage(), ex );
            }
        }
        for( Plugin<?> extraPlugin : extraPlugins )
        {
            if( extraPlugin.enabled() )
            {
                extraPlugin.onActivate( application );
                activatedPlugins.add( extraPlugin );
            }
        }
        activePlugins = activatedPlugins;
        activated = true;
    }


    /* package */ void onPassivate( Application application )
    {
        List<Exception> errors = new ArrayList<>();
        for( Plugin<?> activePlugin : activePlugins )
        {
            try
            {
                activePlugin.onPassivate( application );
            }
            catch( Exception ex )
            {
                errors.add( ex );
            }
        }
        activePlugins = Collections.emptySet();
        activated = false;
        if( !errors.isEmpty() )
        {
            PassivationException ex = new PassivationException( "There were errors during Plugins passivation" );
            for( Exception err : errors )
            {
                ex.addSuppressed( err );
            }
            LOG.error( ex.getMessage(), ex );
        }
    }

    /* package */ <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        if( !activated )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        Set<T> result = new LinkedHashSet<>();
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().equals( pluginApiType ) )
            {
                // Type equals
                result.add( pluginApiType.cast( plugin.api() ) );
            }
        }
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().isAssignableFrom( pluginApiType ) )
            {
                // Type is assignable
                result.add( pluginApiType.cast( plugin.api() ) );
            }
        }
        return result;
    }

    /* package */ <T> T plugin( Class<T> pluginApiType )
    {
        if( !activated )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().equals( pluginApiType ) )
            {
                // Type equals
                return pluginApiType.cast( plugin.api() );
            }
        }
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().isAssignableFrom( pluginApiType ) )
            {
                // Type is assignable
                return pluginApiType.cast( plugin.api() );
            }
        }
        // No Plugin found
        throw new IllegalArgumentException( "Plugin<" + pluginApiType.getName() + "> not found." );
    }
}
