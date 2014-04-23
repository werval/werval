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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.qiweb.api.Application;
import org.qiweb.api.Global;
import org.qiweb.api.context.Context;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.util.Couple;
import org.qiweb.runtime.filters.FilterChainInstance.FilterChainControllerTail;

/**
 * FilterChain Factory.
 */
public class FilterChainFactory
{
    public FilterChain buildFilterChain( Application app, Global global, Context context )
    {
        Set<Couple<Class<? extends Filter>, Annotation>> uniqueFilters = new LinkedHashSet<>();
        uniqueFilters.addAll( findFiltersOnType( context.route().controllerType() ) );
        uniqueFilters.addAll( findFilters( context.route().controllerMethod().getAnnotations() ) );
        Stack<Couple<Class<? extends Filter>, Annotation>> filtersStack = new Stack<>();
        filtersStack.addAll( uniqueFilters );
        return buildFilterChain( app, global, filtersStack, context );
    }

    private List<Couple<Class<? extends Filter>, Annotation>> findFiltersOnType( Class<?> controllerType )
    {
        List<Couple<Class<? extends Filter>, Annotation>> filters = new ArrayList<>();
        if( controllerType.getSuperclass() != null )
        {
            filters.addAll( findFiltersOnType( controllerType.getSuperclass() ) );
        }
        if( controllerType.getInterfaces() != null )
        {
            for( Class<?> clazz : controllerType.getInterfaces() )
            {
                filters.addAll( findFiltersOnType( clazz ) );
            }
        }
        filters.addAll( findFilters( controllerType.getAnnotations() ) );
        return filters;
    }

    private List<Couple<Class<? extends Filter>, Annotation>> findFilters( Annotation[] annotations )
    {
        List<Couple<Class<? extends Filter>, Annotation>> filters = new ArrayList<>();
        for( Annotation annotation : annotations )
        {
            if( FilterWith.class.equals( annotation.annotationType() ) )
            {
                FilterWith filterWith = (FilterWith) annotation;
                for( Class<? extends Filter> filterClass : filterWith.value() )
                {
                    filters.add( Couple.leftOnly( filterClass ) );
                }
            }
            else
            {
                FilterWith filterWith = annotation.annotationType().getAnnotation( FilterWith.class );
                if( filterWith != null )
                {
                    for( Class<? extends Filter> filterClass : filterWith.value() )
                    {
                        filters.add( Couple.of( filterClass, annotation ) );
                    }
                }
            }
        }
        return filters;
    }

    private FilterChain buildFilterChain(
        Application app, Global global, Stack<Couple<Class<? extends Filter>, Annotation>> filters, Context context
    )
    {
        if( filters.isEmpty() )
        {
            return new FilterChainControllerTail( app, global );
        }
        else
        {
            return new FilterChainInstance(
                app,
                global,
                filters.pop(),
                buildFilterChain( app, global, filters, context )
            );
        }
    }
}
