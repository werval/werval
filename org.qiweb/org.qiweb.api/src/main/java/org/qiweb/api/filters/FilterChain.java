package org.qiweb.api.filters;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;

/**
 * Chain of Filters.
 */
public interface FilterChain
{

    /**
     * Pass the Context to the next Filter in the chain.
     */
    Outcome next( Context context );
}
