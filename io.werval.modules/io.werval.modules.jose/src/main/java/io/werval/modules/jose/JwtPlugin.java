/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.modules.jose;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;
import io.werval.modules.jose.internal.Issuer;
import io.werval.modules.metrics.Metrics;
import io.werval.util.Hex;
import java.security.Key;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.HmacKey;

import static java.util.Collections.EMPTY_LIST;

/**
 * JSON Web Token Plugin.
 */
public class JwtPlugin
    implements Plugin<JWT>
{
    private JWT jwt;

    @Override
    public Class<JWT> apiType()
    {
        return JWT.class;
    }

    @Override
    public JWT api()
    {
        return jwt;
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "jose.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        String defaultIssuer = config.string( "jose.default_issuer" );
        Map<String, Issuer> issuers = new LinkedHashMap<>();
        Config issuersConfig = config.atPath( "jose.issuers" );
        for( String issuerId : issuersConfig.subKeys() )
        {
            Config issuerConfig = issuersConfig.atKey( issuerId );
            String dn = issuerConfig.string( "name" );
            String type = issuerConfig.has( "type" ) ? issuerConfig.string( "type" ) : "jws";
            byte[] keyBytes = issuerConfig.has( "key" )
                              ? Hex.decode( issuerConfig.string( "key" ).toCharArray() )
                              : application.crypto().secret();
            Key key;
            String algorithm;
            switch( type.toLowerCase( Locale.US ) )
            {
                case "jwe":
                    throw new UnsupportedOperationException( "JWE Not Implemented Yet!" );
                case "jws":
                default:
                    key = new HmacKey( keyBytes );
                    algorithm = AlgorithmIdentifiers.HMAC_SHA256;
            }
            issuers.put(
                issuerId,
                new Issuer(
                    dn,
                    key,
                    issuerConfig.has( "key_id" ) ? issuerConfig.string( "key_id" ) : dn,
                    algorithm,
                    issuerConfig.has( "not_before" ) ? issuerConfig.seconds( "not_before" ) : 300L,
                    issuerConfig.has( "expiration" ) ? issuerConfig.seconds( "expiration" ) : 300L
                )
            );
        }
        jwt = new JWT(
            defaultIssuer,
            issuers,
            config.bool( "jose.metrics" ) ? application.plugin( Metrics.class ) : null
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        jwt = null;
    }
}
