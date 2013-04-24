package org.qiweb.api;

import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.filters.Filter;

public interface Instanciation
{

    /**
     * Provide Controller Instances.
     */
    interface ControllerInstanciation
    {

        /**
         * Provide Controller Instance.
         * 
         * @param controllerType Request Context
         * @return Controller Instance
         */
        <T> T get( Class<T> controllerType );

        /**
         * Default Controller Instanciation.
         * <p>Simple {@link Class#newInstance()} instanciation without any cache.</p>
         */
        class Default
            implements ControllerInstanciation
        {

            @Override
            public <T> T get( Class<T> controllerType )
            {
                try
                {
                    return controllerType.newInstance();
                }
                catch( InstantiationException | IllegalAccessException ex )
                {
                    throw new QiWebException( "Unable to instanciate Controller Type.", ex );
                }
            }
        }
    }

    /**
     * Provide Filter Instances.
     */
    interface FilterInstanciation
    {

        /**
         * Provide Filter Instance.
         * 
         * @param <T> Filter Parameterized Type
         * @param filterType Filter Type
         * @return Filter Instance
         */
        <T extends Filter> T get( Class<T> filterType );

        /**
         * Default Filter Instanciation.
         * <p>Simple {@link Class#newInstance()} instanciation without any cache.</p>
         */
        class Default
            implements FilterInstanciation
        {

            @Override
            public <T extends Filter> T get( Class<T> filterType )
            {
                try
                {
                    return filterType.newInstance();
                }
                catch( InstantiationException | IllegalAccessException ex )
                {
                    throw new QiWebException( "Unable to instanciate Controller Filter Type.", ex );
                }
            }
        }
    }
}
