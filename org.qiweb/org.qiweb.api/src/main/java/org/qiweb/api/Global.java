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
package org.qiweb.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.RequestHeader;

/**
 * Application Global Object.
 * <p>Provide lifecycle, instanciation and invocation hooks.</p>
 * <p>
 *     You are encouraged to subclass it in your Application code by setting the <code>app.global</code> configuration
 *     property to the FQCN of your Global implementation.
 * </p>
 */
public class Global
{

    /**
     * Invoked on Application start.
     * @param application Application
     */
    public void onStart( Application application )
    {
    }

    /**
     * Invoked on Application stop.
     * @param application Application
     */
    public void onStop( Application application )
    {
    }

    /**
     * Invoked before binding Http Server.
     * <p>Invoked after {@link #onStart(org.qiweb.api.Application)}.</p>
     * <p>Default to NOOP.</p>
     * <p>Reloads occuring in development mode do not trigger this call.</p>
     *
     * @param application Application
     */
    public void beforeHttpBind( Application application )
    {
    }

    /**
     * Invoked after binding Http Server.
     * <p>Default to NOOP.</p>
     * <p>Reloads occuring in development mode do not trigger this call.</p>
     *
     * @param application Application
     */
    public void afterHttpBind( Application application )
    {
    }

    /**
     * Invoked before unbinding Http Server.
     * <p>Default to NOOP.</p>
     * <p>Reloads occuring in development mode do not trigger this call.</p>
     *
     * @param application Application
     */
    public void beforeHttpUnbind( Application application )
    {
    }

    /**
     * Invoked after unbinding Http Server.
     * <p>Invoked before {@link #onStop(org.qiweb.api.Application)}.</p>
     * <p>Default to NOOP.</p>
     * <p>Reloads occuring in development mode do not trigger this call.</p>
     *
     * @param application Application
     */
    public void afterHttpUnbind( Application application )
    {
    }

    /**
     * Get Filter instance.
     * <p>Default to {@link Class#newInstance()} instanciation without any cache.</p>
     * 
     * @param <T> Filter Parameterized Type
     * @param application Application
     * @param filterType Filter Type
     * @return Filter Instance
     */
    public <T> T getFilterInstance( Application application, Class<T> filterType )
    {
        try
        {
            return filterType.newInstance();
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new QiWebException( "Unable to instanciate Filter Type.", ex );
        }
    }

    /**
     * Get Controller instance.
     * <p>Default to {@link Class#newInstance()} instanciation without any cache.</p>
     *
     * @param <T> Controller Parameterized Type
     * @param application Application
     * @param controllerType Controller Type
     * @return Controller Instance
     */
    public <T> T getControllerInstance( Application application, Class<T> controllerType )
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

    /**
     * Invoke Controller Method.
     * <p>Default to {@link Method#invoke(java.lang.Object, java.lang.Object[])}.</p>
     * 
     * @param context Request Context
     * @param controller Controller Instance
     * @return Invocation Outcome
     */
    public Outcome invokeControllerMethod( Context context, Object controller )
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

    /**
     * Invoked when a request completed successfully.
     * <p>Default to NOOP.</p>
     * 
     * @param application Application
     * @param requestHeader Request Header
     */
    public void onHttpRequestComplete( Application application, RequestHeader requestHeader )
    {
    }

    /**
     * Invoked when an exception pops out of the Application.
     * <p>Default to NOOP.</p>
     *
     * @param requestHeader Request Header
     * @param error Error
     */
    public void onHttpRequestError( Application application, Error error )
    {
    }
}
