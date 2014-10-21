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
 * X-Content-Type-Options Annotation.
 * <p>
 * The only defined value, "nosniff", prevents browsers from infering the MIME type.
 * <p>
 * <a href="http://msdn.microsoft.com/en-us/library/gg622941%28v=vs.85%29.aspx">MSDN has a good description</a>
 * of how browsers behave when this header is sent.
 * <p>
 * This also applies to Google Chrome, when
 * <a href="https://developer.chrome.com/extensions/hosting">downloading extensions</a>.
 */
@FilterWith( XContentTypeOptions.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface XContentTypeOptions
{
    /**
     * X-Content-Type-Options Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<XContentTypeOptions>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<XContentTypeOptions> annotation
        )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    outcome.responseHeader().headers().withSingle( "X-Content-Type-Options", "nosniff" );
                    return outcome;
                }
            );
        }
    }
}
