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
package org.qiweb.api.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.outcomes.Outcome;

/**
 * Annotation to easily cache controllers outcomes.
 */
@FilterWith( Cached.Filter.class )
@Target(
     {
        ElementType.TYPE, ElementType.METHOD
    } )
@Retention( RetentionPolicy.RUNTIME )
public @interface Cached
{
    /**
     * @return Cache Key
     */
    String key();

    /**
     * @return Time To Live in Cache in Seconds
     */
    int ttl() default 0;

    /**
     * Cached Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<Cached>
    {
        @Override
        public Outcome filter( Cached filterConfig, FilterChain chain, Context context )
        {
            String key = filterConfig.key();
            int ttl = filterConfig.ttl();
            Optional<Object> cached = context.application().cache().getOptional( key );
            if( cached.isPresent() )
            {
                return (Outcome) cached.get();
            }
            Outcome outcome = chain.next( context );
            context.application().cache().set( ttl, key, outcome );
            return outcome;
        }
    }

}
