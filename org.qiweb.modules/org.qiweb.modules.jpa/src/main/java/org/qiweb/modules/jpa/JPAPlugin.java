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
package org.qiweb.modules.jpa;

import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.PluginAdapter;
import org.qiweb.api.exceptions.ActivationException;

/**
 * JPA 2 Plugin.
 */
public class JPAPlugin
    extends PluginAdapter<JPA>
{
    private JPA jpa;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        Map<String, Map<String, ?>> properties = new HashMap<>();
        if( config.has( "jpa.units" ) )
        {
            Config units = config.object( "jpa.units" );
            units.subKeys().stream().forEach( unit -> properties.put( unit, units.stringMap( unit ) ) );
        }
        jpa = new JPA(
            application.classLoader(),
            properties,
            config.has( "jpa.default_pu" ) ? config.string( "jpa.default_pu" ) : null
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        jpa.passivate();
        jpa = null;
    }

    @Override
    public Class<JPA> apiType()
    {
        return JPA.class;
    }

    @Override
    public JPA api()
    {
        return jpa;
    }
}
