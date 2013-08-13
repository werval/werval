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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.qiweb.api.Crypto;

import static io.netty.util.CharsetUtil.*;

public class CryptoInstance
    implements Crypto
{

    private final byte[] keyBytes;

    public CryptoInstance( String secret )
    {
        this.keyBytes = secret.getBytes( UTF_8 );
    }

    @Override
    public String hexHmacSha1( String message )
    {
        try
        {
            SecretKeySpec signingKey = new SecretKeySpec( keyBytes, "HmacSHA1" );

            Mac mac = Mac.getInstance( "HmacSHA1" );
            mac.init( signingKey );

            byte[] rawHmac = mac.doFinal( message.getBytes( UTF_8 ) );

            // To Hex
            StringBuilder sb = new StringBuilder();
            for( byte b : rawHmac )
            {
                sb.append( String.format( "%02x", b ) );
            }
            return sb.toString();
        }
        catch( NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e )
        {
            throw new RuntimeException( e );
        }
    }
}
