/*
 * Copyright (c) 2014-2015 the original author or authors
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
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.http.Headers;
import io.werval.api.outcomes.Outcome;

/**
 * Accept Content Types Annotation.
 * <p>
 * A request with a content-type not listed will be rejected with a {@literal 400 Bad Request}.
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( AcceptContentTypes.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface AcceptContentTypes
{
    /**
     * @return Accepted content-types, all others will lead to a 400 Bad Request
     */
    String[] value();

    /**
     * Accept Content Types Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<AcceptContentTypes>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<AcceptContentTypes> annotation
        )
        {
            String[] acceptedContentTypes = annotation.map( annot -> annot.value() ).orElse( new String[ 0 ] );
            Optional<String> contentType = context.request().contentType()
                .map( Headers::extractContentMimeType )
                .orElse( Optional.empty() );
            if( contentType.isPresent() )
            {
                if( Arrays.stream( acceptedContentTypes ).anyMatch( ct -> ct.equals( contentType.get() ) ) )
                {
                    return chain.next( context );
                }
                return CompletableFuture.completedFuture(
                    context.outcomes().badRequest().asTextPlain()
                    .withBody( "Unacceptable content-type: `" + contentType.get() + '`' ).build()
                );
            }
            return CompletableFuture.completedFuture(
                context.outcomes().badRequest().asTextPlain()
                .withBody( "Content-Type header must be provided" ).build()
            );
        }
    }
}
