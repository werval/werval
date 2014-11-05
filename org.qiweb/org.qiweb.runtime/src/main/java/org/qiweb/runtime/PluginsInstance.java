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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Global;
import org.qiweb.api.Plugin;
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.exceptions.PassivationException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.runtime.routes.RouteBuilderInstance;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.util.Couple;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;
import static org.qiweb.util.IllegalArguments.ensureNotNull;

/**
 * A Plugins Instance.
 *
 * Manage Plugins lifecycle and provide lookup for {@link ApplicationInstance}.
 */
/* package */ class PluginsInstance
{
    private volatile boolean activated = false;
    private boolean activatingOrPassivating = false;
    private final List<Config> configuredPlugins;
    private final List<Plugin<?>> extraPlugins;

    /**
     * Couple left is the actual plugin instance, right is its Config node.
     */
    private List<Couple<Plugin<?>, Config>> activePlugins = EMPTY_LIST;

    /* package */ PluginsInstance( Config config, List<Plugin<?>> extraPlugins, boolean devMode )
    {
        this.configuredPlugins = config.array( "app.plugins" );
        if( devMode )
        {
            this.configuredPlugins.addAll( 0, config.array( "qiweb.devshell.plugins" ) );
        }
        this.extraPlugins = extraPlugins;
    }

    /* package */ void onActivate( ApplicationInstance application, Global global )
    {
        activatingOrPassivating = true;
        try
        {
            EnumSet<ExtensionPlugin> extensions = EnumSet.allOf( ExtensionPlugin.class );
            List<Couple<Plugin<?>, Config>> plugins = new ArrayList<>(
                configuredPlugins.size() + extraPlugins.size() + extensions.size()
            );

            // Application Configured Plugins
            for( Config pluginConfig : configuredPlugins )
            {
                try
                {
                    Class<?> pluginClass = application.classLoader().loadClass( pluginConfig.string( "plugin" ) );
                    Plugin<?> plugin = (Plugin<?>) global.getPluginInstance( application, pluginClass );
                    plugins.add( Couple.of( plugin, pluginConfig ) );
                    extensions.removeIf( extension -> extension.satisfiedBy( plugin ) );
                }
                catch( ClassNotFoundException ex )
                {
                    throw new ActivationException( "Unable to activate a plugin: " + ex.getMessage(), ex );
                }
            }

            // Global Extra Plugins
            for( Plugin<?> extraPlugin : extraPlugins )
            {
                plugins.add( Couple.leftOnly( extraPlugin ) );
                extensions.removeIf( extension -> extension.satisfiedBy( extraPlugin ) );
            }

            // Core Extensions Plugins
            for( ExtensionPlugin extension : extensions )
            {
                Plugin<?> extensionPlugin = extension.newDefaultPluginInstance();
                plugins.add( Couple.leftOnly( extensionPlugin ) );
            }

            // Load available dynamics plugins
            List<Couple<Plugin<?>, Config>> dynamicPlugins = loadDynamicPlugins(
                application,
                plugins.stream().map( Couple::left ).collect( Collectors.toList() )
            );

            // Resolve dependencies
            plugins = resolveDeps( application.config(), plugins, dynamicPlugins );

            // Activate all plugins, in order
            activePlugins = new ArrayList<>( plugins.size() );
            for( Couple<Plugin<?>, Config> plugin : plugins )
            {
                plugin.left().onActivate( application );
                activePlugins.add( plugin );
            }

            // Plugins Activated
            activated = true;
        }
        catch( Exception ex )
        {
            activePlugins = EMPTY_LIST;
            throw ex;
        }
        finally
        {
            activatingOrPassivating = false;
        }
    }

    private List<Couple<Plugin<?>, Config>> loadDynamicPlugins( ApplicationSPI application, List<Plugin<?>> alreadyLoaded )
    {
        try
        {
            List<Couple<Plugin<?>, Config>> dynamicPlugins = new ArrayList<>();
            Enumeration<URL> descriptors = application.classLoader().getResources( "META-INF/qiweb-plugins.properties" );
            while( descriptors.hasMoreElements() )
            {
                try( InputStream stream = descriptors.nextElement().openStream() )
                {
                    Properties descriptor = new Properties();
                    descriptor.load( stream );
                    for( String name : descriptor.stringPropertyNames() )
                    {
                        String fqcn = descriptor.getProperty( name );
                        Class<?> clazz = application.classLoader().loadClass( fqcn );
                        if( !alreadyLoaded.stream().anyMatch( p -> p.getClass().equals( clazz ) ) )
                        {
                            Plugin<?> plugin = (Plugin<?>) application.global().getPluginInstance( application, clazz );
                            dynamicPlugins.add( Couple.leftOnly( plugin ) );
                        }
                    }
                }
            }
            return dynamicPlugins;
        }
        catch( IOException | ClassNotFoundException ex )
        {
            throw new QiWebRuntimeException( "Unable to load dynamic plugins", ex );
        }
    }

    private List<Couple<Plugin<?>, Config>> resolveDeps(
        Config appConfig,
        List<Couple<Plugin<?>, Config>> input,
        List<Couple<Plugin<?>, Config>> dynamics
    )
    {
        List<Couple<Plugin<?>, Config>> output = new ArrayList<>( input.size() );
        ArrayDeque<Couple<Plugin<?>, Config>> queue = new ArrayDeque<>( input );
        boolean replay = false;
        while( !queue.isEmpty() )
        {
            Couple<Plugin<?>, Config> plugin = queue.poll();
            for( Class<?> dependency : plugin.left().dependencies( appConfig ) )
            {
                // Already resolved?
                if( !output.stream().anyMatch(
                    p -> p.left().apiType().equals( dependency ) || dependency.isAssignableFrom( p.left().apiType() )
                ) )
                {
                    // Same type, then assignable or null
                    Couple<Plugin<?>, Config> match = input.stream()
                        .filter( p -> p.left().apiType().equals( dependency ) )
                        .findFirst()
                        .orElse(
                            input.stream()
                            .filter( p -> dependency.isAssignableFrom( p.left().apiType() ) )
                            .findFirst()
                            .orElse( null )
                        );
                    if( match == null )
                    {
                        // Dynamic plugins, same type, then assignable or null
                        match = dynamics.stream()
                            .filter( p -> p.left().apiType().equals( dependency ) )
                            .findFirst()
                            .orElse(
                                input.stream()
                                .filter( p -> dependency.isAssignableFrom( p.left().apiType() ) )
                                .findFirst()
                                .orElse( null )
                            );
                        // If no match, throw
                        if( match == null )
                        {
                            throw new QiWebException( "Plugin dependency not resolved: " + dependency );
                        }
                        // Replay will be needed to properly order plugins according to dependencies
                        replay = true;
                        queue.addFirst( match );
                        output.add( match );
                    }
                    else
                    {
                        queue.remove( match );
                        output.add( match );
                    }
                }
            }
            if( !output.contains( plugin ) )
            {
                output.add( plugin );
            }
        }
        if( replay )
        {
            return resolveDeps( appConfig, output, EMPTY_LIST );
        }
        return output;
    }

    /* package */ void onPassivate( Application application )
    {
        activatingOrPassivating = true;
        try
        {
            Collections.reverse( activePlugins );
            Iterator<Couple<Plugin<?>, Config>> it = activePlugins.iterator();
            List<Exception> errors = new ArrayList<>();
            while( it.hasNext() )
            {
                try
                {
                    it.next().left().onPassivate( application );
                }
                catch( Exception ex )
                {
                    errors.add( ex );
                }
                finally
                {
                    it.remove();
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
        finally
        {
            activatingOrPassivating = false;
        }
    }

    /* package */ List<Route> firstRoutes( Application app )
    {
        List<Route> firstRoutes = new ArrayList<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            String routesPrefix = plugin.left().routesPrefix( plugin.right() );
            firstRoutes.addAll( plugin.left().firstRoutes( app.mode(), new RouteBuilderInstance( app, routesPrefix ) ) );
        }
        return firstRoutes;
    }

    /* package */ List<Route> lastRoutes( Application app )
    {
        List<Route> lastRoutes = new ArrayList<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            String routesPrefix = plugin.left().routesPrefix( plugin.right() );
            lastRoutes.addAll( plugin.left().lastRoutes( app.mode(), new RouteBuilderInstance( app, routesPrefix ) ) );
        }
        return lastRoutes;
    }

    /* package */ void beforeInteraction( Context context )
    {
        // Fail fast
        activePlugins.forEach( cp -> cp.left().beforeInteraction( context ) );
    }

    /* package */ void afterInteraction( Context context )
    {
        // Fail safe
        List<Exception> errors = new ArrayList<>();
        for( Couple<Plugin<?>, Config> plugin : activePlugins )
        {
            try
            {
                plugin.left().afterInteraction( context );
            }
            catch( Exception ex )
            {
                errors.add( ex );
            }
        }
        if( !errors.isEmpty() )
        {
            QiWebException ex = new QiWebException( "There were errors during Plugins after interaction hooks" );
            for( Exception err : errors )
            {
                ex.addSuppressed( err );
            }
            throw ex;
        }
    }

    /* package */ <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        if( !activated && !activatingOrPassivating )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        ensureNotNull( "Plugin API Type", pluginApiType );
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
            if( pluginApiType.isAssignableFrom( plugin.left().apiType() ) && plugin.left().api() != null )
            {
                // Type is assignable
                result.add( pluginApiType.cast( plugin.left().api() ) );
            }
        }
        return result;
    }

    /* package */ <T> T plugin( Class<T> pluginApiType )
    {
        if( !activated && !activatingOrPassivating )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        ensureNotNull( "Plugin API Type", pluginApiType );
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
            if( pluginApiType.isAssignableFrom( plugin.left().apiType() ) && plugin.left().api() != null )
            {
                // Type is assignable
                return pluginApiType.cast( plugin.left().api() );
            }
        }
        // No Plugin found
        throw new IllegalArgumentException(
            "API for Plugin<" + pluginApiType.getName() + "> not found. "
            + "Active plugins APIs: " + activePlugins.stream().map( c -> c.left().apiType() ).collect( toList() ) + "."
        );
    }
}
