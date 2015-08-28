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

import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.hasTextOrNull;

/**
 * X-Frame-Options Annotation.
 * <p>
 * <a href="http://tools.ietf.org/html/rfc7034">RFC7034</a> - HTTP Header Field X-Frame-Options
 * <p>
 * By default, values from configuration are used.
 * Values set with the annotation parameters override thoses from the configuration, except if set to an empty string.
 * </p>
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( XFrameOptions.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface XFrameOptions
{
    /**
     * @return {@literal X-Frame-Options} header value, empty to use value from configuration
     */
    String value() default EMPTY;

    /**
     * X-Frame-Options Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<XFrameOptions>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<XFrameOptions> annotation
        )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    outcome.responseHeader().headers().withSingle(
                        "X-Frame-Options",
                        annotation.map(
                            annot -> hasTextOrNull( annot.value() )
                        ).orElse(
                            context.application().config().string( "werval.filters.x_frame_options.value" )
                        )
                    );
                    return outcome;
                }
            );
        }
    }
}
