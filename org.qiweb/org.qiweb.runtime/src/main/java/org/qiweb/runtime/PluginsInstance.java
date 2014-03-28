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
import org.qiweb.api.routes.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_LIST;

/**
 * A Plugins Instance.
 *
 * Manage Plugins lifecycle and provide lookup for {@link ApplicationInstance}.
 */
/* package */ class PluginsInstance
{
    private static final Logger LOG = LoggerFactory.getLogger( PluginsInstance.class );
    private volatile boolean activated = false;
    private final List<String> pluginsFQCNs;
    private final List<Plugin<?>> extraPlugins;
    private List<Plugin<?>> activePlugins = EMPTY_LIST;

    /* package */ PluginsInstance( Config config, List<Plugin<?>> extraPlugins )
    {
        this.pluginsFQCNs = config.stringList( "app.plugins" );
        this.extraPlugins = extraPlugins;
    }

    /* package */ void onActivate( ApplicationInstance application )
    {
        List<Plugin<?>> activatedPlugins = new ArrayList<>( pluginsFQCNs.size() + extraPlugins.size() );
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

    /* package */ List<Route> firstRoutes( RouteBuilder routeBuilder )
    {
        List<Route> firstRoutes = new ArrayList<>();
        for( Plugin plugin : activePlugins )
        {
            firstRoutes.addAll( plugin.firstRoutes( routeBuilder ) );
        }
        return firstRoutes;
    }

    /* package */ List<Route> lastRoutes( RouteBuilder routeBuilder )
    {
        List<Route> lastRoutes = new ArrayList<>();
        for( Plugin plugin : activePlugins )
        {
            lastRoutes.addAll( plugin.lastRoutes( routeBuilder ) );
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
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().equals( pluginApiType ) && plugin.api() != null )
            {
                // Type equals
                result.add( pluginApiType.cast( plugin.api() ) );
            }
        }
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().isAssignableFrom( pluginApiType ) && plugin.api() != null )
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
            if( plugin.apiType().equals( pluginApiType ) && plugin.api() != null )
            {
                // Type equals
                return pluginApiType.cast( plugin.api() );
            }
        }
        for( Plugin<?> plugin : activePlugins )
        {
            if( plugin.apiType().isAssignableFrom( pluginApiType ) && plugin.api() != null )
            {
                // Type is assignable
                return pluginApiType.cast( plugin.api() );
            }
        }
        // No Plugin found
        throw new IllegalArgumentException( "API for Plugin<" + pluginApiType.getName() + "> not found." );
    }
}
