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
