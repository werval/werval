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
import io.werval.api.Config;
import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;

/**
 * HTTP Strict-Transport-Security Annotation.
 * <p>
 * <a href="https://tools.ietf.org/html/rfc6797">RFC6797</a>.
 * <p>
 * <a href="http://caniuse.com/stricttransportsecurity">Can I use?</a>
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( HSTS.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface HSTS
{
    /**
     * {@literal max-age} directive, in seconds.
     *
     * <p>
     * Don't set this to a too high value in order to prevent privacy issues for your users. See
     * <a href="http://security.stackexchange.com/a/5579/15615">this security.stackexchange answer</a> and
     * <a href="http://www.leviathansecurity.com/blog/the-double-edged-sword-of-hsts-persistence-and-privacy/">The
     * double edged sword of HSTS persistence and privacy</a> for tremendous details.
     * </p>
     * <p>
     * Default to {@literal werval.filters.hsts.max_age} configuration property value.
     * </p>
     *
     * @return The max-age directive value, in seconds
     */
    long maxAge() default -1;

    /**
     * {@literal includeSubDomains} directive.
     *
     * <p>
     * Default to {@literal werval.filters.hsts.include_sub_domains} configuration property value.
     * </p>
     *
     * @return {@literal TRUE} if the {@literal includeSubDomains} directive should be included, {@literal FALSE}
     *         otherwise.
     *         Defaults to {@literal FALSE}.
     */
    boolean includeSubDomains() default false;

    /**
     * HTTP Strict-Transport-Security Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<HSTS>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<HSTS> annotation )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    Config config = context.application().config().object( "werval.filters.hsts" );
                    long maxAge = annotation.map(
                        annot -> annot.maxAge() == -1 ? null : annot.maxAge()
                    ).orElse(
                        config.seconds( "max_age" )
                    );
                    boolean includeSubDomains = annotation.map(
                        annot -> annot.includeSubDomains()
                    ).orElse(
                        config.bool( "include_sub_domains" )
                    );
                    StringBuilder sb = new StringBuilder();
                    sb.append( "max-age=" ).append( maxAge );
                    if( includeSubDomains )
                    {
                        sb.append( "; includeSubDomains" );
                    }
                    outcome.responseHeader().headers().withSingle( "Strict-Transport-Security", sb.toString() );
                    return outcome;
                }
            );
        }
    }
}
