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

import java.io.File;
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
     * {@link Application} Mode.
     * <p>Default is {@link #TEST} Mode.</p>
     */
    enum Mode
    {

        /**
         * Development Mode.
         * <p>Source is watched, Application is reloaded on-demand.</p>
         */
        DEV( "Development" ),
        /**
         * Test Mode.
         * <p>Intended to be used in unit tests, almost equivalent to {@link #PROD} Mode.</p>
         */
        TEST( "Test" ),
        /**
         * Production Mode.
         * <p>Run from binaries only, nothing gets reloaded.</p>
         */
        PROD( "Production" );
        private final String displayName;

        private Mode( String displayName )
        {
            this.displayName = displayName;
        }

        /**
         * @return The display name of this Application Mode
         */
        @Override
        public String toString()
        {
            return displayName;
        }
    }

    /**
     * @return Application {@link Mode}, defaults to {@link Mode#TEST}.
     */
    Mode mode();

    /**
     * Application {@link Config}.
     * <p>Don't hold references to the Config instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Config}
     */
    Config config();

    /**
     * Application {@link Crypto}.
     * <p>Don't hold references to the Crypto instance in order to make your code {@link Mode#DEV} friendly.</p>
     * @return Application {@link Crypto}
     */
    Crypto crypto();

    /**
     * Application temporary directory {@link File}.
     * <p>Don't hold references to the File instance in order to make your code {@link Mode#DEV} friendly.</p>
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
}
