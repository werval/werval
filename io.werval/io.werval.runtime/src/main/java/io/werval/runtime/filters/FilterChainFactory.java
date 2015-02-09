/*
 * Copyright (c) 2013-2015 the original author or authors
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
package io.werval.runtime.filters;

import io.werval.api.Application;
import io.werval.api.Global;
import io.werval.api.context.Context;
import io.werval.api.filters.Filter;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.util.Couple;
import io.werval.runtime.exceptions.WervalRuntimeException;
import io.werval.runtime.filters.FilterChainInstance.FilterChainControllerTail;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * FilterChain Factory.
 */
public final class FilterChainFactory
{
    public FilterChain buildFilterChain( Application app, Global global, Context context )
    {
        Queue<Couple<Class<? extends Filter>, Annotation>> filters = new ArrayDeque<>();
        filters.addAll( findFiltersOnType( context.route().controllerType() ) );
        filters.addAll( findFilters( context.route().controllerMethod().getAnnotations() ) );
        return buildFilterChain( app, global, filters, context );
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

    private List<Couple<Class<? extends Filter>, Annotation>> findFilters( Annotation... annotations )
    {
        List<Couple<Class<? extends Filter>, Annotation>> filters = new ArrayList<>();
        for( Annotation annotation : annotations )
        {
            if( annotation.annotationType().getName().startsWith( "java.lang" ) )
            {
                // Skip java.lang annotations, there contains cyclic declarations causing stack overflows
            }
            else if( FilterWith.class.equals( annotation.annotationType() ) )
            {
                // Direct @FilterWith usage
                FilterWith filterWith = (FilterWith) annotation;
                for( Class<? extends Filter> filterClass : filterWith.value() )
                {
                    filters.add( Couple.leftOnly( filterClass ) );
                }
            }
            else
            {
                // Repeatable annotation usage
                Method[] methods = annotation.annotationType().getDeclaredMethods();
                if( methods.length == 1
                    && "value".equals( methods[0].getName() )
                    && methods[0].getReturnType().isArray() )
                {
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
                                // Ignored
                            }
                        }
                        if( repeatedAnnotations.isEmpty() )
                        {
                            // Not a Repeatable annotation usage, handling as any other annotation
                            for( Annotation innerAnnotation : annotation.annotationType().getAnnotations() )
                            {
                                if( FilterWith.class.equals( innerAnnotation.annotationType() ) )
                                {
                                    FilterWith filterWith = (FilterWith) innerAnnotation;
                                    for( Class<? extends Filter> filterClass : filterWith.value() )
                                    {
                                        filters.add( Couple.of( filterClass, annotation ) );
                                    }
                                }
                                else
                                {
                                    filters.addAll( findFilters( innerAnnotation ) );
                                }
                            }
                        }
                        else
                        {
                            // This is a Repeatable annotation usage !
                            filters.addAll(
                                findFilters(
                                    repeatedAnnotations.toArray( new Annotation[ repeatedAnnotations.size() ] )
                                )
                            );
                        }
                    }
                    catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
                    {
                        throw new WervalRuntimeException( ex );
                    }
                }
                else
                {
                    // Any other annotation
                    for( Annotation innerAnnotation : annotation.annotationType().getAnnotations() )
                    {
                        if( FilterWith.class.equals( innerAnnotation.annotationType() ) )
                        {
                            FilterWith filterWith = (FilterWith) innerAnnotation;
                            for( Class<? extends Filter> filterClass : filterWith.value() )
                            {
                                filters.add( Couple.of( filterClass, annotation ) );
                            }
                        }
                        else
                        {
                            filters.addAll( findFilters( innerAnnotation ) );
                        }
                    }
                }
            }
        }
        return filters;
    }

    private FilterChain buildFilterChain(
        Application app, Global global, Queue<Couple<Class<? extends Filter>, Annotation>> filters, Context context
    )
    {
        if( filters.isEmpty() )
        {
            return new FilterChainControllerTail( app, global );
        }
        return new FilterChainInstance(
            app,
            global,
            filters.poll(),
            buildFilterChain( app, global, filters, context )
        );
    }
}
