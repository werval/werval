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
package org.qiweb.api.util;

import org.junit.Test;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * UUIDIdentityGenerator Test.
 */
public class UUIDIdentityGeneratorTest
{
    @Test
    public void reset()
    {
        UUIDIdentityGenerator idGen = new UUIDIdentityGenerator();
        String identity = idGen.newIdentity();
        assertThat( identity, endsWith( "_0" ) );
        assertThat( identity.length(), is( 36 + 1 + 1 ) );
        identity = idGen.newIdentity();
        assertThat( identity, endsWith( "_1" ) );
        idGen.reset();
        identity = idGen.newIdentity();
        assertThat( identity, endsWith( "_0" ) );
    }

    @Test
    public void prefix()
    {
        String prefix = "prefix";
        IdentityGenerator idGen = new UUIDIdentityGenerator( prefix );
        String identity = idGen.newIdentity();
        assertThat( identity, startsWith( "prefix_" ) );
        assertThat( identity.length(), is( prefix.length() + 1 + 36 + 1 + 1 ) );
    }

    @Test
    public void countLeftPad()
    {
        IdentityGenerator idGen = new UUIDIdentityGenerator( 12 );
        String identity = idGen.newIdentity();
        assertThat( identity, endsWith( "_000000000000" ) );
        assertThat( identity.length(), is( 36 + 1 + 12 ) );
    }

    @Test
    public void prefixAndCountLeftPad()
    {
        String prefix = "prefix";
        IdentityGenerator idGen = new UUIDIdentityGenerator( prefix, 12 );
        String identity = idGen.newIdentity();
        assertThat( identity, startsWith( "prefix_" ) );
        assertThat( identity, endsWith( "_000000000000" ) );
        assertThat( identity.length(), is( prefix.length() + 1 + 36 + 1 + 12 ) );
    }
}
