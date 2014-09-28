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
import java.util.Optional;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.ActivationException;

import static org.qiweb.modules.jpa.JPAContext.METADATA_CONTEXT_KEY;

/**
 * JPA 2 Plugin.
 */
public class JPAPlugin
    implements Plugin<JPA>
{
    private JPA jpa;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        boolean hasXml = application.classLoader().getResource( "META-INF/persistence.xml" ) != null;

        Config config = application.config();
        Map<String, Map<String, Object>> properties = new HashMap<>();
        if( config.has( "jpa.units" ) )
        {
            Config units = config.object( "jpa.units" );
            units.subKeys().stream().forEach(
                unit -> properties.put( unit, (Map<String, Object>) (Map) units.stringMap( unit ) )
            );
        }
        jpa = new JPA(
            application.mode(),
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

    @Override
    public void beforeInteraction( Context context )
    {
        Optional.ofNullable(
            context.metaData().get( JPAContext.class, METADATA_CONTEXT_KEY )
        ).ifPresent(
            jpaContext ->
            {
                jpaContext.closeFailFast();
                context.metaData().remove( METADATA_CONTEXT_KEY );
            }
        );
    }

    @Override
    public void afterInteraction( Context context )
    {
        Optional.ofNullable(
            context.metaData().get( JPAContext.class, METADATA_CONTEXT_KEY )
        ).ifPresent(
            jpaContext ->
            {
                jpaContext.closeFailSafe();
                context.metaData().remove( METADATA_CONTEXT_KEY );
            }
        );
    }
}
