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
package io.werval.modules.rythm;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Mode;
import io.werval.api.exceptions.ActivationException;
import io.werval.api.templates.Templates;
import io.werval.api.templates.TemplatesPlugin;
import io.werval.modules.metrics.Metrics;
import io.werval.modules.metrics.internal.TemplatesMetricsHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;

import static java.util.Collections.EMPTY_LIST;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_CLASS_LOADER_PARENT_IMPL;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_MODE;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_PLUGIN_VERSION;
import static org.rythmengine.conf.RythmConfigurationKey.LOG_FACTORY_IMPL;
import static org.rythmengine.conf.RythmConfigurationKey.RESOURCE_DEF_LOADER_ENABLED;
import static org.rythmengine.conf.RythmConfigurationKey.RESOURCE_LOADER_IMPLS;

/**
 * Rythm Templates Plugin.
 *
 * See the Rythm documentation at <a href="http://rythmengine.org/">rythmengine.org</a>.
 */
public class RythmPlugin
    extends TemplatesPlugin
{
    private RythmTemplates templates;

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "rythm.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

    @Override
    public Templates api()
    {
        return templates;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Map<String, Object> conf = new HashMap<>();

        // Rythm base configuration
        conf.put(
            ENGINE_PLUGIN_VERSION.getKey(),
            BuildVersion.VERSION
        );
        conf.put(
            ENGINE_MODE.getKey(),
            application.mode() == Mode.DEV ? Rythm.Mode.dev : Rythm.Mode.prod
        );
        conf.put(
            ENGINE_CLASS_LOADER_PARENT_IMPL.getKey(),
            application.classLoader()
        );
        conf.put(
            RESOURCE_DEF_LOADER_ENABLED.getKey(),
            false
        );
        conf.put(
            RESOURCE_LOADER_IMPLS.getKey(),
            new NamedTemplateLoader( application, application.config().string( "rythm.base_path" ) )
        );
        conf.put(
            LOG_FACTORY_IMPL.getKey(),
            new SLF4JLoggerFactory()
        );

        // Load Rythm configuration, possibly overriding base configuration
        conf.putAll( application.config().stringMap( "rythm.config" ) );

        // Activate Rythm Engine
        RythmEngine ryhtm = new RythmEngine( conf );

        // Done!
        templates = new RythmTemplates(
            ryhtm,
            application.config().bool( "rythm.metrics" )
            ? new TemplatesMetricsHandler.Impl( application.plugin( Metrics.class ).metrics(), "rythm" )
            : TemplatesMetricsHandler.NOOP
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        templates.shutdown();
        templates = null;
    }
}
