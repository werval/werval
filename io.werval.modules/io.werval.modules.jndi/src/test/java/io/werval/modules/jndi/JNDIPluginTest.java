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
package io.werval.modules.jndi;

import io.werval.test.WervalRule;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * JNDI Plugin Test.
 *
 * This test assert embedded behaviour only.
 */
public class JNDIPluginTest
{
    @Rule
    public final WervalRule werval = new WervalRule();
    private static InitialContext ctx;

    @Before
    public void setJNDIEntriesUp()
        throws NamingException
    {
        ctx = werval.application().plugin( JNDI.class ).initialContext();
        ctx.rebind( "foo", "bar" );
        ctx.rebind( "baz", "bazar" );
    }

    @Test
    public void jndiPluginInitialContext()
        throws NamingException
    {
        assertThat( ctx.lookup( "foo" ), equalTo( "bar" ) );
        assertThat( ctx.lookup( "baz" ), equalTo( "bazar" ) );
    }

    @Test
    public void newInitialContext()
        throws NamingException
    {
        assertThat( new InitialContext().lookup( "foo" ), equalTo( "bar" ) );
        assertThat( new InitialContext().lookup( "baz" ), equalTo( "bazar" ) );
    }

    @Test
    public void lifecycleLeft()
        throws NamingException
    {
        ctx.rebind( "left", "left" );
        try
        {
            ctx.lookup( "right" );
            fail( "JNDI Embedded Context not cleared on passivation" );
        }
        catch( NameNotFoundException expected )
        {
        }
    }

    @Test
    public void lifecycleRight()
        throws NamingException
    {
        ctx.rebind( "right", "right" );
        try
        {
            ctx.lookup( "left" );
            fail( "JNDI Embedded Context not cleared on passivation" );
        }
        catch( NameNotFoundException expected )
        {
        }
    }
}
