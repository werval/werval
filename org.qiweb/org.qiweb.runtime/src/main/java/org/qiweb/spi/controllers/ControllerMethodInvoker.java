package org.qiweb.spi.controllers;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;

/**
 * Invoke Controller Method.
 */
public interface ControllerMethodInvoker
{

    /**
     * Invoke Controller Method.
     * 
     * @param controller Controller Instance
     * @param context Request Context
     * @return Invocation Outcome
     */
    Outcome invoke( Object controller, Context context );
}
