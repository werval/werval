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

import java.util.List;
import java.util.Set;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.Config;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Nested Config Test.
 */
public class NestedConfigTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "nested-config.conf" );

    @Test
    public void nestedObjectsAndArrays()
    {
        Config config = QIWEB.application().config();
        assertTrue( config.has( "nested" ) );
        assertTrue( config.has( "nested.list" ) );

        Config nested = config.object( "nested" );
        assertTrue( nested.has( "list" ) );

        List<Config> list = nested.array( "list" );
        assertThat( list.size(), is( 2 ) );

        assertThat( list.get( 0 ).string( "name" ), equalTo( "foo" ) );
        assertThat( list.get( 1 ).string( "name" ), equalTo( "bar" ) );
    }

    @Test
    public void dynamicNestedObjects()
    {
        Config config = QIWEB.application().config();
        assertTrue( config.has( "nested" ) );
        assertTrue( config.has( "nested.dynamics" ) );

        Config dynamics = config.object( "nested.dynamics" );
        Set<String> keys = dynamics.subKeys();
        assertThat( keys.size(), is( 2 ) );
        assertTrue( keys.contains( "foo" ) );
        assertTrue( keys.contains( "baz" ) );

        assertThat( dynamics.object( "foo" ).string( "prop" ), equalTo( "bar" ) );
        assertThat( dynamics.object( "baz" ).string( "prop" ), equalTo( "bazar" ) );
    }
}
