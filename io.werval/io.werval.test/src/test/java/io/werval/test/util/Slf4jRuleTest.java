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
package io.werval.test.util;

import java.util.Stack;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Assert the SLF4J JUnit Rule behaviour.
 */
public class Slf4jRuleTest
{
    static class SimpleStack
    {
        private static final Logger LOG = LoggerFactory.getLogger( SimpleStack.class );
        private final Stack<String> stack = new Stack<>();

        void push( String item )
        {
            final String method = "push() :";
            LOG.trace( "{} Start", method );
            stack.push( item );
            LOG.info( "{} Push item {} into stack.", method, item );
            LOG.trace( "{} End", method );
        }
    }

    @Rule
    public Slf4jRule slf4j = new Slf4jRule()
    {
        {
            record( Level.INFO );
            recordForType( SimpleStack.class );
        }
    };

    SimpleStack stack;

    @Before
    public void setup()
    {
        stack = new SimpleStack();
    }

    @Test
    public void testPush()
    {
        stack.push( "Item One" );
        assertThat( slf4j.contains( "Push item Item One into stack." ), is( true ) );
        assertThat( slf4j.size(), is( 1 ) );
    }
}
