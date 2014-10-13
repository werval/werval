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
package org.qiweb.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Strings Test.
 */
public class StringsTest
{
    @Test
    public void rightPad()
    {
        try
        {
            Strings.rightPad( -1, null, 'c' );
            fail( "Negative length should have raised an IllegalArgumentException" );
        }
        catch( IllegalArgumentException expected )
        {
        }
        assertThat( Strings.rightPad( 7, null, 'c' ), equalTo( "ccccccc" ) );
        assertThat( Strings.rightPad( 7, "", 'c' ), equalTo( "ccccccc" ) );
        assertThat( Strings.rightPad( 7, "FOO", 'c' ), equalTo( "FOOcccc" ) );
        assertThat( Strings.rightPad( 7, "FOOOOOO", 'c' ), equalTo( "FOOOOOO" ) );
        assertThat( Strings.rightPad( 7, "FOOOOOO---", 'c' ), equalTo( "FOOOOOO---" ) );
    }

    @Test
    public void leftPad()
    {
        try
        {
            Strings.leftPad( -1, null, 'c' );
            fail( "Negative length should have raised an IllegalArgumentException" );
        }
        catch( IllegalArgumentException expected )
        {
        }
        assertThat( Strings.leftPad( 7, null, 'c' ), equalTo( "ccccccc" ) );
        assertThat( Strings.leftPad( 7, "", 'c' ), equalTo( "ccccccc" ) );
        assertThat( Strings.leftPad( 7, "FOO", 'c' ), equalTo( "ccccFOO" ) );
        assertThat( Strings.leftPad( 7, "FOOOOOO", 'c' ), equalTo( "FOOOOOO" ) );
        assertThat( Strings.leftPad( 7, "FOOOOOO---", 'c' ), equalTo( "FOOOOOO---" ) );
    }

    @Test
    public void trail()
    {
        assertThat( Strings.withTrail( "foo", "/" ), equalTo( "foo/" ) );
        assertThat( Strings.withoutTrail( "foo/", "/" ), equalTo( "foo" ) );
        assertThat( Strings.withTrail( "foo", "///" ), equalTo( "foo///" ) );
        assertThat( Strings.withoutTrail( "foo///", "///" ), equalTo( "foo" ) );
    }

    @Test
    public void head()
    {
        assertThat( Strings.withHead( "foo", "/" ), equalTo( "/foo" ) );
        assertThat( Strings.withoutHead( "/foo", "/" ), equalTo( "foo" ) );
        assertThat( Strings.withHead( "foo", "///" ), equalTo( "///foo" ) );
        assertThat( Strings.withoutHead( "///foo", "///" ), equalTo( "foo" ) );
    }

    @Test
    public void indexOfNth()
    {
        String tested = "	at org.qiweb.runtime.filters.FilterChainInstance$FilterChainControllerTail.next";
        int index = Strings.indexOfNth( tested, 2, "." );
        assertThat( index, is( 13 ) );
        assertThat(
            tested.substring( index ),
            equalTo( ".runtime.filters.FilterChainInstance$FilterChainControllerTail.next" )
        );
    }

    @Test
    public void lastIndexOfNth()
    {
        String tested = "	at org.qiweb.runtime.filters.FilterChainInstance$FilterChainControllerTail.next";
        int index = Strings.lastIndexOfNth( tested, 2, "." );
        assertThat( index, is( 29 ) );
        assertThat( tested.substring( 0, index ), equalTo( "	at org.qiweb.runtime.filters" ) );
    }
}
