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
package io.werval.modules.jpa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.context.Context;
import io.werval.api.exceptions.ActivationException;
import io.werval.modules.jdbc.JDBC;
import io.werval.modules.metrics.Metrics;

import static io.werval.modules.jpa.JPAContext.METADATA_CONTEXT_KEY;

/**
 * JPA 2 Plugin.
 */
public class JPAPlugin
    implements Plugin<JPA>
{
    private JPA jpa;

    @Override
    public Class<JPA> apiType()
    {
        return JPA.class;
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "jpa.metrics" ) )
        {
            return Arrays.asList( Metrics.class, JDBC.class );
        }
        return Arrays.asList( JDBC.class );
    }

    @Override
    public JPA api()
    {
        return jpa;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        boolean hasXml = application.classLoader().getResource( "META-INF/persistence.xml" ) != null;

        Config config = application.config();
        Map<String, Map<String, Object>> properties = new HashMap<>();
        if( config.has( "jpa.units" ) )
        {
            Config units = config.atPath( "jpa.units" );
            units.subKeys().stream().forEach(
                unit -> properties.put( unit, (Map<String, Object>) (Map) units.stringMap( unit ) )
            );
        }
        jpa = new JPA(
            application.mode(),
            application.classLoader(),
            properties,
            config.has( "jpa.default_pu" ) ? config.string( "jpa.default_pu" ) : null,
            config.bool( "jpa.metrics" ) ? application.plugin( Metrics.class ) : null
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        jpa.passivate();
        jpa = null;
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
