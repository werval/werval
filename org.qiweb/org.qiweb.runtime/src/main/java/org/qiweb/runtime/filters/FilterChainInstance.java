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
            Object controller = global.controllerInstanciation().get( context.route().controllerType() );
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
        Filter filter = global.filterInstanciation().get( filterType );
        return filter.filter( next, context );
    }
}
