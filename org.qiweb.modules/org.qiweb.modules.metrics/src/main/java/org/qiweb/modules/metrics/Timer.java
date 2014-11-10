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
package org.qiweb.modules.metrics;

import com.codahale.metrics.MetricRegistry;
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

import static org.qiweb.util.IllegalArguments.ensureNotEmpty;

/**
 * Timer Metric Annotation.
 * <p>
 * Time each interaction.
 *
 * @see Filter
 */
@FilterWith( Timer.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface Timer
{
    String name();

    /**
     * Timer Metric Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<Timer>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Timer> annotation )
        {
            String metricName = annotation.map( annot -> annot.name() ).get();
            ensureNotEmpty( "Timer name", metricName );

            MetricRegistry metrics = context.application().plugin( Metrics.class ).metrics();
            com.codahale.metrics.Timer.Context timer = metrics.timer( metricName ).time();

            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    timer.close();
                    return outcome;
                }
            );
        }
    }
}
