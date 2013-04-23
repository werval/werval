package org.qiweb.runtime.filters;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.filters.FilterChainInstance.FilterChainControllerTail;
import org.qiweb.spi.controllers.ControllerInstanceProvider;
import org.qiweb.spi.controllers.ControllerMethodInvoker;

public class FilterChainFactory
{

    public FilterChain buildFilterChain( Route route )
    {
        Set<Class<? extends Filter>> uniqueFilters = new LinkedHashSet<>();
        uniqueFilters.addAll( findFilterWithOnType( route.controllerType() ) );
        FilterWith filterWith = route.controllerMethod().getAnnotation( FilterWith.class );
        if( filterWith != null )
        {
            uniqueFilters.addAll( Arrays.asList( filterWith.value() ) );
        }
        Stack<Class<? extends Filter>> filtersStack = new Stack<>();
        filtersStack.addAll( uniqueFilters );
        return buildFilterChain( filtersStack, route );
    }

    private FilterChain buildFilterChain( Stack<Class<? extends Filter>> filters, final Route route )
    {

        if( filters.isEmpty() )
        {
            return new FilterChainControllerTail(
                new ControllerInstanceProvider()
            {
                @Override
                public Object get( Context context )
                {
                    // TODO Implement Controller Instanciation by Application Code
                    try
                    {
                        return context.application().classLoader().loadClass( route.controllerType().getName() ).newInstance();
                    }
                    catch( ClassNotFoundException | InstantiationException | IllegalAccessException ex )
                    {
                        throw new QiWebException( "Unable to instanciate Controller Type.", ex );
                    }
                }
            },
                new ControllerMethodInvoker()
            {
                @Override
                public Outcome invoke( Object controller, Context context )
                {
                    // TODO Implement Controller Method Invocation by Application Code
                    try
                    {
                        return (Outcome) route.controllerMethod().invoke( controller, context.request().pathParams().values().toArray() );
                    }
                    catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
                    {
                        throw new QiWebException( "Unable to invoke Controller Method.", ex );
                    }
                }
            } );
        }
        else
        {
            return new FilterChainInstance( filters.pop(), buildFilterChain( filters, route ) );

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
