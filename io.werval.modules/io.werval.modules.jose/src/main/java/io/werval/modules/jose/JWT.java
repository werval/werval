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

import io.werval.api.exceptions.WervalException;
import io.werval.modules.jose.internal.Issuer;
import io.werval.modules.metrics.Metrics;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

/**
 * JSON Web Token.
 */
// See https://auth0.com/blog/2014/01/27/ten-things-you-should-know-about-tokens-and-cookies/
public class JWT
{
    public static final String HTTP_HEADER_CONFIG_KEY = "jose.jwt.http_header";
    public static final String TOKEN_METADATA_KEY = "JWT";
    public static final String CLAIMS_METADATA_KEY = "JWT-Claims";
    public static final String CLAIM_ISSUER = "iss";
    public static final String CLAIM_SUBJECT = "sub";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ISSUED_AT = "iat";
    public static final String CLAIM_NOT_BEFORE = "nbf";
    public static final String CLAIM_EXPIRATION = "exp";
    public static final String METRIC_ISSUED_TOKENS = "io.werval.modules.jose.issued-tokens";
    public static final String METRIC_VALIDATED_TOKENS = "io.werval.modules.jose.validated-tokens";
    public static final String METRIC_RENEWED_TOKENS = "io.werval.modules.jose.renewed-tokens";
    public static final String METRIC_TOKEN_ISSUANCE_ERRORS = "io.werval.modules.jose.token-issuance-errors";
    public static final String METRIC_TOKEN_VALIDATION_ERRORS = "io.werval.modules.jose.token-validation-errors";
    public static final String METRIC_TOKEN_RENEWAL_ERRORS = "io.werval.modules.jose.token-renewal-errors";

    private final String defaultIssuer;
    private final Map<String, Issuer> issuers;
    private final Metrics metrics;

    /* package */ JWT( String defaultIssuer, Map<String, Issuer> issuers, Metrics metrics )
    {
        this.defaultIssuer = defaultIssuer;
        this.issuers = issuers;
        this.metrics = metrics;
    }

    public String tokenForClaims( Map<String, Object> claims )
    {
        if( !claims.containsKey( CLAIM_SUBJECT ) )
        {
            throw new IllegalArgumentException( "Cannot issue token without a subject claim ('sub')!" );
        }

        // Token issuer
        Issuer issuer = issuers.get( defaultIssuer );

        // Prepare claims
        Map<String, Object> actualClaims = new LinkedHashMap<>( claims );
        actualClaims.put( CLAIM_ISSUER, issuer.dn() );
        setTimeRelatedClaimsIfAbsent( issuer, actualClaims );

        // Create token
        try
        {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setKey( issuer.key() );
            jws.setKeyIdHeaderValue( issuer.keyId() );
            jws.setAlgorithmHeaderValue( issuer.algorithm() );
            jws.setPayload( JsonUtil.toJson( actualClaims ) );
            String jwt = jws.getCompactSerialization();
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_ISSUED_TOKENS ).mark();
            }
            return jwt;
        }
        catch( JoseException ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_ISSUANCE_ERRORS ).mark();
            }
            throw new WervalException( "Unable to issue JSON Web Token", ex );
        }
        catch( Exception ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_ISSUANCE_ERRORS ).mark();
            }
            throw ex;
        }
    }

    public Map<String, Object> claimsOfToken( String token )
    {
        // Token issuer
        Issuer issuer = issuers.get( defaultIssuer );

        try
        {
            // Validate token signature
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization( token );
            jws.setKey( issuer.key() );
            if( !jws.verifySignature() )
            {
                throw new WervalException( "JSON Web Token signature verification failed" );
            }

            // Extract claims
            Map<String, Object> claims = JsonUtil.parseJson( jws.getPayload() );

            // Validate token not-before/expiration
            ZoneId utc = ZoneId.of( "UTC" );
            ZonedDateTime nowUtc = ZonedDateTime.now( utc );
            if( claims.get( CLAIM_NOT_BEFORE ) != null
                && ZonedDateTime.ofInstant( Instant.ofEpochSecond( (Long) claims.get( CLAIM_NOT_BEFORE ) ), utc )
                .isAfter( nowUtc ) )
            {
                throw new WervalException( "JSON Web Token is not valid yet!" );
            }
            if( claims.get( CLAIM_EXPIRATION ) != null
                && ZonedDateTime.ofInstant( Instant.ofEpochSecond( (Long) claims.get( CLAIM_EXPIRATION ) ), utc )
                .isBefore( nowUtc ) )
            {
                throw new WervalException( "JSON Web Token has expired!" );
            }
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_VALIDATED_TOKENS ).mark();
            }

            // Claims
            return Collections.unmodifiableMap( claims );
        }
        catch( JoseException ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_VALIDATION_ERRORS ).mark();
            }
            throw new WervalException( "JSON Web Token validation failed", ex );
        }
        catch( Exception ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_VALIDATION_ERRORS ).mark();
            }
            throw ex;
        }
    }

    public String renewToken( String token )
    {
        // Token issuer
        Issuer issuer = issuers.get( defaultIssuer );
        try
        {
            // Validate token signature
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization( token );
            jws.setKey( issuer.key() );
            if( !jws.verifySignature() )
            {
                throw new WervalException( "JSON Web Token signature verification failed" );
            }

            // Extract claims, remove all time related ones and set them afresh
            Map<String, Object> claims = JsonUtil.parseJson( jws.getPayload() );
            clearTimeRelatedClaims( claims );
            setTimeRelatedClaimsIfAbsent( issuer, claims );

            // Create renewed token
            jws = new JsonWebSignature();
            jws.setKey( issuer.key() );
            jws.setKeyIdHeaderValue( issuer.keyId() );
            jws.setAlgorithmHeaderValue( issuer.algorithm() );
            jws.setPayload( JsonUtil.toJson( claims ) );
            String jwt = jws.getCompactSerialization();
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_RENEWED_TOKENS ).mark();
            }
            return jwt;
        }
        catch( JoseException ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_RENEWAL_ERRORS ).mark();
            }
            throw new WervalException( "JSON Web Token renewal failed", ex );
        }
        catch( Exception ex )
        {
            if( metrics != null )
            {
                metrics.metrics().meter( METRIC_TOKEN_RENEWAL_ERRORS ).mark();
            }
            throw ex;
        }
    }

    private void clearTimeRelatedClaims( Map<String, Object> claims )
    {
        claims.remove( CLAIM_ISSUED_AT );
        claims.remove( CLAIM_NOT_BEFORE );
        claims.remove( CLAIM_EXPIRATION );
    }

    private void setTimeRelatedClaimsIfAbsent( Issuer issuer, Map<String, Object> claims )
    {
        ZonedDateTime nowUtc = ZonedDateTime.now( ZoneId.of( "UTC" ) );
        // Issued at
        if( claims.get( CLAIM_ISSUED_AT ) == null )
        {
            claims.put( CLAIM_ISSUED_AT, nowUtc.toEpochSecond() );
        }
        // Not before
        if( claims.get( CLAIM_NOT_BEFORE ) == null )
        {
            ZonedDateTime nbf = nowUtc.minusSeconds( issuer.notBeforeSeconds() );
            claims.put( CLAIM_NOT_BEFORE, nbf.toEpochSecond() );
        }
        // Expiration
        if( claims.get( CLAIM_EXPIRATION ) == null )
        {
            ZonedDateTime exp = nowUtc.plusSeconds( issuer.expirationSeconds() );
            claims.put( CLAIM_EXPIRATION, exp.toEpochSecond() );
        }
    }
}
