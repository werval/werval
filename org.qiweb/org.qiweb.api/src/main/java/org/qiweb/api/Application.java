/**
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

import java.io.File;
import java.nio.charset.Charset;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Routes;

/**
 * Application.
 * <p>
 *     An Application instance can run in different {@link Mode}s, default Mode is {@link Mode#TEST}. For your code to
 *     be {@link Mode#DEV} friendly, don't hold references to instances returned by an Application instance.
 * </p>
 */
public interface Application
{
    /**
     * Activate the Application.
     * @throws IllegalStateException if the {@literal Application} is already active.
     * @throws ActivationException if something goes wrong.
     */
    void activate()
        throws ActivationException;

    /**
     * Passivate the Application.
     * @throws IllegalStateException if the {@literal Application} was not active.
     */
    void passivate();

    /**
     * Application activation state.
     * @return {@literal TRUE} if the {@literal Application} is active, otherwise return {@literal FALSE}
     */
    boolean isActive();

    /**
     * @return {@literal Application} {@link Mode}, defaults to {@link Mode#TEST}.
     */
    Mode mode();

    /**
     * Application {@link Config}.
     * <p>Don't hold references to the Config instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Config}
     */
    Config config();

    /**
     * Lookup a Plugin's API.
     * <p>Don't hold references to the Plugins API instances in order to make your code {@link Mode#DEV} friendly.</p>
     * @param <T> Parameterized Plugin API type
     * @param pluginApiType Plugin type
     * @return The first Plugin API found that match given type (Type equals first, then assignable).
     * @throws IllegalArgumentException if no Plugin is found for the given API type
     */
    <T> T plugin( Class<T> pluginApiType );

    /**
     * Lookup possibly several Plugin's API.
     * <p>Don't hold references to the Plugins API instances in order to make your code {@link Mode#DEV} friendly.</p>
     * @param <T> Parameterized Plugin API type
     * @param pluginApiType Plugin type
     * @return All Plugin APIs found that match the he given type (Type equals first, then assignables), or none if no
     *         Plugin is found for the given API type.
     */
    <T> Iterable<T> plugins( Class<T> pluginApiType );

    /**
     * Application {@link Crypto}.
     * <p>Don't hold references to the Crypto instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Crypto}
     */
    Crypto crypto();

    /**
     * Application default {@link Charset}.
     * @return Application default {@link Charset}
     */
    Charset defaultCharset();

    /**
     * Application temporary directory {@link File}.
     * @return Application temporary directory {@link File}
     */
    File tmpdir();

    /**
     * Application {@link ClassLoader}.
     * <p>Don't hold references to the ClassLoader instance to prevent memory leaks in {@link Mode#DEV}.</p>
     * @return Application {@link ClassLoader}
     */
    ClassLoader classLoader();

    /**
     * Application {@link Routes}.
     * <p>Don't hold references to the Routes instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Routes}
     */
    Routes routes();

    /**
     * Application {@link ReverseRoutes}.
     * <p>Don't hold references to the ReverseRoutes instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link ReverseRoutes}
     */
    ReverseRoutes reverseRoutes();

    /**
     * Application {@link ParameterBinders}.
     * <p>Don't hold references to the ParameterBinders instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link ParameterBinders}
     */
    ParameterBinders parameterBinders();

    /**
     * Application {@link MimeTypes}.
     * <p>Don't hold references to the MimeTypes instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link MimeTypes}
     */
    MimeTypes mimeTypes();

    /**
     * Application {@link MetaData}.
     * <p>Don't hold references to the MetaData instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link MetaData}
     */
    MetaData metaData();

    /**
     * Application {@link Errors}.
     * <p>Don't hold references to the Errors instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Errors}
     */
    Errors errors();
}
