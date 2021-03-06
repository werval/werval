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
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.http.MutableHeaders;
import io.werval.api.http.StatusClass;
import io.werval.api.outcomes.Outcome;

import static io.werval.api.http.Headers.Names.CACHE_CONTROL;
import static io.werval.api.http.Headers.Names.EXPIRES;
import static io.werval.api.http.Headers.Names.PRAGMA;

/**
 * Never Cached Annotation.
 * <blockquote><pre>
 * Cache-Control: no-cache, no-store, max-age=0, must-revalidate
 * Pragma: no-cache
 * Expires: 0
 * </pre></blockquote>
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( NeverCached.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface NeverCached
{
    /**
     * HTTP Status classes for which this filter should be applied.
     * <p>
     * See {@link StatusClass}.
     * <p>
     * Default to all status classes.
     *
     * @return HTTP Status classes for which this filter should be applied
     */
    StatusClass[] value() default {};

    /**
     * Never Cached Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<NeverCached>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<NeverCached> annotation )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    StatusClass[] classes = annotation.map(
                        annot -> annot.value().length == 0 ? StatusClass.values() : annot.value()
                    ).orElse(
                        StatusClass.values()
                    );
                    if( Arrays.asList( classes ).contains( outcome.responseHeader().status().statusClass() ) )
                    {
                        MutableHeaders headers = outcome.responseHeader().headers();
                        headers.withSingle( CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate" );
                        headers.withSingle( PRAGMA, "no-cache" );
                        headers.withSingle( EXPIRES, "0" );
                    }
                    return outcome;
                }
            );
        }
    }
}
