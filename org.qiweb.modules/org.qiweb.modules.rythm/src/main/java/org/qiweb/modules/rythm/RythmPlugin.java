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
package org.qiweb.modules.rythm;

import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Mode;
import org.qiweb.api.Plugin;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;

import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_CLASS_LOADER_PARENT_IMPL;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_MODE;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_PLUGIN_VERSION;
import static org.rythmengine.conf.RythmConfigurationKey.LOG_FACTORY_IMPL;

/**
 * Rythm Template Engine Plugin.
 *
 * See the Rythm documentation at <a href="http://rythmengine.org/">rythmengine.org</a>.
 */
public class RythmPlugin
    implements Plugin<RythmEngine>
{
    private RythmEngine rythm;

    @Override
    public Class<RythmEngine> apiType()
    {
        return RythmEngine.class;
    }

    @Override
    public RythmEngine api()
    {
        return rythm;
    }

    @Override
    public void onActivate( Application application )
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
            LOG_FACTORY_IMPL.getKey(),
            new SLF4JLoggerFactory()
        );

        // Load Rythm configuration, possibly overriding base configuration
        conf.putAll( application.config().stringMap( "rythm" ) );

        // Activate Rythm Engine
        rythm = new RythmEngine( conf );
    }

    @Override
    public void onPassivate( Application application )
    {
        rythm.shutdown();
        rythm = null;
    }
}
