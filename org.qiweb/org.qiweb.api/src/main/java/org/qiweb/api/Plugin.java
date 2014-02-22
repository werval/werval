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

import org.qiweb.api.exceptions.ActivationException;

/**
 * QiWeb Application Plugin.
 * @param <API> Parameterized Plugin API type, ie. the type the {@literal Application} will use.
 */
public interface Plugin<API>
{

    /**
     * @return TRUE is the Plugin is enabled, otherwise return FALSE
     */
    boolean enabled();

    /**
     * Invoked on Application activation.
     * @param application Application
     * @throws ActivationException if something goes wrong.
     */
    void onActivate( Application application )
        throws ActivationException;

    /**
     * Invoked on Application passivation.
     * @param application Application
     */
    void onPassivate( Application application );

    /**
     * @return Plugin API type
     */
    Class<API> apiType();

    /**
     * @return Plugin API for the {@link Application} to use
     */
    API api();

}
