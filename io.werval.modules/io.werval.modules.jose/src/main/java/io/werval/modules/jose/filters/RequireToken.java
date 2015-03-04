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
package io.werval.modules.jose.filters;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.modules.jose.JWT;
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
 * RequireToken Filter Annotation.
 */
@FilterWith( RequireToken.Filter.class )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface RequireToken
{
    /**
     * RequireToken Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<RequireToken>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<RequireToken> annotation )
        {
            String jwtHeader = context.application().config().string( JWT.HTTP_HEADER_CONFIG_KEY );
            if( !context.request().headers().has( jwtHeader ) )
            {
                return CompletableFuture.completedFuture( context.outcomes().unauthorized().build() );
            }
            String token = context.request().headers().singleValue( jwtHeader );
            Map<String, Object> claims = context.application().plugin( JWT.class ).claimsOfToken( token );
            context.metaData().put( JWT.TOKEN_METADATA_KEY, token );
            context.metaData().put( JWT.CLAIMS_METADATA_KEY, claims );
            return chain.next( context );
        }
    }
}
