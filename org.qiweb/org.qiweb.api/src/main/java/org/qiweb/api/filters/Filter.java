package org.qiweb.api.filters;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;

/**
 * Controller invocation Filter.
 */
public interface Filter
{

    /**
     * Filter a request.
     * 
     * @param chain Filter Chain
     * @param context Request Context
     * @return Filtered Outcome
     */
    Outcome filter( FilterChain chain, Context context );
}
