/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.runtime.filters;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.qiweb.api.Application;
import org.qiweb.api.Global;
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.util.Couple;

/**
 * Instance of FilterChain.
 */
/* package */ class FilterChainInstance
    implements FilterChain
{
    /**
     * End of the FilterChain, this is the Controller Method Invocation.
     */
    /* package */ static class FilterChainControllerTail
        implements FilterChain
    {
        private final Application app;
        private final Global global;

        /* package */ FilterChainControllerTail( Application app, Global global )
        {
            this.app = app;
            this.global = global;
        }

        @Override
        public CompletableFuture<Outcome> next( Context context )
        {
            Object controller = global.getControllerInstance( app, context.route().controllerType() );
            Object result = global.invokeControllerMethod( context, controller );
            if( CompletableFuture.class.isAssignableFrom( result.getClass() ) )
            {
                try
                {
                    return (CompletableFuture<Outcome>) result;
                }
                catch( ClassCastException ex )
                {
                    throw new QiWebException(
                        context.route().controllerMethod()
                        + " returned a CompletableFuture of something else than Outcome. Check your code.",
                        ex
                    );
                }
            }
            if( Outcome.class.isAssignableFrom( result.getClass() ) )
            {
                return CompletableFuture.completedFuture( (Outcome) result );
            }
            throw new QiWebException(
                context.route().controllerMethod()
                + " did not return an Outcome nor a CompletableFuture<Outcome> but a "
                + result.getClass()
                + ". Check your code."
            );
        }
    }

    private final Application app;
    private final Global global;
    private final Couple<Class<? extends Filter>, Annotation> filterInfo;
    private final FilterChain next;

    /* package */ FilterChainInstance(
        Application app, Global global, Couple<Class<? extends Filter>, Annotation> filterInfo, FilterChain next
    )
    {
        this.app = app;
        this.global = global;
        this.filterInfo = filterInfo;
        this.next = next;
    }

    @Override
    public CompletableFuture<Outcome> next( Context context )
    {
        Filter filter = global.getFilterInstance( app, filterInfo.left() );
        return filter.filter( next, context, Optional.ofNullable( filterInfo.right() ) );
    }
}
