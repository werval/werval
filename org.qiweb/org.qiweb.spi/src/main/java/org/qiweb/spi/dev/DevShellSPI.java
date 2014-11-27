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
package org.qiweb.spi.dev;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * Development Shell Service Provider Interface.
 * <p>
 * Methods of this class are used as extension points by the QiWeb Runtime in Development Mode.
 *
 * @navassoc 1 throws * DevShellRebuildException
 */
public interface DevShellSPI
{
    /**
     * File watching abstraction.
     * <p>
     * Allow DevShell implementations to choose their file watching implementation.
     *
     * @navassoc 1 signal * SourceChangeListener
     * @navassoc 1 create * SourceWatch
     */
    interface SourceWatcher
    {
        /**
         * Watch a set of directories.
         *
         * @param filesAndDirectories Set of files and directories to watch
         * @param listener            Listener to notify on change
         *
         * @return A handle to unwatch when done
         */
        SourceWatch watch( Set<File> filesAndDirectories, SourceChangeListener listener );
    }

    /**
     * Listener for source changes.
     */
    interface SourceChangeListener
    {
        void onChange();
    }

    /**
     * Watch allowing to stop watching.
     */
    interface SourceWatch
    {
        void unwatch();
    }

    /**
     * Application Classpath.
     * <p>
     * Typically build output directories.
     *
     * @return Application Classpath elements
     */
    URL[] applicationClassPath();

    /**
     * Runtime Classpath.
     * <p>
     * Typically application dependencies, including QiWeb.
     *
     * @return Runtime Classpath elements
     */
    URL[] runtimeClassPath();

    /**
     * Build Application source file URL if it exists.
     *
     * @param packageName Package name
     * @param fileName    File name
     * @param lineNumber  Line number
     *
     * @return URL to the Application source file or null if not present.
     */
    String sourceURL( String packageName, String fileName, int lineNumber );

    /**
     * @return Return true if source has changed since last call to {@link #rebuild()}, false otherwise
     */
    boolean isSourceChanged();

    /**
     * Rebuild the Application sources.
     *
     * @throws DevShellRebuildException if something goes wrong, HttpServer implementation should use its
     *                                  {@link DevShellRebuildException#htmlErrorPage()} methods to get the payload to
     *                                  present to the user browser.
     */
    void rebuild()
        throws DevShellRebuildException;

    /**
     * Stop.
     */
    void stop();
}
