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

import org.qiweb.api.Instanciation.ControllerInstanciation;
import org.qiweb.api.Instanciation.FilterInstanciation;
import org.qiweb.api.controllers.ControllerMethodInvocation;
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
     * Invoked when each request is complete.
     * <p>Default to NOOP.</p>
     */
    public void onHttpRequestComplete( RequestHeader requestHeader )
    {
    }

    /**
     * Controller Filter Instanciation.
     * <p>Default to {@link Instanciation.FilterInstanciation.Default}.</p>
     * @return A Controller Filter Instanciation
     */
    public FilterInstanciation filterInstanciation()
    {
        return new FilterInstanciation.Default();
    }

    /**
     * A Provider of Controller Instances.
     * <p>Default to {@link ControllerInstanceProvider.Default}.</p>
     * @return A Provider of Controller Instances
     */
    public ControllerInstanciation controllerInstanciation()
    {
        return new ControllerInstanciation.Default();
    }

    /**
     * A Controller Method Invoker.
     * <p>Default to {@link ControllerMethodInvoker.Default}.</p>
     * @return A Controller Method Invoker
     */
    public ControllerMethodInvocation controllerMethodInvocation()
    {
        return new ControllerMethodInvocation.Default();
    }
}
