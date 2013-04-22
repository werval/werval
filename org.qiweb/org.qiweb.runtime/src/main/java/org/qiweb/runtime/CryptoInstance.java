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
