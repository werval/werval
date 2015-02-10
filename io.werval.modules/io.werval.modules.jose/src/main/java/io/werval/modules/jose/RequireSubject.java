/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.modules.jose;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.util.Strings;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * RequireSubject Filter Annotation.
 */
@RequireToken
@FilterWith( RequireSubject.Filter.class )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface RequireSubject
{
    /**
     * RequireSubject Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<RequireSubject>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain, Context context, Optional<RequireSubject> annotation
        )
        {
            Map<String, Object> claims = context.metaData().get( Map.class, JWT.CLAIMS_METADATA_KEY );
            if( !claims.containsKey( JWT.CLAIM_SUBJECT )
                || !( claims.get( JWT.CLAIM_SUBJECT ) instanceof String )
                || Strings.isEmpty( (String) claims.get( JWT.CLAIM_SUBJECT ) ) )
            {
                return CompletableFuture.completedFuture( context.outcomes().unauthorized().build() );
            }
            return chain.next( context );
        }
    }
}
