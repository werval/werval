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
package io.werval.util;

import java.util.Arrays;
import org.junit.Test;

import static io.werval.util.Hashids.DEFAULT_ALPHABET;
import static io.werval.util.Hashids.DEFAULT_SEPARATORS;
import static io.werval.util.Strings.hasText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Hashids Test.
 */
public class HashidsTest
{
    @Test
    public void oneNumber()
    {
        Hashids hashids = new Hashids( "this is my salt" );

        long number = 12_345L;
        String expected = "NkK9";

        assertThat( hashids.encodeToString( number ), equalTo( expected ) );

        long[] decoded = hashids.decodeLongs( expected );
        assertThat( decoded.length, is( 1 ) );
        assertThat( decoded[0], is( number ) );
    }

    @Test
    public void severalNumbers()
    {
        Hashids hashids = new Hashids( "this is my salt" );

        long[] numbers =
        {
            683L, 94_108L, 123L, 5L
        };
        String expected = "aBMswoO2UB3Sj";

        assertThat( hashids.encodeToString( numbers ), equalTo( expected ) );

        long[] decoded = hashids.decodeLongs( expected );
        assertThat( decoded.length, is( numbers.length ) );
        assertTrue( Arrays.equals( decoded, numbers ) );
    }

    @Test
    public void customHashLength()
    {
        Hashids hashids = new Hashids( "this is my salt", 8 );

        long number = 1L;
        String expected = "gB0NV05e";

        assertThat( hashids.encodeToString( number ), equalTo( expected ) );

        long[] decoded = hashids.decodeLongs( expected );
        assertThat( decoded.length, is( 1 ) );
        assertThat( decoded[0], is( number ) );
    }

    @Test
    public void randomness()
    {
        Hashids hashids = new Hashids( "this is my salt" );

        long[] numbers =
        {
            5L, 5L, 5L, 5L
        };
        String expected = "1Wc8cwcE";

        assertThat( hashids.encodeToString( numbers ), equalTo( expected ) );

        long[] decoded = hashids.decodeLongs( expected );
        assertThat( decoded.length, is( numbers.length ) );
        assertTrue( Arrays.equals( decoded, numbers ) );
    }

    @Test
    public void randomnessForIncrementingNumbers()
    {
        Hashids hashids = new Hashids( "this is my salt" );

        long[] numbers =
        {
            1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L
        };
        String expected = "kRHnurhptKcjIDTWC3sx";

        assertThat( hashids.encodeToString( numbers ), equalTo( expected ) );

        long[] decoded = hashids.decodeLongs( expected );
        assertThat( decoded.length, is( numbers.length ) );
        assertTrue( Arrays.equals( decoded, numbers ) );
    }

    @Test
    public void randomnessForIncrementing()
    {
        Hashids hashids = new Hashids( "this is my salt" );
        assertEquals( hashids.encodeToString( 1L ), "NV" );
        assertEquals( hashids.encodeToString( 2L ), "6m" );
        assertEquals( hashids.encodeToString( 3L ), "yD" );
        assertEquals( hashids.encodeToString( 4L ), "2l" );
        assertEquals( hashids.encodeToString( 5L ), "rD" );
    }

    @Test
    public void valuesGreaterIntMaxValue()
    {
        assertThat( new Hashids( "this is my salt" ).encodeToString( 9_876_543_210_123L ), equalTo( "Y8r7W1kNN" ) );
    }

    @Test
    public void maxNumberValue()
        throws Exception
    {
        try
        {
            new Hashids( "this is my salt" ).encodeToString( Hashids.MAX_NUMBER_VALUE + 1 );
            fail( "Hashids shoud not allow encoding number greater or equal to 2^53." );
        }
        catch( IllegalArgumentException expected )
        {
            assertThat( expected.getMessage(), equalTo( "Number out of range" ) );
        }
    }

    @Test
    public void wrongDecoding()
    {
        assertEquals( new Hashids( "this is my pepper" ).decodeLongs( "NkK9" ).length, 0 );
    }

    @Test
    public void hexString()
    {
        Hashids hashids = new Hashids( "this is my salt" );
        String hex = "507f1f77bcf86cd799439011";

        String hash = hashids.encodeToString( hex );
        String returnedHex = hashids.decodeHex( hash );

        assertTrue( hasText( hash ) );
        assertThat( returnedHex, equalTo( hex ) );
    }

    @Test
    public void hexStringWithMinHashLength()
    {
        int minHashLength = 23;
        Hashids hashids = new Hashids( "", minHashLength );
        String hex = "507f1f77bcf86cd799439011";

        String hash = hashids.encodeToString( hex );
        String returnedHex = hashids.decodeHex( hash );

        assertTrue( hasText( hash ) );
        assertThat( returnedHex, equalTo( hex ) );

        assertThat( hash.length(), equalTo( minHashLength ) );
    }

    @Test
    public void longHexString()
    {
        Hashids hashids = new Hashids( "this is my salt" );
        String hex = "f000000000000000000000000000000000000000000000000000000000000000000000000000000000000f";

        String hash = hashids.encodeToString( hex );
        String returnedHex = hashids.decodeHex( hash );

        assertTrue( hasText( hash ) );
        assertThat( returnedHex, equalTo( hex ) );
    }

    @Test
    public void bigMinHashLength()
    {
        int minHashLength = 1000;
        Hashids hashids = new Hashids( "this is my salt", minHashLength );
        long[] numbers = new long[]
        {
            1, 2, 3, 4, 5
        };

        String hash = hashids.encodeToString( numbers );
        long[] returnedNumbers = hashids.decodeLongs( hash );

        assertTrue( hasText( hash ) );
        assertThat( returnedNumbers, equalTo( numbers ) );

        assertThat( hash.length(), equalTo( minHashLength ) );
    }

    @Test
    public void saltMinHashLengthAndCustomAlphabet()
    {
        int minHashLength = 23;
        String customAlphabet = "0123456789abcdef";
        Hashids hashids = new Hashids( "this is my salt", minHashLength, customAlphabet );
        long[] numbers = new long[]
        {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        String hash = hashids.encodeToString( numbers );
        long[] returnedNumbers = hashids.decodeLongs( hash );

        assertTrue( hasText( hash ) );
        assertThat( returnedNumbers, equalTo( numbers ) );

        assertThat( hash.length(), equalTo( minHashLength ) );

        assertThat( hash.matches( "^[0-9a-f]+$" ), is( true ) );
    }

    @Test
    public void negativeValues()
    {
        try
        {
            new Hashids( "this is my salt" ).encodeToString( -1 );
            fail( "Hashids shoud not allow encoding negative numbers." );
        }
        catch( IllegalArgumentException expected )
        {
            assertThat( expected.getMessage(), equalTo( "Number out of range" ) );
        }
    }

    @Test
    public void builder()
    {
        // No salt, use an empty one
        new Hashids.Builder().build();

        // Still no salt, use an empty one
        new Hashids.Builder()
            .alphabet( DEFAULT_ALPHABET )
            .minimumLength( 16 )
            .separators( DEFAULT_SEPARATORS )
            .build();
    }

    @Test
    public void noSeparators()
    {
        Hashids hashids = new Hashids.Builder().separators( null ).build();
        long[] numbers = new long[]
        {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        String hashid = hashids.encodeToString( numbers );
        long[] decoded = hashids.decodeLongs( hashid );
        assertTrue( Arrays.equals( numbers, decoded ) );
    }

    @Test
    public void customSeparators()
    {
        Hashids hashids = new Hashids.Builder().separators( "abcdABCD" ).build();
        long[] numbers = new long[]
        {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        String hashid = hashids.encodeToString( numbers );
        long[] decoded = hashids.decodeLongs( hashid );
        assertTrue( Arrays.equals( numbers, decoded ) );
    }

    @Test
    public void integers()
    {
        Hashids hashids = new Hashids.Builder().separators( "abcdABCD" ).build();
        int[] numbers = new int[]
        {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        String hashid = hashids.encodeToString( numbers );
        int[] decoded = hashids.decodeInts( hashid );
        assertTrue( Arrays.equals( numbers, decoded ) );
    }
}
