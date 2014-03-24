/*
 * Copyright (c) 2013 the original author or authors
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

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.qiweb.runtime.util.Iterables.count;

/**
 * Assert Plugin system overall behaviour.
 */
public class PluginsTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( "plugin-test.conf" );

    @Test( expected = IllegalArgumentException.class )
    public void givenWrongPluginAPIWhenGetPluginExpectIAE()
    {
        QIWEB.application().plugin( HelloWorldPlugin.class );
    }

    @Test
    public void givenRegisteredPluginWhenGetPluginExpectPresent()
    {
        HelloWorld plugin = QIWEB.application().plugin( HelloWorld.class );
        assertThat( plugin, notNullValue() );
    }

    @Test
    public void givenRegisteredPluginWhenUsePluginExpectCorrectResult()
    {
        String hello = QIWEB.application().plugin( HelloWorld.class ).sayHello( "John" );
        assertThat( hello, equalTo( "Hello John!" ) );
    }

    @Test
    public void givenRegisteredPluginWhenGetPluginsExpectSingle()
    {
        Iterable<HelloWorld> plugins = QIWEB.application().plugins( HelloWorld.class );
        assertThat( count( plugins ), is( 1L ) );
    }

    @Test
    public void testActivations()
    {
        HelloWorld plugin = QIWEB.application().plugin( HelloWorld.class );
        assertThat( plugin.activations(), is( 1 ) );
        assertThat( plugin.passivations(), is( 0 ) );
    }
}
