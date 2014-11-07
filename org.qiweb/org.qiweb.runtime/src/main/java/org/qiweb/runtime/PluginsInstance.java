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
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.qiweb.api.Application;
import org.qiweb.api.Mode;
import org.qiweb.api.Plugin;
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.PassivationException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.routes.RouteBuilderInstance;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;
import static org.qiweb.util.IllegalArguments.ensureNotNull;
import static org.qiweb.util.Strings.EMPTY;
import static org.qiweb.util.Strings.NEWLINE;
import static org.qiweb.util.Strings.SPACE;
import static org.qiweb.util.Strings.rightPad;

/**
 * Plugins Instance.
 * <p>
 * Manage Plugins lifecycle and provide lookup for {@link ApplicationInstance}.
 */
/* package */ class PluginsInstance
{
    /**
     * Plugin Information.
     * <p>
     * For internal use only.
     * Ties a Plugin to its runtime configuration properties.
     */
    private static final class PluginInfo
    {
        private final Plugin<?> plugin;
        private final String routesPrefix;

        private PluginInfo( Plugin<?> plugin, String routesPrefix )
        {
            this.plugin = plugin;
            this.routesPrefix = routesPrefix == null ? EMPTY : routesPrefix;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 17 * hash + Objects.hashCode( this.plugin );
            hash = 17 * hash + Objects.hashCode( this.routesPrefix );
            return hash;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( obj == null )
            {
                return false;
            }
            if( getClass() != obj.getClass() )
            {
                return false;
            }
            final PluginInfo other = (PluginInfo) obj;
            if( !Objects.equals( this.plugin, other.plugin ) )
            {
                return false;
            }
            return Objects.equals( this.routesPrefix, other.routesPrefix );
        }

        @Override
        public String toString()
        {
            return plugin.getClass().getName();
        }
    }

    private volatile boolean activated = false;
    private boolean activatingOrPassivating = false;
    private List<PluginInfo> activePlugins = EMPTY_LIST;

    /**
     * Activate Plugins.
     *
     * @param application Application
     */
    /* package */ void onActivate( ApplicationInstance application )
    {
        activatingOrPassivating = true;
        try
        {
            LinkedHashMap<String, String> pluginsDescriptors = loadPluginsDescriptors( application );
            List<PluginInfo> appPlugins = loadApplicationPlugins( application, pluginsDescriptors );
            List<PluginInfo> dynamicPlugins = loadDynamicPlugins( application, pluginsDescriptors, appPlugins );
            List<PluginInfo> plugins = resolveDependencies( application, appPlugins, dynamicPlugins );

            System.out.println( "PLUGINS DESCRIPTORS: " + pluginsDescriptors );
            System.out.println( "APPLICATION PLUGINS: " + appPlugins );
            System.out.println( "DYNAMIC PLUGINS: " + dynamicPlugins );
            System.out.println( "RESOLVED PLUGINS: " + plugins );

            // Activate all plugins, in order
            activePlugins = new ArrayList<>( plugins.size() );
            for( PluginInfo pluginInfo : plugins )
            {
                pluginInfo.plugin.onActivate( application );
                activePlugins.add( pluginInfo );
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

    /**
     * Passivate Plugins.
     *
     * @param application Application
     */
    /* package */ void onPassivate( ApplicationInstance application )
    {
        activatingOrPassivating = true;
        try
        {
            Collections.reverse( activePlugins );
            Iterator<PluginInfo> it = activePlugins.iterator();
            List<Exception> errors = new ArrayList<>();
            while( it.hasNext() )
            {
                try
                {
                    it.next().plugin.onPassivate( application );
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

    /**
     * Collect first routes contributed by active Plugins.
     *
     * @param app Application
     *
     * @return First routes contributed by active Plugins
     */
    /* package */ List<Route> firstRoutes( Application app )
    {
        List<Route> firstRoutes = new ArrayList<>();
        for( PluginInfo pluginInfo : activePlugins )
        {
            firstRoutes.addAll(
                pluginInfo.plugin.firstRoutes( app.mode(), new RouteBuilderInstance( app, pluginInfo.routesPrefix ) )
            );
        }
        return firstRoutes;
    }

    /**
     * Collect last routes contributed by active Plugins.
     *
     * @param app Application
     *
     * @return Last routes contributed by active Plugins
     */
    /* package */ List<Route> lastRoutes( Application app )
    {
        List<Route> lastRoutes = new ArrayList<>();
        for( PluginInfo pluginInfo : activePlugins )
        {
            lastRoutes.addAll(
                pluginInfo.plugin.lastRoutes( app.mode(), new RouteBuilderInstance( app, pluginInfo.routesPrefix ) )
            );
        }
        return lastRoutes;
    }

    /**
     * Before each interaction hook.
     *
     * @param context Context
     */
    /* package */ void beforeInteraction( Context context )
    {
        // Fail fast
        activePlugins.forEach( cp -> cp.plugin.beforeInteraction( context ) );
    }

    /**
     * After each interaction hook.
     *
     * @param context Context
     */
    /* package */ void afterInteraction( Context context )
    {
        // Fail safe
        List<Exception> errors = new ArrayList<>();
        for( PluginInfo pluginInfo : activePlugins )
        {
            try
            {
                pluginInfo.plugin.afterInteraction( context );
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

    /**
     * Lookup plugins by API type.
     *
     * @param pluginApiType Plugin API type
     *
     * @return Plugins matching the given API type, maybe none
     */
    /* package */ <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        if( !activated && !activatingOrPassivating )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        ensureNotNull( "Plugin API Type", pluginApiType );
        Set<T> result = new LinkedHashSet<>();
        for( PluginInfo pluginInfo : activePlugins )
        {
            if( pluginInfo.plugin.apiType().equals( pluginApiType ) && pluginInfo.plugin.api() != null )
            {
                // Type equals
                result.add( pluginApiType.cast( pluginInfo.plugin.api() ) );
            }
        }
        for( PluginInfo pluginInfo : activePlugins )
        {
            if( pluginApiType.isAssignableFrom( pluginInfo.plugin.apiType() ) && pluginInfo.plugin.api() != null )
            {
                // Type is assignable
                result.add( pluginApiType.cast( pluginInfo.plugin.api() ) );
            }
        }
        return result;
    }

    /**
     * Lookup plugin by API type.
     *
     * @param pluginApiType Plugin API Type
     *
     * @return First Plugin matching the given API type, never null
     *
     * @throws IllegalArgumentException if no matching plugin can be found
     */
    /* package */ <T> T plugin( Class<T> pluginApiType )
    {
        if( !activated && !activatingOrPassivating )
        {
            throw new IllegalStateException( "Plugins are passivated." );
        }
        ensureNotNull( "Plugin API Type", pluginApiType );
        for( PluginInfo pluginInfo : activePlugins )
        {
            if( pluginInfo.plugin.apiType().equals( pluginApiType ) && pluginInfo.plugin.api() != null )
            {
                // Type equals
                return pluginApiType.cast( pluginInfo.plugin.api() );
            }
        }
        for( PluginInfo pluginInfo : activePlugins )
        {
            if( pluginApiType.isAssignableFrom( pluginInfo.plugin.apiType() ) && pluginInfo.plugin.api() != null )
            {
                // Type is assignable
                return pluginApiType.cast( pluginInfo.plugin.api() );
            }
        }
        // No Plugin found
        throw new IllegalArgumentException(
            "API for Plugin<" + pluginApiType.getName() + "> not found. "
            + "Active plugins APIs: " + activePlugins.stream().map( p -> p.plugin.apiType() ).collect( toList() ) + "."
        );
    }

    /**
     * Load plugins descriptors from the application classpath.
     *
     * @param application Application
     *
     * @return Plugins descriptors as a Map, keys are names, values are FQCNs
     */
    private LinkedHashMap<String, String> loadPluginsDescriptors( ApplicationInstance application )
    {
        try
        {
            LinkedHashMap<String, String> pluginsDescriptors = new LinkedHashMap<>();
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
                        pluginsDescriptors.put( name, fqcn );
                    }
                }
            }
            return pluginsDescriptors;
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( "Unable to load plugins descriptors", ex );
        }
    }

    /**
     * Load Application Plugins.
     *
     * @param application        Application
     * @param pluginsDescriptors Plugins descriptors
     *
     * @return Application Plugins
     */
    private List<PluginInfo> loadApplicationPlugins(
        ApplicationInstance application,
        LinkedHashMap<String, String> pluginsDescriptors
    )
    {
        EnumSet<ExtensionPlugin> extensions = EnumSet.allOf( ExtensionPlugin.class );
        List<String> enabled = application.config().stringList( "app.plugins.enabled" );
        Map<String, String> routesPrefixes = application.config().stringMap( "app.plugins.routes_prefixes" );
        if( application.mode() == Mode.DEV )
        {
            enabled.addAll( application.config().stringList( "qiweb.devshell.plugins.enabled" ) );
            routesPrefixes.putAll( application.config().stringMap( "qiweb.devshell.plugins.routes_prefixes" ) );
        }
        try
        {
            List<PluginInfo> applicationPlugins = new ArrayList<>();

            // Application Configured Plugins
            for( String pluginNameOrFqcn : enabled )
            {
                String pluginFqcn = pluginsDescriptors.containsKey( pluginNameOrFqcn )
                                    ? pluginsDescriptors.get( pluginNameOrFqcn )
                                    : pluginNameOrFqcn;
                Class<?> pluginClass = application.classLoader().loadClass( pluginFqcn );
                Plugin<?> plugin = (Plugin<?>) application.global().getPluginInstance( application, pluginClass );
                applicationPlugins.add( new PluginInfo( plugin, routesPrefixes.get( pluginNameOrFqcn ) ) );
                extensions.removeIf( extension -> extension.satisfiedBy( plugin ) );
            }

            // Global Extra Plugins
            for( Plugin<?> extraPlugin : application.global().extraPlugins() )
            {
                applicationPlugins.add( new PluginInfo( extraPlugin, null ) );
                extensions.removeIf( extension -> extension.satisfiedBy( extraPlugin ) );
            }

            // Core Extensions Plugins
            for( ExtensionPlugin extension : extensions )
            {
                Plugin<?> extensionPlugin = extension.newDefaultPluginInstance();
                applicationPlugins.add( new PluginInfo( extensionPlugin, null ) );
            }

            return applicationPlugins;
        }
        catch( ClassNotFoundException ex )
        {
            throw new QiWebException( "Unable to load application plugins", ex );
        }
    }

    /**
     * Load dynamic plugins.
     * 
     * @param application        Application
     * @param pluginsDescriptors Plugins descriptors
     * @param appPlugins         Already loaded application plugins
     *
     * @return Dynamic plugins except thoses already loaded by the application
     */
    private List<PluginInfo> loadDynamicPlugins(
        ApplicationInstance application,
        LinkedHashMap<String, String> pluginsDescriptors,
        List<PluginInfo> appPlugins
    )
    {
        try
        {
            List<PluginInfo> dynamicPlugins = new ArrayList<>();
            for( String fqcn : pluginsDescriptors.values() )
            {
                Class<?> pluginClass = application.classLoader().loadClass( fqcn );
                if( !appPlugins.stream().anyMatch( p -> p.plugin.getClass().equals( pluginClass ) ) )
                {
                    Plugin<?> plugin = (Plugin<?>) application.global().getPluginInstance( application, pluginClass );
                    dynamicPlugins.add( new PluginInfo( plugin, null ) );
                }
            }
            return dynamicPlugins;
        }
        catch( ClassNotFoundException ex )
        {
            throw new QiWebException( "Unable to load dynamic plugins", ex );
        }
    }

    /**
     * Resolve and flatten plugins dependency graph.
     *
     * @return flattenned list of plugins, dependency resolved
     */
    private List<PluginInfo> resolveDependencies(
        ApplicationInstance application,
        List<PluginInfo> appPlugins,
        List<PluginInfo> dynamicPlugins
    )
    {
        List<PluginInfo> output = new ArrayList<>( appPlugins.size() );
        ArrayDeque<PluginInfo> queue = new ArrayDeque<>( appPlugins );
        boolean replay = false;
        while( !queue.isEmpty() )
        {
            PluginInfo pluginInfo = queue.poll();
            for( Class<?> dependency : pluginInfo.plugin.dependencies( application.config() ) )
            {
                // Already resolved?
                if( !output.stream().anyMatch(
                    p -> p.plugin.apiType().equals( dependency ) || dependency.isAssignableFrom( p.plugin.apiType() )
                ) )
                {
                    // Same type, then assignable or null
                    PluginInfo match = appPlugins.stream()
                        .filter( p -> p.plugin.apiType().equals( dependency ) )
                        .findFirst()
                        .orElse(
                            appPlugins.stream()
                            .filter( p -> dependency.isAssignableFrom( p.plugin.apiType() ) )
                            .findFirst()
                            .orElse( null )
                        );
                    if( match == null )
                    {
                        // Dynamic plugins, same type, then assignable or null
                        match = dynamicPlugins.stream()
                            .filter( p -> p.plugin.apiType().equals( dependency ) )
                            .findFirst()
                            .orElse(
                                appPlugins.stream()
                                .filter( p -> dependency.isAssignableFrom( p.plugin.apiType() ) )
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
                        // Add to queue so dependencies of dependency gets resolved
                        queue.addFirst( match );
                        // Register matched dependency
                        output.add( match );
                    }
                    else
                    {
                        // Dependency resolved directly from application plugins
                        queue.remove( match );
                        output.add( match );
                    }
                }
            }
            if( !output.contains( pluginInfo ) )
            {
                output.add( pluginInfo );
            }
        }
        if( replay )
        {
            return resolveDependencies( application, output, EMPTY_LIST );
        }
        return output;
    }

    @Override
    public String toString()
    {
        int apiTypePadLen = 0;
        for( PluginInfo pluginInfo : activePlugins )
        {
            String apiName = pluginInfo.plugin.apiType().getSimpleName();
            if( apiName.length() > apiTypePadLen )
            {
                apiTypePadLen = apiName.length();
            }
        }
        StringBuilder sb = new StringBuilder();
        for( Iterator<PluginInfo> it = activePlugins.iterator(); it.hasNext(); )
        {
            PluginInfo pluginInfo = it.next();
            sb.append( rightPad( apiTypePadLen, pluginInfo.plugin.apiType().getSimpleName() ) );
            sb.append( " provided by " );
            sb.append( pluginInfo.plugin.getClass().getName() );
            if( it.hasNext() )
            {
                sb.append( NEWLINE );
            }            
        }
        return sb.toString();
    }
}
