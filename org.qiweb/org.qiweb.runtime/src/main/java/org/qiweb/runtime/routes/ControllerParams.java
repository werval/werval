package org.qiweb.runtime.routes;

import org.qiweb.runtime.routes.ControllerParams.ControllerParam;

/**
 * Route Controller Params.
 */
public interface ControllerParams
    extends Iterable<ControllerParam>
{

    /**
     * @param name Name of the ControllerParam
     * @return The ControllerParam or null if absent
     */
    ControllerParam get( String name );

    /**
     * @return The names of all ControllerParam in method order
     */
    Iterable<String> names();

    /**
     * @return The types of all ControllerParams in method order
     */
    Class<?>[] types();

    /**
     * Route Controller Param.
     */
    interface ControllerParam
    {

        /**
         * @return Name of the Controller Param
         */
        String name();

        /**
         * @return Type of the Controller Param
         */
        Class<?> type();

        /**
         * @return TRUE if this ControllerParam has a forced value, otherwise return FALSE
         */
        boolean hasForcedValue();

        /**
         * @return Forced value of the Controller Param
         * @throws IllegalStateException if no forced value
         */
        Object forcedValue();
    }
}
