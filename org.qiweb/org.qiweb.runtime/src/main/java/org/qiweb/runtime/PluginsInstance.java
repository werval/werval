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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.exceptions.PassivationException;
import org.qiweb.api.routes.Route;
import org.qiweb.api.util.Couple;
import org.qiweb.runtime.routes.RouteBuilderInstance;

import static java.util.Collections.EMPTY_LIST;

/**
 * A Plugins Instance.
 *
 * Manage Plugins lifecycle and provide lookup for {@link ApplicationInstance}.
 */
/* package */ class PluginsInstance
{
    private volatile boolean activated = false;
    private final List<Config> configuredPlugins;
    private final List<Plugin<?>> extraPlugins;
    private List<Couple<Plugin<?>, Config>> activePlugins = EMPTY_LIST;

    /* package */ PluginsInstance( Config config, List<Plugin<?>> extraPlugins )
    {
        this.configuredPlugins = config.array( "app.plugins" );
        this.extraPlugins = extraPlugins;
    }

    /* package */ void onActivate( ApplicationInstance application )
    {
        List<Couple<Plugin<?>, Config>> activatedPlugins = new ArrayList<>(
            configuredPlugins.size() + extraPlugins.size()
        );
        for( Config pluginConfig : configuredPlugins )
        {
            try
            {
                Class<?> pluginClass = application.classLoader().loadClass( pluginConfig.string( "plugin" ) );
                Plugin<?> plugin = (Plugin<?>) application.global().getPluginInstance( application, pluginClass );
                if( plugin.enabled() )
                {
                    plugin.onActivate( application );
                    activatedPlugins.add( Couple.of( plugin, pluginConfig ) );
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
                activatedPlugins.add( Couple.leftOnly( extraPlugin ) );
            }
        }
        activePlugins = activatedPlugins;
        activated = true;
    }

    /* package */ void onPassivate( Application application )
    {
        List<Exception> errors = new ArrayList<>();
        for( Couple<Plugin<?>, Config> activePlugin : activePlugins )
        {
            try
            {
                activePlugin.left().onPassivate( application );
            }
            catch( Exception ex )
            {
                errors.add( ex );
            }
        }
        activePlugins = EMPTY_LIST;
        activated = false;
        if( !errors.isEmpty() )
        {
            PassivationException ex = new PassivationException( "There were errors during Plugins passivation" );
            for( Exception err : errors )
            {
                ex.addSuppressed( err );
            }
            throw ex;
        }
    }

    /* package */ List<Route> firstRoutes( Application app )
    {
        List<Route> firstRoutes = new ArrayList<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            String routesPrefix = plugin.left().routesPrefix( plugin.right() );
            firstRoutes.addAll( plugin.left().firstRoutes( new RouteBuilderInstance( app, routesPrefix ) ) );
        }
        return firstRoutes;
    }

    /* package */ List<Route> lastRoutes( Application app )
    {
        List<Route> lastRoutes = new ArrayList<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            String routesPrefix = plugin.left().routesPrefix( plugin.right() );
            lastRoutes.addAll( plugin.left().lastRoutes( new RouteBuilderInstance( app, routesPrefix ) ) );
        }
        return lastRoutes;
    }

    /* package */ <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        if( !activated )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        Set<T> result = new LinkedHashSet<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            if( plugin.left().apiType().equals( pluginApiType ) && plugin.left().api() != null )
            {
                // Type equals
                result.add( pluginApiType.cast( plugin.left().api() ) );
            }
        }
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            if( plugin.left().apiType().isAssignableFrom( pluginApiType ) && plugin.left().api() != null )
            {
                // Type is assignable
                result.add( pluginApiType.cast( plugin.left().api() ) );
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
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            if( plugin.left().apiType().equals( pluginApiType ) && plugin.left().api() != null )
            {
                // Type equals
                return pluginApiType.cast( plugin.left().api() );
            }
        }
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            if( plugin.left().apiType().isAssignableFrom( pluginApiType ) && plugin.left().api() != null )
            {
                // Type is assignable
                return pluginApiType.cast( plugin.left().api() );
            }
        }
        // No Plugin found
        throw new IllegalArgumentException( "API for Plugin<" + pluginApiType.getName() + "> not found." );
    }
}
