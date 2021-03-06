/*
 * Copyright (c) 2014 the original author or authors
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.util.Dates;
import io.werval.util.Strings;

import static io.werval.api.http.Headers.Names.ETAG;
import static io.werval.api.http.Headers.Names.EXPIRES;
import static io.werval.api.http.Headers.Names.IF_NONE_MATCH;

/**
 * Cached Annotation.
 * <p>
 * The annotation can be used on controllers and on their methods.
 * <p>
 * Leverage both server-side and client-side caching mechanisms:
 * <ul>
 * <li>set the {@literal Expires} and {@literal Etag} headers</li>
 * <li>handle the {@literal If-None-Match} header and return {@literal 304 Not Modified} ;</li>
 * <li>cache the {@link Outcome} in the Application {@link io.werval.api.cache.Cache}.</li>
 * </ul>
 * Using {@link #ttl()} you can set how long the data will be cached.
 * <p>
 * Using {@link #vary()} you can set the request headers that make the response vary, see the {@literal Vary} header
 * documentation.
 * <p>
 * The cache key is generated using the current {@literal Route}, the request parameters and the values of all
 * variable headers declared using {@link #vary()}.
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( Cached.Filter.class )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface Cached
{
    /**
     * @return Time To Live in Cache in seconds, defaults to 0, will never expire
     */
    int ttl() default 0;

    /**
     * @return {@literal Vary} HTTP Headers, defaults to none
     */
    String[] vary() default "";

    /**
     * Cached Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<Cached>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Cached> annotation )
        {
            String key = key( context, annotation.get().vary() );
            String etagKey = key + "-etag";

            Optional<String> requestEtag = context.request().headers().singleValueOptional( IF_NONE_MATCH );
            String cacheEtag = context.application().cache().get( etagKey );
            if( requestEtag.isPresent()
                && ( "*".equals( requestEtag.get() ) || Objects.equals( cacheEtag, requestEtag.get() ) ) )
            {
                return CompletableFuture.completedFuture( context.outcomes().notModified().build() );
            }

            Optional<Object> cached = context.application().cache().getOptional( key );
            if( cached.isPresent() )
            {
                return CompletableFuture.completedFuture( (Outcome) cached.get() );
            }

            CompletableFuture<Outcome> futureOutcome = chain.next( context );

            return futureOutcome.thenApplyAsync(
                outcome ->
                {
                    int ttl = annotation.get().ttl();
                    String expiration = Dates.HTTP.format(
                        System.currentTimeMillis() + ( ttl == 0 ? 1000 * 60 * 60 * 24 * 365 : ttl * 1000 )
                    );
                    String etag = expiration;

                    outcome.responseHeader().headers().withSingle( EXPIRES, expiration );
                    outcome.responseHeader().headers().withSingle( ETAG, etag );

                    context.application().cache().set( ttl, key, outcome );
                    context.application().cache().set( ttl, etagKey, etag );

                    return outcome;
                },
                context.executor()
            );
        }

        private String key( Context context, String[] vary )
        {
            StringBuilder sb = new StringBuilder( context.route().toString() );
            sb.append( context.request().parameters().toString() );
            for( String varyHeader : vary )
            {
                if( Strings.hasText( varyHeader ) )
                {
                    List<String> variables = context.request().headers().values( varyHeader );
                    if( !variables.isEmpty() )
                    {
                        sb.append( "_" );
                        Iterator<String> vit = variables.iterator();
                        while( vit.hasNext() )
                        {
                            sb.append( vit.next() );
                            if( vit.hasNext() )
                            {
                                sb.append( "-" );
                            }
                        }
                    }
                }
            }
            return context.application().crypto().sha256Base64( sb.toString() );
        }
    }
}
