/**
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import org.qiweb.api.exceptions.QiWebException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Assert Stacktraces behaviour.
 */
public class StacktracesTest
{
    private final Throwable root;

    public StacktracesTest()
    {
        Throwable ex = new QiWebException( "QiWeb" );
        ex.addSuppressed( new IllegalStateException( "IllegalState" ) );
        root = new IllegalArgumentException( "IllegalArgument", ex );
        root.addSuppressed( new RuntimeException( "Runtime", new UnsupportedEncodingException( "UnsupportedEncoding" ) ) );
    }

    @Test
    public void containsEqual()
    {
        assertTrue( Stacktraces.containsEqual( IllegalArgumentException.class ).test( root ) );
        assertTrue( Stacktraces.containsEqual( QiWebException.class ).test( root ) );
        assertTrue( Stacktraces.containsEqual( IllegalStateException.class ).test( root ) );
        assertTrue( Stacktraces.containsEqual( UnsupportedEncodingException.class ).test( root ) );

        assertFalse( Stacktraces.containsEqual( Exception.class ).test( root ) );
        assertFalse( Stacktraces.containsEqual( IOException.class ).test( root ) );
        assertFalse( Stacktraces.containsEqual( Error.class ).test( root ) );
    }

    @Test
    public void containsAssignable()
    {
        assertTrue( Stacktraces.containsAssignable( QiWebException.class ).test( root ) );
        assertTrue( Stacktraces.containsAssignable( IOException.class ).test( root ) );

        assertFalse( Stacktraces.containsAssignable( Error.class ).test( root ) );
        assertFalse( Stacktraces.containsAssignable( UnsupportedOperationException.class ).test( root ) );
    }

    @Test
    public void containsMessage()
    {
        assertTrue( Stacktraces.containsMessage( "QiWeb" ).test( root ) );
        assertTrue( Stacktraces.containsMessage( "IllegalState" ).test( root ) );
        assertTrue( Stacktraces.containsMessage( "IllegalArgument" ).test( root ) );
        assertTrue( Stacktraces.containsMessage( "Runtime" ).test( root ) );
        assertTrue( Stacktraces.containsMessage( "UnsupportedEncoding" ).test( root ) );

        assertFalse( Stacktraces.containsMessage( "legalSta" ).test( root ) );
        assertFalse( Stacktraces.containsMessage( "WTF" ).test( root ) );
    }

    @Test
    public void containsInMessage()
    {
        assertTrue( Stacktraces.containsInMessage( "iWe" ).test( root ) );
        assertTrue( Stacktraces.containsInMessage( "legalSta" ).test( root ) );
        assertTrue( Stacktraces.containsInMessage( "legalArg" ).test( root ) );
        assertTrue( Stacktraces.containsInMessage( "ntim" ).test( root ) );
        assertTrue( Stacktraces.containsInMessage( "tedEnc" ).test( root ) );

        assertFalse( Stacktraces.containsInMessage( "WTF" ).test( root ) );
    }
}
