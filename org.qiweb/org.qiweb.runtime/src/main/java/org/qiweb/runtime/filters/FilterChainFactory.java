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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import org.qiweb.api.Application;
import org.qiweb.api.Global;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.runtime.filters.FilterChainInstance.FilterChainControllerTail;

public class FilterChainFactory
{
    public FilterChain buildFilterChain( Application app, Global global, Context context )
    {
        Set<Class<? extends Filter>> uniqueFilters = new LinkedHashSet<>();
        uniqueFilters.addAll( findFilterWithOnType( context.route().controllerType() ) );
        FilterWith filterWith = context.route().controllerMethod().getAnnotation( FilterWith.class );
        if( filterWith != null )
        {
            uniqueFilters.addAll( Arrays.asList( filterWith.value() ) );
        }
        Stack<Class<? extends Filter>> filtersStack = new Stack<>();
        filtersStack.addAll( uniqueFilters );
        return buildFilterChain( app, global, filtersStack, context );
    }

    private FilterChain buildFilterChain( Application app, Global global, Stack<Class<? extends Filter>> filters, Context context )
    {
        if( filters.isEmpty() )
        {
            return new FilterChainControllerTail( app, global );
        }
        else
        {
            return new FilterChainInstance( app, global, filters.pop(), buildFilterChain( app, global, filters, context ) );
        }
    }

    private Set<Class<? extends Filter>> findFilterWithOnType( Class<?> controllerType )
    {
        Set<Class<? extends Filter>> filters = new LinkedHashSet<>();
        if( controllerType.getSuperclass() != null )
        {
            filters.addAll( findFilterWithOnType( controllerType.getSuperclass() ) );
        }
        if( controllerType.getInterfaces() != null )
        {
            for( Class<?> clazz : controllerType.getInterfaces() )
            {
                filters.addAll( findFilterWithOnType( clazz ) );
            }
        }
        FilterWith filterWith = controllerType.getAnnotation( FilterWith.class );
        if( filterWith != null )
        {
            filters.addAll( Arrays.asList( filterWith.value() ) );
        }
        return filters;
    }
}
