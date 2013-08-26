/**
 * Copyright (c) 2013 the original author or authors
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

import org.qiweb.api.Global;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;

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

        private final Global global;

        /* package */ FilterChainControllerTail( Global global )
        {
            this.global = global;
        }

        @Override
        public Outcome next( Context context )
        {
            Object controller = global.getControllerInstance( context.route().controllerType() );
            return global.controllerMethodInvocation().invoke( context, controller );
        }
    }
    private final Global global;
    private final Class<? extends Filter> filterType;
    private final FilterChain next;

    /* package */ FilterChainInstance( Global global, Class<? extends Filter> filterType, FilterChain next )
    {
        this.global = global;
        this.filterType = filterType;
        this.next = next;
    }

    @Override
    public Outcome next( Context context )
    {
        Filter filter = global.getFilterInstance( filterType );
        return filter.filter( next, context );
    }
}
