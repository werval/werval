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

import java.io.File;
import java.nio.charset.Charset;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.http.HttpBuilders;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Routes;
import org.qiweb.api.util.Reflectively;
import org.qiweb.api.util.Stacktraces;

/**
 * Application.
 *
 * An Application instance can run in different {@link Mode}s, default Mode is {@link Mode#TEST}. For your code to
 * be {@link Mode#DEV} friendly, don't hold references to instances returned by an Application instance.
 */
@Reflectively.Loaded( by = "DevShell" )
public interface Application
{
    /**
     * Activate the Application.
     *
     * @throws IllegalStateException if the {@literal Application} is already active.
     * @throws ActivationException   if something goes wrong.
     */
    void activate()
        throws ActivationException;

    /**
     * Passivate the Application.
     *
     * @throws IllegalStateException if the {@literal Application} was not active.
     */
    void passivate();

    /**
     * Application activation state.
     *
     * @return {@literal TRUE} if the {@literal Application} is active, otherwise return {@literal FALSE}
     */
    boolean isActive();

    /**
     * Application Mode.
     *
     * @return {@literal Application} {@link Mode}, defaults to {@link Mode#TEST}.
     */
    Mode mode();

    /**
     * Application {@link Config}.
     *
     * Don't hold references to the Config instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link Config}
     */
    Config config();

    /**
     * Lookup a Plugin's API.
     *
     * Don't hold references to the Plugins API instances in order to make your code {@link Mode#DEV} friendly.
     *
     * @param <T>           Parameterized Plugin API type
     * @param pluginApiType Plugin type
     *
     * @return The first Plugin API found that match given type (Type equals first, then assignable).
     *
     * @throws IllegalArgumentException if no {@literal Plugin} is found for the given API type
     * @throws IllegalStateException    if the {@literal Application} is not active
     */
    <T> T plugin( Class<T> pluginApiType );

    /**
     * Lookup possibly several Plugin's API.
     *
     * Don't hold references to the Plugins API instances in order to make your code {@link Mode#DEV} friendly.
     *
     * @param <T>           Parameterized Plugin API type
     * @param pluginApiType Plugin type
     *
     * @return All Plugin APIs found that match the he given type (Type equals first, then assignables), or none if no
     *         Plugin is found for the given API type.
     *
     * @throws IllegalStateException if the {@literal Application} is not active
     */
    <T> Iterable<T> plugins( Class<T> pluginApiType );

    /**
     * Application {@link Crypto}.
     *
     * Don't hold references to the Crypto instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link Crypto}
     */
    Crypto crypto();

    /**
     * Application default {@link Charset}.
     *
     * @return Application default {@link Charset}
     */
    Charset defaultCharset();

    /**
     * Application temporary directory {@link File}.
     *
     * @return Application temporary directory {@link File}
     */
    File tmpdir();

    /**
     * Application {@link ClassLoader}.
     *
     * Don't hold references to the ClassLoader instance to prevent memory leaks in {@link Mode#DEV}.
     *
     * @return Application {@link ClassLoader}
     */
    ClassLoader classLoader();

    /**
     * HTTP API Objects Builders SPI.
     *
     * Use this to create instances of HTTP API Objects found in the {@link org.qiweb.api.http} package.
     * All builders are immutable and reusable.
     *
     * @return HTTP API Objects Builders SPI
     */
    HttpBuilders httpBuilders();

    /**
     * Application {@link Routes}.
     *
     * Don't hold references to the Routes instance in order to make your code {@link Mode#DEV} friendly.
     * <p>
     * The <em>Application Routes</em> include Routes contributed by registered Plugins.
     *
     * @return Application {@link Routes}
     *
     * @throws IllegalStateException if the {@literal Application} is not active
     */
    Routes routes();

    /**
     * Application {@link ReverseRoutes}.
     *
     * Don't hold references to the ReverseRoutes instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link ReverseRoutes}
     *
     * @throws IllegalStateException if the {@literal Application} is not active
     */
    ReverseRoutes reverseRoutes();

    /**
     * Application {@link ParameterBinders}.
     *
     * Don't hold references to the ParameterBinders instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link ParameterBinders}
     */
    ParameterBinders parameterBinders();

    /**
     * Application {@link MimeTypes}.
     *
     * Don't hold references to the MimeTypes instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link MimeTypes}
     */
    MimeTypes mimeTypes();

    /**
     * Application {@link MetaData}.
     *
     * Don't hold references to the MetaData instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link MetaData}
     */
    MetaData metaData();

    /**
     * Application {@link Errors}.
     *
     * Don't hold references to the Errors instance in order to make your code {@link Mode#DEV} friendly.
     *
     * @return Application {@link Errors}
     */
    Errors errors();

    /**
     * FileURLGenerator for Application Sources.
     *
     * Works only in {@link Mode#DEV} mode.
     *
     * @return FileURLGenerator for Application Sources
     */
    Stacktraces.FileURLGenerator sourceFileURLGenerator();
}
