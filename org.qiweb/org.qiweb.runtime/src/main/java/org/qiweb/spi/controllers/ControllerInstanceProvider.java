package org.qiweb.spi.controllers;

import org.qiweb.api.controllers.Context;

/**
 * Provide Controller Instance.
 */
public interface ControllerInstanceProvider
{

    /**
     * Provide Controller Instance.
     * 
     * @param context Request Context
     * @return Controller Instance
     */
    Object get( Context context );
}
