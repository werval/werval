package org.qiweb.runtime.filters;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import org.qiweb.api.Global;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.runtime.filters.FilterChainInstance.FilterChainControllerTail;

public class FilterChainFactory
{

    public FilterChain buildFilterChain( Global global, Context context )
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
        return buildFilterChain( global, filtersStack, context );
    }

    private FilterChain buildFilterChain( Global global, Stack<Class<? extends Filter>> filters, Context context )
    {

        if( filters.isEmpty() )
        {
            return new FilterChainControllerTail( global );
        }
        else
        {
            return new FilterChainInstance( global, filters.pop(), buildFilterChain( global, filters, context ) );
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