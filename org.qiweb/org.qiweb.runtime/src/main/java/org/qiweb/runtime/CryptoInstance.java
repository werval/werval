/*
 * Copyright (c) 2013-2014 the original author or authors
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.qiweb.api.Crypto;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.util.Hex;
import org.qiweb.util.Reflectively;

/**
 * Cryptography service instance.
 */
public class CryptoInstance
    implements Crypto
{
    private final byte[] secretBytes;
    private final Charset charset;

    public CryptoInstance( String secret, Charset charset )
    {
        try
        {
            this.secretBytes = Hex.decode( secret.toCharArray() );
        }
        catch( IllegalArgumentException ex )
        {
            throw new QiWebException( "Wrong Application Secret: " + ex.getMessage(), ex );
        }
        if( this.secretBytes.length < 32 )
        {
            throw new QiWebException( "Weak Application Secret: must be at least 256bits long" );
        }
        this.charset = charset;
    }

    @Override
    public byte[] newSecret()
    {
        return newRandomSecret256Bits();
    }

    @Override
    public String newSecretHex()
    {
        return newRandomSecret256BitsHex();
    }

    @Override
    public String newSecretBase64()
    {
        return Base64.getEncoder().encodeToString( newRandomSecret256Bits() );
    }

    @Reflectively.Invoked( by = "DevShell" )
    public static String newRandomSecret256BitsHex()
    {
        return Hex.encode( newRandomSecret256Bits() );
    }

    private static byte[] newRandomSecret256Bits()
    {
        try
        {
            byte[] bytes = new byte[ 32 ];
            SecureRandom.getInstanceStrong().nextBytes( bytes );
            return bytes;
        }
        catch( NoSuchAlgorithmException ex )
        {
            throw new InternalError( ex.getMessage(), ex );
        }
    }

    @Override
    public byte[] hmacSha256( byte[] message )
    {
        return hmacSha256( message, secretBytes );
    }

    @Override
    public byte[] hmacSha256( byte[] message, byte[] secret )
    {
        try
        {
            SecretKeySpec signingKey = new SecretKeySpec( secret, "HmacSHA256" );
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( signingKey );
            return mac.doFinal( message );
        }
        catch( NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex )
        {
            throw new QiWebException( "Unable to HMAC message", ex );
        }
    }

    @Override
    public String hmacSha256Hex( CharSequence message )
    {
        return Hex.encode(
            hmacSha256(
                message.toString().getBytes( charset ),
                secretBytes
            )
        );
    }

    @Override
    public String hmacSha256Hex( CharSequence message, String secret )
    {
        return Hex.encode(
            hmacSha256(
                message.toString().getBytes( charset ),
                Hex.decode( secret.toCharArray() )
            )
        );
    }

    @Override
    public byte[] sha256( byte[] message )
    {
        try
        {
            return MessageDigest.getInstance( "SHA-256" ).digest( message );
        }
        catch( NoSuchAlgorithmException ex )
        {
            throw new QiWebException( "Unable to SHA256 message", ex );
        }
    }

    @Override
    public byte[] sha256( CharSequence message )
    {
        return sha256( message.toString().getBytes( charset ) );
    }

    @Override
    public String sha256Hex( CharSequence message )
    {
        return Hex.encode( sha256( message ) );
    }

    @Override
    public String sha256Base64( CharSequence message )
    {
        return Base64.getEncoder().encodeToString( sha256( message ) );
    }
}
