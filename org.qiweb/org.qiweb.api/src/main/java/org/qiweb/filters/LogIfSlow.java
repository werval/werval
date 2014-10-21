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
import org.qiweb.api.http.Request;
import org.qiweb.api.outcomes.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.qiweb.util.Strings.EMPTY;

/**
 * LogIfSlow Annotation.
 * <p>
 * Logs method and URI of requests which take longer than a given duration.
 */
@FilterWith( LogIfSlow.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface LogIfSlow
{
    /**
     * Slow request duration threshold in milliseconds.
     * <p>
     * Default to 1 second.
     *
     * @return Slow request duration threshold in milliseconds
     */
    long threshold() default 1_000L;

    /**
     * LogIfSlow Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<LogIfSlow>
    {
        private final static Logger LOG = LoggerFactory.getLogger( LogIfSlow.class );

        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<LogIfSlow> annotation )
        {
            final long startTime = System.nanoTime();
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    long threshold = MILLISECONDS.toNanos(
                        annotation.map(
                            annot -> annot.threshold()
                        ).orElse(
                            context.application().config().milliseconds( "qiweb.filters.log_if_slow.threshold" )
                        )
                    );
                    long nanos = System.nanoTime() - startTime;
                    if( nanos - threshold >= 0 )
                    {
                        Request request = context.request();
                        LOG.warn(
                            "Slow interaction: {} {} [{}ms]",
                            request.method(),
                            request.uri() + ( request.queryString().isEmpty() ? EMPTY : "?" + request.queryString() ),
                            NANOSECONDS.toMillis( nanos )
                        );
                    }
                    return outcome;
                }
            );
        }
    }
}
