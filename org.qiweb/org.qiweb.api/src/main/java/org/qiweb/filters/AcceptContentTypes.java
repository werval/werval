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
package org.qiweb.filters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.outcomes.Outcome;

/**
 * AcceptContentTypes.
 * <p>
 * A request with a content-type not listed will be rejected with a {@literal 400 Bad Request}.
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
     * AcceptContentTypes Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<AcceptContentTypes>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<AcceptContentTypes> annotation
        )
        {
            String[] acceptedContentTypes = annotation.map( annot -> annot.value() ).orElse( new String[ 0 ] );
            boolean accepted = false;
            String requestContentType = context.request().contentType();
            for( String acceptedContentType : acceptedContentTypes )
            {
                if( acceptedContentType.equals( requestContentType ) )
                {
                    accepted = true;
                    break;
                }
            }
            if( !accepted )
            {
                return CompletableFuture.completedFuture(
                    context.outcomes()
                    .badRequest()
                    .withBody( "Unacceptable content-type: " + requestContentType )
                    .asTextPlain()
                    .build()
                );
            }
            return chain.next( context );
        }
    }
}
