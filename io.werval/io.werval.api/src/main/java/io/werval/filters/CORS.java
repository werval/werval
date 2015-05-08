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
package io.werval.filters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.http.MutableHeaders;
import io.werval.api.outcomes.Outcome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.werval.api.http.Headers.Names.ACCESS_CONTROL_EXPOSE_HEADERS;
import static io.werval.api.http.Headers.Names.ORIGIN;
import static io.werval.util.Iterables.notEmptyOrNull;
import static io.werval.util.Strings.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * CORS Annotation.
 * <p>
 * To be used in conjunction with the {@link io.werval.controllers.CORS} controller.
 */
@FilterWith( CORS.Filter.class )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface CORS
{
    /**
     * Allowed origins.
     *
     * @return Allowed origins
     */
    String[] allowOrigin() default
    {
    };

    /**
     * Allow credentials (cookies, http authentication).
     *
     * @return {@literal true} if credentials are allowed, {@literal false} otherwise
     */
    boolean allowCredentials() default false;

    /**
     * Exposed headers.
     *
     * @return Exposed headers
     */
    String[] exposeHeaders() default
    {
    };

    /**
     * CORS Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<CORS>
    {
        private static final Logger LOG = LoggerFactory.getLogger( CORS.class );

        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<CORS> annotation )
        {
            if( context.request().headers().has( ORIGIN ) )
            {
                String origin = context.request().headers().singleValue( ORIGIN );
                // Is the origin allowed?
                List<String> allowOrigins = annotation.map(
                    annot -> notEmptyOrNull( asList( annot.allowOrigin() ) )
                ).orElse(
                    context.application().config().stringList( "werval.filters.cors.allow_origin" )
                ).stream()
                    .map( String::trim )
                    .collect( toList() );
                if( allowOrigins.contains( "*" ) || allowOrigins.contains( origin ) )
                {
                    // Are credentials allowed?
                    boolean allowCredentials = annotation.map(
                        annot -> annot.allowCredentials()
                    ).orElse(
                        context.application().config().bool( "werval.filters.cors.allow_credentials" )
                    ) && !allowOrigins.contains( "*" );
                    // Exposed headers
                    List<String> exposeHeaders = annotation.map(
                        annot -> notEmptyOrNull( asList( annot.exposeHeaders() ) )
                    ).orElse(
                        context.application().config().stringList( "werval.filters.cors.expose_headers" )
                    );
                    // Set response headers
                    String allowCredentialsValue = String.valueOf( allowCredentials );
                    String exposeHeadersValue = exposeHeaders.isEmpty() ? null : join( exposeHeaders, ", " );
                    if( LOG.isTraceEnabled() )
                    {
                        if( exposeHeadersValue == null )
                        {
                            LOG.trace(
                                "CORS actual request with headers {}: {} ; {}: {}",
                                ACCESS_CONTROL_ALLOW_ORIGIN, origin,
                                ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentialsValue
                            );
                        }
                        else
                        {
                            LOG.trace(
                                "CORS actual request with headers {}: {} ; {}: {} ; {}: {}",
                                ACCESS_CONTROL_ALLOW_ORIGIN, origin,
                                ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentialsValue,
                                ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeadersValue
                            );
                        }
                    }
                    MutableHeaders headers = context.response().headers();
                    headers.withSingle( ACCESS_CONTROL_ALLOW_ORIGIN, origin );
                    headers.withSingle( ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentialsValue );
                    if( exposeHeadersValue != null )
                    {
                        headers.withSingle( ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeadersValue );
                    }
                }
            }
            return chain.next( context );
        }
    }
}
