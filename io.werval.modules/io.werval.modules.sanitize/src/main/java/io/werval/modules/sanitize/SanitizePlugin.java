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
package io.werval.modules.sanitize;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import io.werval.modules.metrics.Metrics;
import io.werval.modules.xml.XML;

/**
 * Sanitize Plugin.
 */
public final class SanitizePlugin
    implements Plugin<Sanitize>
{
    private Sanitize sanitize;

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "sanitize.metrics" ) )
        {
            return Arrays.asList( Metrics.class, XML.class );
        }
        return Arrays.asList( XML.class );
    }

    @Override
    public Class<Sanitize> apiType()
    {
        return Sanitize.class;
    }

    @Override
    public Sanitize api()
    {
        return sanitize;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config().object( "sanitize" );
        URL policy = application.classLoader().getResource( config.string( "policy" ) );
        sanitize = new Sanitize(
            application.classLoader(),
            application.langs().defaultLang(),
            policy,
            config.bool( "metrics" ) ? application.plugin( Metrics.class ) : null
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        sanitize = null;
    }
}
