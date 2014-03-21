/*
 * Copyright (c) 2013-2014 the original author or authors
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

import java.util.List;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.routes.Route;

import static java.util.Collections.EMPTY_LIST;

/**
 * QiWeb Application Plugin.
 *
 * @param <API> Parameterized Plugin API type, ie. the type the {@literal Application} will use.
 */
public interface Plugin<API>
{
    /**
     * @return TRUE is the Plugin is enabled, otherwise return FALSE
     */
    default boolean enabled()
    {
        return true;
    }

    /**
     * @return Plugin API type
     */
    Class<API> apiType();

    /**
     * @return Plugin API for the {@link Application} to use
     */
    API api();

    /**
     * Invoked on Application activation.
     *
     * Defaults to no operation.
     *
     * @param application Application
     *
     * @throws ActivationException if something goes wrong.
     */
    default void onActivate( Application application )
        throws ActivationException
    {
        // NOOP
    }

    /**
     * Routes to prepend to the Application routes.
     *
     * Defaults to no Route.
     * <p>
     * Called by {@literal Application} once activated.
     *
     * @return Routes this plugin prepend to the {@literal Application} routes
     */
    default List<Route> firstRoutes()
    {
        return EMPTY_LIST;
    }

    /**
     * Routes to append to the Application routes.
     *
     * Defaults to no Route.
     * <p>
     * Called by {@literal Application} once activated.
     *
     * @return Routes this plugin append to the {@literal Application} routes
     */
    default List<Route> lastRoutes()
    {
        return EMPTY_LIST;
    }

    /**
     * Invoked on Application passivation.
     *
     * Defaults to no operation.
     *
     * @param application Application
     */
    default void onPassivate( Application application )
    {
        // NOOP
    }

    /**
     * Void Plugin exposing no type to the Application.
     */
    abstract class Void
        implements Plugin<java.lang.Void>
    {
        @Override
        public final Class<java.lang.Void> apiType()
        {
            return java.lang.Void.class;
        }

        @Override
        public final java.lang.Void api()
        {
            return null;
        }
    }
}
