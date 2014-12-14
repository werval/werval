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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;

/**
 * Remove Headers Annotation.
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( RemoveHeaders.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface RemoveHeaders
{
    /**
     * @return Names of headers to remove from the Outcome
     */
    String[] value();

    /**
     * Remove Headers Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<RemoveHeaders>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<RemoveHeaders> annotation
        )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    String[] blacklist = annotation.map( annot -> annot.value() ).orElse( new String[ 0 ] );
                    for( String blacklisted : blacklist )
                    {
                        outcome.responseHeader().headers().without( blacklisted );
                    }
                    return outcome;
                }
            );
        }
    }
}
