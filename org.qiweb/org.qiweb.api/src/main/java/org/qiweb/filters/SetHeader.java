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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.outcomes.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.util.Strings.EMPTY;
import static org.qiweb.util.Strings.isEmpty;

/**
 * Set Header.
 */
@FilterWith( SetHeader.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
@Repeatable( SetHeader.Repeat.class )
public @interface SetHeader
{
    /**
     * @return Name of the header to set
     */
    String name();

    /**
     * @return Values to set for the header
     */
    String[] values();

    /**
     * Set Header Filter.
     */
    public static class Filter
        implements org.qiweb.api.filters.Filter<SetHeader>
    {
        private static final Logger LOG = LoggerFactory.getLogger( Filter.class );

        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<SetHeader> annotation )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    String name = annotation.map( annot -> annot.name() ).orElse( EMPTY );
                    String[] values = annotation.map( annot -> annot.values() ).orElse( new String[ 0 ] );
                    if( isEmpty( name ) )
                    {
                        LOG.warn( "@SetHeader name is not set, not setting any header." );
                        return outcome;
                    }
                    if( values.length < 1 )
                    {
                        LOG.warn( "@SetHeader has no values, not setting any header." );
                        return outcome;
                    }
                    outcome.responseHeader().headers().withAll( name, values );
                    return outcome;
                }
            );
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD, ElementType.TYPE } )
    @Inherited
    @Documented
    public static @interface Repeat
    {
        SetHeader[] value();
    }
}
