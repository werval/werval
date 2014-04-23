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
import org.qiweb.api.Application;
import org.qiweb.api.Global;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.util.Couple;

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
        public Outcome next( Context context )
        {
            Object controller = global.getControllerInstance( app, context.route().controllerType() );
            return global.invokeControllerMethod( context, controller );
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
    public Outcome next( Context context )
    {
        Filter filter = global.getFilterInstance( app, filterInfo.left() );
        return filter.filter( filterInfo.right(), next, context );
    }
}
