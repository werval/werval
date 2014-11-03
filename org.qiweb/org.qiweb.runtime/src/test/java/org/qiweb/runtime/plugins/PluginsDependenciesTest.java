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
package org.qiweb.runtime.plugins;

import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Plugins Dependencies Test.
 */
public class PluginsDependenciesTest
{
    // The configuration file declare the plugins in the wrong order
    // This test ensure that the dependency resolution algorithm does its job
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "plugin-dependencies-test.conf" );

    public static class Upstream
    {
    }

    public static class UpstreamPlugin
        implements Plugin<Upstream>
    {
        private final Upstream api = new Upstream();

        @Override
        public Class<Upstream> apiType()
        {
            return Upstream.class;
        }

        @Override
        public Upstream api()
        {
            return api;
        }
    }

    public static class Downstream
    {
    }

    public static class DownstreamPlugin
        implements Plugin<Downstream>
    {
        private final Downstream api = new Downstream();

        @Override
        public Class<Downstream> apiType()
        {
            return Downstream.class;
        }

        @Override
        public Downstream api()
        {
            return api;
        }

        @Override
        public List<Class<?>> dependencies( Config config )
        {
            return Arrays.asList( Upstream.class, Cache.class );
        }

        @Override
        public void onActivate( Application application )
            throws ActivationException
        {
            application.plugin( Upstream.class );
        }
    }

    @Test
    public void ensurePluginsActivated()
    {
        Upstream upstream = QIWEB.application().plugin( Upstream.class );
        Downstream downstream = QIWEB.application().plugin( Downstream.class );
        assertThat( upstream, notNullValue() );
        assertThat( downstream, notNullValue() );
    }
}
