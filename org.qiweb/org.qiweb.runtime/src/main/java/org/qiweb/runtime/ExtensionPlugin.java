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
package org.qiweb.runtime;

import io.werval.api.Plugin;
import io.werval.api.cache.Cache;
import io.werval.spi.cache.MapCachePlugin;

/**
 * Extension Plugins.
 *
 * An Extension Plugin is a Plugin that MUST be registered into a QiWeb Application.
 * <p>
 * This enum owns the list of Extension Plugins and their type.
 * <p>
 * An Extension is considered registered if the a plugin whose API is assignable to one of thoses Extension Plugin types
 * is either configured or provided by the Global object.
 * <p>
 * If absent, the {@link #newDefaultPluginInstance()} method provide the default implementations.
 * <p>
 * All default implementations used here usually comes from SPI but can reside in Runtime too.
 */
/* package */ enum ExtensionPlugin
{
    /**
     * Cache Plugin defaulted to a {@link MapCachePlugin}.
     */
    CACHE( Cache.class );

    private final Class<?> pluginApi;

    private ExtensionPlugin( Class<?> pluginApi )
    {
        this.pluginApi = pluginApi;
    }

    /**
     * Check if this extension point is satisfied by a given Plugin.
     *
     * @param plugin Candidate Plugin
     *
     * @return {@literal true} if this extension plugin API type is assignable from the given plugin API type
     */
    /* package */ boolean satisfiedBy( Plugin<?> plugin )
    {
        return pluginApi.isAssignableFrom( plugin.apiType() );
    }

    /**
     * @return A new instance of the default plugin for this extension point.
     */
    /* package */ Plugin<?> newDefaultPluginInstance()
    {
        switch( this )
        {
            case CACHE:
                return new MapCachePlugin();
            default:
                throw new InternalError(
                    "Unknown Extension Plugin: " + this + ". Codebase is corrupted, "
                    + "please report the issue: https://scm.codeartisans.org/qiweb/qiweb/issues/new"
                );
        }
    }
}
