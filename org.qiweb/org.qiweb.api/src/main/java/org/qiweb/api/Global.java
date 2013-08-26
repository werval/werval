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

import org.qiweb.api.controllers.ControllerMethodInvocation;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.RequestHeader;

/**
 * Application Global Object.
 * <p>Provide lifecycle, instanciation and invocation hooks.</p>
 * <p>You are encouraged to subclass it in your Application code.</p>
 */
public class Global
{

    /**
     * Invoked before binding Http Server.
     * <p>Default to NOOP.</p>
     */
    public void beforeHttpBind( Application application )
    {
    }

    /**
     * Invoked after binding Http Server.
     * <p>Default to NOOP.</p>
     */
    public void afterHttpBind( Application application )
    {
    }

    /**
     * Invoked before unbinding Http Server.
     * <p>Default to NOOP.</p>
     */
    public void beforeHttpUnbind( Application application )
    {
    }

    /**
     * Invoked after unbinding Http Server.
     * <p>Default to NOOP.</p>
     */
    public void afterHttpUnbind( Application application )
    {
    }

    /**
     * Get Filter instance.
     * <p>Default to {@link Class#newInstance()} instanciation without any cache.</p>
     * 
     * @param <T> Filter Parameterized Type
     * @param filterType Filter Type
     * @return Filter Instance
     */
    public <T> T getFilterInstance( Class<T> filterType )
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
     * @param controllerType Controller Type
     * @return Controller Instance
     */
    public <T> T getControllerInstance( Class<T> controllerType )
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
     * A Controller Method Invocation.
     * <p>Default to {@link org.qiweb.api.controllers.ControllerMethodInvocation.Default}.</p>
     * @return A Controller Method Invocation
     */
    public ControllerMethodInvocation controllerMethodInvocation()
    {
        return new ControllerMethodInvocation.Default();
    }

    /**
     * Invoked when a request completed successfully.
     * <p>Default to NOOP.</p>
     */
    public void onHttpRequestComplete( RequestHeader requestHeader )
    {
    }
}
