/**
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                Object[] parameters = context.request().parameters().values().toArray();
                return (Outcome) method.invoke( controller, parameters );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
            {
                throw new QiWebException( "Unable to invoke Controller Method.", ex );
            }
        }
    }
}
