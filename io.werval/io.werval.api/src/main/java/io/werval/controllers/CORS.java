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
package io.werval.controllers;

import io.werval.api.outcomes.Outcome;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_METHODS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_REQUEST_HEADERS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_REQUEST_METHOD;
import static io.werval.api.http.Headers.Names.ORIGIN;
import static io.werval.api.http.Method.OPTIONS;
import static io.werval.util.Strings.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * CORS controller for preflight requests.
 * <p>
 * To be used in conjunction with the {@link io.werval.filters.CORS} filter annotation.
 * <p>
 * Some typical usages in {@literal routes.conf}:
 * <pre>
 * OPTIONS  /*catchall                  CORS.preflight( String catchall )
 * OPTIONS  /single-resource            CORS.preflight
 * OPTIONS  /variable/:path/resources   CORS.preflight( String path )
 * OPTIONS  /custom/cors                CORS.preflight( String origin = 'http://example.com', \
 *                                                      String methods = 'GET', \
 *                                                      String headers = 'User-Agent', \
 *                                                      Boolean creds = true )
 * </pre>
 * <p>
 * The only thing missing from the spec is support for custom {@literal Access-Control-Max-Age}.
 */
public class CORS
{
    private static final Logger LOG = LoggerFactory.getLogger( CORS.class );
    private static final String MSG_INVALID_METHOD = "{} is not a valid CORS preflight method, only OPTIONS is allowed";
    private static final String MSG_INVALID_PREFLIGHT = "Invalid CORS preflight request, " + ORIGIN + " and "
                                                        + ACCESS_CONTROL_REQUEST_METHOD + " are mandatory";
    private static final String MSG_UNAUTHORIZED_ORIGIN = "Unauthorized CORS origin: {}";
    private static final String MSG_UNAUTHORIZED_METHOD = "Unauthorized CORS method: {}";
    private static final String MSG_UNAUTHORIZED_HEADERS = "Unauthorized CORS headers: {}";
    private static final String MSG_PREFLIGHT_RESULT = "CORS preflight result: {}: {} ; {}: {} ; {}: {} ; {}: {}";

    public Outcome preflight( String path )
    {
        return preflight();
    }

    public Outcome preflight()
    {
        return preflight(
            application().config().stringList( "werval.controllers.cors.allow_origin" ),
            application().config().stringList( "werval.controllers.cors.allow_methods" ),
            application().config().stringList( "werval.controllers.cors.allow_headers" ),
            application().config().bool( "werval.controllers.cors.allow_credentials" )
        );
    }

    public Outcome preflight(
        String path,
        String allowOrigin,
        String allowMethods,
        String allowHeaders,
        Boolean allowCredentials
    )
    {
        return preflight( allowOrigin, allowMethods, allowHeaders, allowCredentials );
    }

    public Outcome preflight(
        String allowOrigin,
        String allowMethods,
        String allowHeaders,
        Boolean allowCredentials
    )
    {
        return preflight(
            asList( allowOrigin.split( "," ) ),
            asList( allowMethods.split( "," ) ),
            asList( allowHeaders.split( "," ) ),
            allowCredentials
        );
    }

    private Outcome preflight(
        List<String> allowOrigin,
        List<String> allowMethods,
        List<String> allowHeaders,
        Boolean allowCredentials
    )
    {
        if( !OPTIONS.equals( request().method() ) )
        {
            LOG.warn( MSG_INVALID_METHOD, request().method() );
            return outcomes().badRequest().build();
        }
        // Is this a valid preflight request?
        if( !request().headers().has( ORIGIN )
            || !request().headers().has( ACCESS_CONTROL_REQUEST_METHOD ) )
        {
            LOG.warn( MSG_INVALID_PREFLIGHT );
            return outcomes().badRequest().build();
        }
        // Is the origin allowed?
        String requestedOrigin = request().headers().singleValue( ORIGIN );
        List<String> allowedOrigins = allowOrigin.stream().map( String::trim ).collect( toList() );
        if( !allowedOrigins.contains( "*" ) && !allowedOrigins.contains( requestedOrigin ) )
        {
            LOG.warn( MSG_UNAUTHORIZED_ORIGIN, requestedOrigin );
            return outcomes().unauthorized().build();
        }
        // Is the requested method allowed?
        String requestedMethod = request().headers().singleValue( ACCESS_CONTROL_REQUEST_METHOD );
        List<String> allowedMethods = allowMethods.stream()
            .map( s -> s.trim().toUpperCase( Locale.US ) )
            .collect( toList() );
        if( !allowedMethods.contains( requestedMethod ) )
        {
            LOG.warn( MSG_UNAUTHORIZED_METHOD, requestedMethod );
            return outcomes().unauthorized().build();
        }
        // Are the requested headers allowed?
        if( request().headers().has( ACCESS_CONTROL_REQUEST_HEADERS ) )
        {
            List<String> allowedHeaders = allowHeaders.stream()
                .map( s -> s.trim().toLowerCase( Locale.US ) )
                .collect( toList() );
            List<String> requestedHeaders = asList(
                join( request().headers().values( ACCESS_CONTROL_REQUEST_HEADERS ), ", " ).split( "," )
            ).stream()
                .map( s -> s.trim().toLowerCase( Locale.US ) )
                .collect( toList() );
            if( !allowedHeaders.containsAll( requestedHeaders ) )
            {
                LOG.warn( MSG_UNAUTHORIZED_HEADERS, requestedHeaders );
                return outcomes().unauthorized().build();
            }
        }
        // Build preflight response
        String allowOriginValue = join( allowOrigin, ", " );
        String allowMethodsValue = join( allowMethods, ", " );
        String allowHeadersValue = join( allowHeaders, ", " );
        String allowCredentialsValue = String.valueOf(
            allowCredentials != null && allowCredentials && !allowOrigin.contains( "*" )
        );
        LOG.trace(
            MSG_PREFLIGHT_RESULT,
            ACCESS_CONTROL_ALLOW_ORIGIN, allowOriginValue,
            ACCESS_CONTROL_ALLOW_METHODS, allowMethodsValue,
            ACCESS_CONTROL_ALLOW_HEADERS, allowHeadersValue,
            ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentialsValue
        );
        return outcomes()
            .noContent()
            .withHeader( ACCESS_CONTROL_ALLOW_ORIGIN, allowOriginValue )
            .withHeader( ACCESS_CONTROL_ALLOW_METHODS, allowMethodsValue )
            .withHeader( ACCESS_CONTROL_ALLOW_HEADERS, allowHeadersValue )
            .withHeader( ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentialsValue )
            .build();
    }
}
