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
import java.util.Arrays;
import java.util.List;

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
import static java.util.stream.Collectors.toList;

/**
 * CORS controller for preflight requests.
 * <p>
 * To be used in conjunction with the {@link io.werval.filters.CORS} filter annotation.
 * <p>
 * Typical routing for catching all paths:
 * <pre>
 * OPTIONS /*catchall CORS.preflight( String catchall )
 * </pre>
 */
public class CORS
{
    public Outcome preflight( String path )
    {
        return preflight();
    }

    public Outcome preflight()
    {
        return preflight(
            application().config().string( "werval.controllers.cors.allow_origin" ),
            application().config().string( "werval.controllers.cors.allow_methods" ),
            application().config().string( "werval.controllers.cors.allow_headers" ),
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
        if( !OPTIONS.equals( request().method() ) )
        {
            return outcomes().badRequest()
                .withBody( request().method() + " is not a valid CORS preflight method, only OPTIONS is allowed" )
                .asTextPlain()
                .build();
        }
        // Is this a valid preflight request?
        if( !request().headers().has( ORIGIN )
            || !request().headers().has( ACCESS_CONTROL_REQUEST_METHOD ) )
        {
            return outcomes().badRequest().withBody( "Invalid CORS preflight request." ).asTextPlain().build();
        }
        // Is the origin allowed?
        List<String> allowedOrigins = Arrays.asList( allowOrigin.split( "," ) ).stream()
            .map( String::trim )
            .collect( toList() );
        if( !allowedOrigins.contains( "*" )
            && !allowedOrigins.contains( request().headers().singleValue( ORIGIN ) ) )
        {
            return outcomes().unauthorized().withBody( "Unauthorized CORS origin." ).asTextPlain().build();
        }
        // Is the requested method allowed?
        List<String> allowedMethods = Arrays.asList( allowMethods.split( "," ) ).stream()
            .map( String::trim )
            .collect( toList() );
        if( !allowedMethods.contains( request().headers().singleValue( ACCESS_CONTROL_REQUEST_METHOD ) ) )
        {
            return outcomes().unauthorized().withBody( "Unauthorized CORS method." ).asTextPlain().build();
        }
        // Are the requested headers allowed?
        if( request().headers().has( ACCESS_CONTROL_REQUEST_HEADERS ) )
        {
            List<String> allowedHeaders = Arrays.asList( allowHeaders.split( "," ) ).stream()
                .map( String::trim )
                .collect( toList() );
            List<String> requestedHeaders = Arrays.asList(
                join( request().headers().values( ACCESS_CONTROL_REQUEST_HEADERS ), "," ).split( "," )
            ).stream()
                .map( String::trim )
                .collect( toList() );
            if( !allowedHeaders.containsAll( requestedHeaders ) )
            {
                return outcomes().unauthorized().withBody( "Unauthorized CORS headers." ).asTextPlain().build();
            }
        }
        // Build preflight response
        return outcomes()
            .noContent()
            .withHeader( ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin )
            .withHeader( ACCESS_CONTROL_ALLOW_METHODS, allowMethods )
            .withHeader( ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders )
            .withHeader(
                ACCESS_CONTROL_ALLOW_CREDENTIALS,
                String.valueOf( allowCredentials != null && allowCredentials && !"*".equals( allowOrigin ) )
            )
            .build();
    }
}
