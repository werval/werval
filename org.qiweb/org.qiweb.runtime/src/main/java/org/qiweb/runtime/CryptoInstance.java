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
import org.qiweb.runtime.util.Hex;

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
        try
        {
            byte[] bytes = new byte[ 32 ];
            SecureRandom.getInstanceStrong().nextBytes( bytes );
            return Hex.encode( bytes );
        }
        catch( NoSuchAlgorithmException ex )
        {
            throw new InternalError( ex.getMessage() );
        }
    }

    @Override
    public String hexHmacSha256( String message )
    {
        return hexHmacSha256( message, secretBytes );
    }

    @Override
    public String hexHmacSha256( String message, String secret )
    {
        return hexHmacSha256( message, Hex.decode( secret.toCharArray() ) );
    }

    private String hexHmacSha256( String message, byte[] secret )
    {
        try
        {
            SecretKeySpec signingKey = new SecretKeySpec( secret, "HmacSHA256" );
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( signingKey );
            byte[] rawHmac = mac.doFinal( message.getBytes( charset ) );
            return Hex.encode( rawHmac );
        }
        catch( NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex )
        {
            throw new QiWebException( "Unable to HMAC message", ex );
        }
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
    public String hexSha256( CharSequence message )
    {
        return Hex.encode( sha256( message ) );
    }

    @Override
    public String base64Sha256( CharSequence message )
    {
        return Base64.getEncoder().encodeToString( sha256( message ) );
    }
}
