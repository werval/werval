/**
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
package org.qiweb.plugin.rythm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Plugin;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;

import static org.qiweb.api.Application.Mode.DEV;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_CLASS_LOADER_PARENT_IMPL;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_MODE;
import static org.rythmengine.conf.RythmConfigurationKey.ENGINE_PLUGIN_VERSION;
import static org.rythmengine.conf.RythmConfigurationKey.LOG_FACTORY_IMPL;
import static org.rythmengine.conf.RythmConfigurationKey.HOME_TEMPLATE;

/**
 * Rythm Template Engine Plugin.
 * <p>See the Rythm documentation at <a href="http://rythmengine.org/">http://rythmengine.org/</a>.</p>
 */
public class RythmPlugin
    implements Plugin<RythmEngine>
{
    public static final String RYTHM_TEMPLATES_PACKAGE_CONFIG_KEY = "rythm.templates_package";
    public static final String RYTHM_TEMPLATES_PACKAGE_DEFAULT_VAL = "views";
    private RythmEngine rythm;

    @Override
    public boolean enabled()
    {
        return true;
    }

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
        String templatesPackage = application.config().has( RYTHM_TEMPLATES_PACKAGE_CONFIG_KEY )
                                  ? application.config().string( RYTHM_TEMPLATES_PACKAGE_CONFIG_KEY )
                                  : RYTHM_TEMPLATES_PACKAGE_DEFAULT_VAL;

        Map<String, Object> conf = new HashMap<>();
        conf.put( ENGINE_PLUGIN_VERSION.getKey(),
                  BuildVersion.VERSION );
        conf.put( ENGINE_MODE.getKey(),
                  application.mode() == DEV ? Rythm.Mode.dev : Rythm.Mode.prod );
        conf.put( ENGINE_CLASS_LOADER_PARENT_IMPL.getKey(),
                  application.classLoader() );
        conf.put( HOME_TEMPLATE.getKey(),
                  new File( application.classLoader().getResource( templatesPackage ).getFile() ) );
        conf.put( LOG_FACTORY_IMPL.getKey(),
                  new SLF4JLoggerFactory() );

        rythm = new RythmEngine( conf );
    }

    @Override
    public void onPassivate( Application application )
    {
        rythm.shutdown();
        rythm = null;
    }
}
