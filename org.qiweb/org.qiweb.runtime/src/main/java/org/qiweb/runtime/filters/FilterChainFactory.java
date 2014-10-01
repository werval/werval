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
import java.lang.annotation.Repeatable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
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
                // Direct @FilterWith usage
                FilterWith filterWith = (FilterWith) annotation;
                for( Class<? extends Filter> filterClass : filterWith.value() )
                {
                    filters.add( Couple.leftOnly( filterClass ) );
                }
            }
            else if( annotation.annotationType().getAnnotation( FilterWith.class ) != null )
            {
                // Annotation annotated with @FilterWith usage
                FilterWith filterWith = annotation.annotationType().getAnnotation( FilterWith.class );
                if( filterWith != null )
                {
                    for( Class<? extends Filter> filterClass : filterWith.value() )
                    {
                        filters.add( Couple.of( filterClass, annotation ) );
                    }
                }
            }
            else
            {
                // Repeatable annotation usage
                Method[] methods = annotation.annotationType().getDeclaredMethods();
                if( methods.length != 1
                    || !"value".equals( methods[0].getName() )
                    || !methods[0].getReturnType().isArray() )
                {
                    continue;
                }
                try
                {
                    Object[] array = (Object[]) methods[0].invoke( annotation );
                    List<Annotation> repeatedAnnotations = new ArrayList<>();
                    for( Object inner : array )
                    {
                        try
                        {
                            Annotation innerAnnotation = (Annotation) inner;
                            if( innerAnnotation.annotationType().getAnnotation( Repeatable.class ) != null
                                && annotation.annotationType().isAssignableFrom(
                                    innerAnnotation.annotationType().getAnnotation( Repeatable.class ).value()
                                ) )
                            {
                                repeatedAnnotations.add( innerAnnotation );
                            }
                        }
                        catch( ClassCastException ex )
                        {
                        }
                    }
                    filters.addAll(
                        findFilters(
                            repeatedAnnotations.toArray( new Annotation[ repeatedAnnotations.size() ] )
                        )
                    );
                }
                catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
                {
                    throw new QiWebRuntimeException( ex );
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
