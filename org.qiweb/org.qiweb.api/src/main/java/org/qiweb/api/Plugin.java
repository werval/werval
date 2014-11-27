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
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteBuilder;

import static java.util.Collections.EMPTY_LIST;
import static org.qiweb.util.Strings.EMPTY;

/**
 * Application Plugin.
 * <p>
 * A {@literal Plugin} is activated/passivated alongside the {@literal Application},
 * can contribute {@literal Routes} to it,
 * is given a change to hook around interactions
 * and can depend on other plugins.
 * <p>
 * Plugins should not create static state and their instanciation should be as close to {@literal NOOP} as possible.
 *
 * @param <API> Parameterized Plugin API type, ie. the type the {@literal Application} will use.
 */
public interface Plugin<API>
{
    /**
     * Plugin API type.
     *
     * @return Plugin API type
     */
    Class<API> apiType();

    /**
     * Plugin API.
     *
     * @return Plugin API for the {@link Application} to use
     */
    API api();

    /**
     * Plugin dependencies.
     * <p>
     * The runtime will use this information to order the plugins activation order according to the dependency graph.
     * <p>
     * Defaults to an empty list.
     * <p>
     * Application configuration is provided to the plugin so it can decide on what to depend according to its own
     * configuration.
     *
     * @param config Application configuration
     *
     * @return This plugin's dependencies
     */
    default List<Class<?>> dependencies( Config config )
    {
        return EMPTY_LIST;
    }

    /**
     * Invoked on Application activation.
     * <p>
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
     * <p>
     * Defaults to no Route.
     * <p>
     * Called by {@literal Application} once activated.
     *
     * @param mode         Application Mode
     * @param routeBuilder Builder for Routes
     *
     * @return Routes this plugin prepend to the {@literal Application} routes
     */
    default List<Route> firstRoutes( Mode mode, RouteBuilder routeBuilder )
    {
        return EMPTY_LIST;
    }

    /**
     * Routes to append to the Application routes.
     * <p>
     * Defaults to no Route.
     * <p>
     * Called by {@literal Application} once activated.
     *
     * @param mode         Application Mode
     * @param routeBuilder Builder for Routes
     *
     * @return Routes this plugin append to the {@literal Application} routes
     */
    default List<Route> lastRoutes( Mode mode, RouteBuilder routeBuilder )
    {
        return EMPTY_LIST;
    }

    /**
     * Invoked on Application passivation.
     * <p>
     * Defaults to no operation.
     *
     * @param application Application
     */
    default void onPassivate( Application application )
    {
        // NOOP
    }

    /**
     * Invoked before each HTTP Interaction.
     * <p>
     * Defaults to no operation.
     *
     * @param context Interaction Context
     */
    default void beforeInteraction( Context context )
    {
        // NOOP
    }

    /**
     * Invoked after each HTTP Interaction.
     * <p>
     * Defaults to no operation.
     *
     * @param context Interaction Context
     */
    default void afterInteraction( Context context )
    {
        // NOOP
    }

    /**
     * Void Plugin exposing no type to the Application.
     * <p>
     * Use this as a base class for your under cover plugins.
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
