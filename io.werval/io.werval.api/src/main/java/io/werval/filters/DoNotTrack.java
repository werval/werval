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
import io.werval.api.http.Headers;
import io.werval.api.outcomes.Outcome;

/**
 * Do Not Track Annotation.
 * <p>
 * Set a Context MetaData boolean value indicating whether the client advertised a Do-Not-Track header using the
 * {@literal DNT} key.
 * <p>
 * See the <a href="http://www.w3.org/TR/tracking-dnt/">Tracking Preference Expression (DNT)</a> working draft at IETF.
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( DoNotTrack.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface DoNotTrack
{
    /**
     * @return {@literal TRUE} if OPT-IN should be advertised
     */
    boolean optIn() default false;

    /**
     * Context MetaData key for a boolean value indicating whether the client advertised a Do-Not-Track header.
     */
    String DNT = "DNT";

    /**
     * Do Not Track Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<DoNotTrack>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<DoNotTrack> annotation )
        {
            Headers requestHeaders = context.request().headers();
            if( requestHeaders.has( DNT ) )
            {
                context.metaData().put(
                    DNT,
                    "1".equals( requestHeaders.singleValue( DNT ) )
                );
            }
            else
            {
                context.metaData().put( DNT, false );
            }
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    boolean optIn = annotation.map(
                        annot -> annot.optIn() ? true : null
                    ).orElse(
                        context.application().config().bool( "werval.filters.dnt.opt_in" )
                    );
                    outcome.responseHeader().headers().withSingle( DNT, optIn ? "0" : "1" );
                    return outcome;
                }
            );
        }
    }
}
