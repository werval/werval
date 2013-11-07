/**
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
package org.qiweb.runtime;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.qiweb.api.Crypto;
import org.qiweb.api.exceptions.QiWebException;

/**
 * Cryptography service instance.
 */
public class CryptoInstance
    implements Crypto
{

    private static final char[] HEX_DIGITS =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private final byte[] secretBytes;
    private final Charset charset;

    public CryptoInstance( String secret, Charset charset )
    {
        this.secretBytes = decodeHex( secret.toCharArray() );
        if( this.secretBytes.length < 32 )
        {
            throw new QiWebException( "Wrong Application Secret: must be at least 256bits long" );
        }
        this.charset = charset;
    }

    @Override
    public String genNew256bitsHexSecret()
    {
        return genRandom256bitsHexSecret();
    }

    public static String genRandom256bitsHexSecret()
    {
        byte[] bytes = new byte[ 32 ];
        new SecureRandom().nextBytes( bytes );
        new SecureRandom( bytes ).nextBytes( bytes );
        return new String( encodeHex( bytes ) );
    }

    @Override
    public String hexHmacSha256( String message )
    {
        return hexHmacSha256( message, secretBytes );
    }

    @Override
    public String hexHmacSha256( String message, String secret )
    {
        return hexHmacSha256( message, decodeHex( secret.toCharArray() ) );
    }

    private String hexHmacSha256( String message, byte[] secret )
    {
        try
        {
            SecretKeySpec signingKey = new SecretKeySpec( secret, "HmacSHA256" );
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( signingKey );
            byte[] rawHmac = mac.doFinal( message.getBytes( charset ) );
            return new String( encodeHex( rawHmac ) );
        }
        catch( NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e )
        {
            throw new QiWebException( "Unable to HMAC message", e );
        }
    }

    private static byte[] decodeHex( char[] data )
    {
        int len = data.length;
        if( ( len & 0x01 ) != 0 )
        {
            throw new QiWebException( "Wrong Secret: Odd number of characters in hexadecimal encoded." );
        }
        byte[] out = new byte[ len >> 1 ];
        // two characters form the hex value.
        for( int i = 0, j = 0; j < len; i++ )
        {
            int f = toDigit( data[j], j ) << 4;
            j++;
            f |= toDigit( data[j], j );
            j++;
            out[i] = (byte) ( f & 0xFF );
        }
        return out;
    }

    private static int toDigit( char ch, int index )
    {
        int digit = Character.digit( ch, 16 );
        if( digit == -1 )
        {
            throw new QiWebException( "Wrong Secret: Illegal hexadecimal character " + ch + " at index " + index );
        }
        return digit;
    }

    @SuppressWarnings( "ValueOfIncrementOrDecrementUsed" )
    private static char[] encodeHex( byte[] data )
    {
        int l = data.length;
        char[] out = new char[ l << 1 ];
        // two characters form the hex value.
        for( int i = 0, j = 0; i < l; i++ )
        {
            out[j++] = HEX_DIGITS[( 0xF0 & data[i] ) >>> 4];
            out[j++] = HEX_DIGITS[ 0x0F & data[i]];
        }
        return out;
    }

}
