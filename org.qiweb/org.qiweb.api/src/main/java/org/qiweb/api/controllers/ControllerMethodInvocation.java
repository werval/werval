package org.qiweb.api.controllers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qiweb.api.exceptions.QiWebException;

/**
 * Invoke Controller Method.
 */
public interface ControllerMethodInvocation
{

    /**
     * Invoke Controller Method.
     * 
     * @param context Request Context
     * @param controller Controller Instance
     * @return Invocation Outcome
     */
    Outcome invoke( Context context, Object controller );

    /**
     * Default Controller Method Invocation.
     * <p>Simple {@link Method#invoke(java.lang.Object, java.lang.Object[])}.</p>
     */
    class Default
        implements ControllerMethodInvocation
    {

        @Override
        public Outcome invoke( Context context, Object controller )
        {
            try
            {
                Method method = context.route().controllerMethod();
                Object[] arguments = context.request().pathParams().values().toArray();
                return (Outcome) method.invoke( controller, arguments );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
            {
                throw new QiWebException( "Unable to invoke Controller Method.", ex );
            }
        }
    }
}
