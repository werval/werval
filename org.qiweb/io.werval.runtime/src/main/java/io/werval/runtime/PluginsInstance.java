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
package io.werval.runtime;

import io.werval.api.Application;
import io.werval.api.Mode;
import io.werval.api.Plugin;
import io.werval.api.context.Context;
import io.werval.api.exceptions.PassivationException;
import io.werval.api.exceptions.WervalException;
import io.werval.api.routes.Route;
import io.werval.runtime.routes.RouteBuilderInstance;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.Stack;

import static io.werval.util.IllegalArguments.ensureNotNull;
import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.NEWLINE;
import static io.werval.util.Strings.rightPad;
import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;

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
            WervalException ex = new WervalException( "There were errors during Plugins after interaction hooks" );
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
            Enumeration<URL> descriptors = application.classLoader().getResources(
                "META-INF/werval-plugins.properties"
            );
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
            throw new WervalException( "Unable to load application plugins", ex );
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
            throw new WervalException( "Unable to load dynamic plugins", ex );
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
                    // Replay will be needed to discover transitive dependencies
                    replay = true;
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
                            throw new WervalException( "Plugin dependency not resolved: " + dependency );
                        }
                        // Add to queue so dependencies of dependency gets resolved
                        queue.addFirst( match );
                        // Register matched dependency
                        if( !output.contains( match ) )
                        {
                            output.add( 0, match );
                        }
                    }
                    else
                    {
                        // Dependency resolved directly from application plugins
                        queue.remove( match );
                        if( !output.contains( match ) )
                        {
                            output.add( 0, match );
                        }
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
            return resolveDependencies( application, output, dynamicPlugins );
        }
        Collections.sort(
            output,
            new Comparator<PluginInfo>()
            {
                @Override
                public int compare( PluginInfo pi1, PluginInfo pi2 )
                {
                    List<Class<?>> pi1Deps = pi1.plugin.dependencies( application.config() );
                    List<Class<?>> pi2Deps = pi2.plugin.dependencies( application.config() );

                    if( pi1Deps.isEmpty() && pi2Deps.isEmpty() )
                    {
                        // Both have zero dependency
                        return 0;
                    }
                    if( ( pi1Deps.isEmpty() || pi2Deps.isEmpty() ) && pi1Deps.size() != pi2Deps.size() )
                    {
                        // One has zero dependency, the other has some
                        return Integer.compare( pi1Deps.size(), pi2Deps.size() );
                    }

                    // Plugin 1 Dependencies
                    List<PluginInfo> pi1DepsPluginInfos = new ArrayList<>();
                    Stack<Class<?>> pi1Stack = new Stack<>();
                    pi1Stack.addAll( pi1Deps );
                    while( !pi1Stack.empty() )
                    {
                        Class<?> pi1Dep = pi1Stack.pop();
                        PluginInfo match = output.stream()
                        .filter( pi -> pi.plugin.apiType().equals( pi1Dep ) ).findFirst()
                        .orElse(
                            output.stream().filter( pi -> pi1Dep.isAssignableFrom( pi.plugin.apiType() ) )
                            .findFirst().orElse( null )
                        );
                        if( match != null )
                        {
                            pi1DepsPluginInfos.add( match );
                            pi1Stack.addAll( match.plugin.dependencies( application.config() ) );
                        }
                    }
                    if( pi1DepsPluginInfos.contains( pi2 ) )
                    {
                        // Plugin 1, or one of its dependencies, depends on Plugin 2
                        return +1;
                    }

                    // Plugin 2 Dependencies
                    List<PluginInfo> pi2DepsPluginInfos = new ArrayList<>();
                    Stack<Class<?>> pi2Stack = new Stack<>();
                    pi2Stack.addAll( pi2Deps );
                    while( !pi2Stack.empty() )
                    {
                        Class<?> pi2Dep = pi2Stack.pop();
                        PluginInfo match = output.stream()
                        .filter( pi -> pi.plugin.apiType().equals( pi2Dep ) ).findFirst()
                        .orElse(
                            output.stream().filter( pi -> pi2Dep.isAssignableFrom( pi.plugin.apiType() ) )
                            .findFirst().orElse( null )
                        );
                        if( match != null )
                        {
                            pi2DepsPluginInfos.add( match );
                            pi2Stack.addAll( match.plugin.dependencies( application.config() ) );
                        }
                    }
                    if( pi2DepsPluginInfos.contains( pi1 ) )
                    {
                        // Plugin 2, or one of its dependencies, depends on Plugin 1
                        return -1;
                    }

                    // No decision to make
                    return 0;
                }
            }
        );
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
