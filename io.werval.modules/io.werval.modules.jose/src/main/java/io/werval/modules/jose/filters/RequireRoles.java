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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * RequireRoles Filter Annotation.
 */
@RequireSubject
@FilterWith( RequireRoles.Filter.class )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface RequireRoles
{
    String[] value();

    /**
     * RequireRoles Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<RequireRoles>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain, Context context, Optional<RequireRoles> annotation
        )
        {
            String[] requiredRoles = annotation.map( annot -> annot.value() ).orElse( new String[ 0 ] );
            if( requiredRoles.length > 0 )
            {
                Map<String, Object> claims = context.metaData().get( Map.class, JWT.CLAIMS_METADATA_KEY );
                if( !claims.containsKey( "roles" )
                    || !( claims.get( "roles" ) instanceof Collection )
                    || ( (Collection) claims.get( "roles" ) ).containsAll( Arrays.asList( requiredRoles ) ) )
                {
                    return CompletableFuture.completedFuture( context.outcomes().unauthorized().build() );
                }
            }
            return chain.next( context );
        }
    }
}
