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

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.qiweb.util.Maps.fromMap;
import static org.qiweb.util.Strings.leftPad;

/**
 * Base62 Test.
 */
public class Base62Test
{
    @Test
    public void encodeZero()
    {
        assertThat( Base62.encode( BigInteger.ZERO ), equalTo( "0" ) );
        assertThat( Base62.encode( 0L ), equalTo( "0" ) );
        assertThat( Base62.encode( 0 ), equalTo( "0" ) );
        byte[] zeroByte = new byte[]
        {
            0
        };
        assertThat( Base62.encode( zeroByte ), equalTo( "0" ) );
    }

    @Test
    public void decodeZero()
    {
        assertThat( Base62.decode( "0" ), equalTo( BigInteger.ZERO ) );
        assertThat( Base62.decodeLong( "0" ), is( 0L ) );
        assertThat( Base62.decodeInt( "0" ), is( 0 ) );
        byte[] zeroByte = new byte[]
        {
            0
        };
        assertThat( Base62.decodeBytes( "0" ), equalTo( zeroByte ) );
    }

    @Test
    public void decode10()
    {
        assertThat( Base62.decode( "10" ), equalTo( BigInteger.valueOf( 62 ) ) );
        assertThat( Base62.decodeLong( "10" ), is( 62L ) );
        assertThat( Base62.decodeInt( "10" ), is( 62 ) );
        assertThat( Base62.decodeBytes( "10" ), equalTo( BigInteger.valueOf( 62 ).toByteArray() ) );
    }

    @Test
    public void decodeZz()
    {
        assertThat( Base62.decode( "Z" ), equalTo( BigInteger.valueOf( 62 - 1 ) ) );
        assertThat( Base62.decodeLong( "Z" ), is( (long) 62 - 1 ) );
        assertThat( Base62.decodeInt( "Z" ), is( 62 - 1 ) );
        assertThat( Base62.decodeBytes( "Z" ), equalTo( BigInteger.valueOf( 62 - 1 ).toByteArray() ) );

        assertThat( Base62.decode( "z" ), equalTo( BigInteger.valueOf( 62 - 26 - 1 ) ) );
        assertThat( Base62.decodeLong( "z" ), is( (long) 62 - 26 - 1 ) );
        assertThat( Base62.decodeInt( "z" ), is( 62 - 26 - 1 ) );
        assertThat( Base62.decodeBytes( "z" ), equalTo( BigInteger.valueOf( 62 - 26 - 1 ).toByteArray() ) );
    }

    @Test
    public void largeBigInteger()
    {
        String largeBase62 = "ZZZZZZZZZZZZZZZZZZZZZZ";
        BigInteger largeBigInt = new BigInteger( "2707803647802660400290261537185326956543" );
        assertThat( Base62.decode( largeBase62 ), equalTo( largeBigInt ) );
        assertThat( Base62.encode( largeBigInt ), equalTo( largeBase62 ) );
    }

    @Test
    public void various()
    {
        Map<String, String> bigints = fromMap( new LinkedHashMap<String, String>( 5 ) )
            .put( "569537648459913153376380756355687721046060905866402691744198", "42BsBHEkzQRbydZkXmmj4pmP85NMoav7Ho" )
            .put( "8732446772916102734505667802089163434283255232787881656", "eLKvcfcihEfpU8JAIw1cAY5luCxVYCc" )
            .put( "19843601143557967810493896190608", "6Ifs5GQWAzzmDKm94A" )
            .put( "8625722015886075359705964755", "bdwsjptfJX70qQa7" )
            .put( "1765164665452388900175659", "8P7CI6OBincYBR" )
            .toMap();
        bigints.forEach(
            (number, base62) ->
            {
                assertThat( Base62.encode( new BigInteger( number ) ), equalTo( base62 ) );
                assertThat( Base62.decode( base62 ), equalTo( new BigInteger( number ) ) );
                assertThat( Base62.decode( leftPad( 64, base62, '0' ) ), equalTo( new BigInteger( number ) ) );
                try
                {
                    Base62.decodeInt( base62 );
                    fail( "Should have thrown ArithmeticException" );
                }
                catch( ArithmeticException ex )
                {
                }
                try
                {
                    Base62.decodeLong( base62 );
                    fail( "Should have thrown ArithmeticException" );
                }
                catch( ArithmeticException ex )
                {
                }
                assertThat( Base62.decodeBytes( base62 ), equalTo( new BigInteger( number ).toByteArray() ) );
            }
        );

        Map<Long, String> longs = fromMap( new LinkedHashMap<Long, String>( 5 ) )
            .put( 7_448_383_303_063_853_236L, "8SdFOY4A1RW" )
            .put( 7_146_144_673_685_200_945L, "8vTpWpljHRn" )
            .put( 7_788_493_515_434_605_987L, "9hlnJZ5Ldej" )
            .put( 7_131_121_136_665_553_123L, "8uMBQC6zvTd" )
            .put( 852_426_999_587_2064_716L, "a9Hfjx59FNy" )
            .toMap();
        longs.forEach(
            (number, base62) ->
            {
                assertThat( Base62.encode( number ), equalTo( base62 ) );
                assertThat( Base62.decodeLong( base62 ), is( number ) );
                assertThat( Base62.decodeLong( leftPad( 64, base62, '0' ) ), is( number ) );
                try
                {
                    Base62.decodeInt( base62 );
                    fail( "Should have thrown ArithmeticException" );
                }
                catch( ArithmeticException ex )
                {
                }
                assertThat( Base62.decodeBytes( base62 ), equalTo( BigInteger.valueOf( number ).toByteArray() ) );
            }
        );

        Map<Integer, String> ints = fromMap( new LinkedHashMap<Integer, String>( 5 ) )
            .put( 135_539_714, "9aI4q" )
            .put( 502_187_886, "xZ80C" )
            .put( 1_557_282_121, "1Hocdz" )
            .put( 747_061_836, "OyAUs" )
            .put( 98_698_550, "6G806" )
            .toMap();
        ints.forEach(
            (number, base62) ->
            {
                assertThat( Base62.encode( number ), equalTo( base62 ) );
                assertThat( Base62.decodeInt( base62 ), is( number ) );
                assertThat( Base62.decodeInt( leftPad( 64, base62, '0' ) ), is( number ) );
                assertThat( Base62.decodeBytes( base62 ), equalTo( BigInteger.valueOf( number ).toByteArray() ) );
            }
        );
    }
}
