package org.qiweb.runtime.filters;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.spi.controllers.ControllerInstanceProvider;
import org.qiweb.spi.controllers.ControllerMethodInvoker;

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

        private final ControllerInstanceProvider controllerInstanceProvider;
        private final ControllerMethodInvoker controllerMethodInvoker;

        /* package */ FilterChainControllerTail(
            ControllerInstanceProvider controllerInstanceProvider,
            ControllerMethodInvoker controllerMethodInvoker )
        {
            this.controllerInstanceProvider = controllerInstanceProvider;
            this.controllerMethodInvoker = controllerMethodInvoker;
        }

        @Override
        public Outcome next( Context context )
        {
            return controllerMethodInvoker.invoke( controllerInstanceProvider.get( context ), context );
        }
    }
    private final Class<? extends Filter> filterType;
    private final FilterChain next;

    /* package */ FilterChainInstance( Class<? extends Filter> filterType, FilterChain next )
    {
        this.filterType = filterType;
        this.next = next;
    }

    @Override
    public Outcome next( Context context )
    {
        // TODO Implement Filters Instanciation by Application Code
        try
        {
            return filterType.newInstance().filter( next, context );
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new QiWebException( "Unable to instanciate Filter " + filterType, ex );
        }
    }
}
