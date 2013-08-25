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
package org.qiweb.spi.dev;

import java.io.File;
import java.util.Set;

/**
 * File watching abstraction.
 * <p>Allow DevShell implementations to choose their file watching implementation.</p>
 */
public interface Watcher
{

    /**
     * Listener for source changes.
     */
    interface ChangeListener
    {

        void onChange();
    }

    /**
     * Watch allowing to stop watching.
     */
    interface Watch
    {

        void unwatch();
    }

    /**
     * Watch a set of directories.
     *
     * @param directories Set of directories to watch
     * @param listener Listener to notify on change
     * @return A handle to unwatch when done
     */
    Watch watch( Set<File> directories, ChangeListener listener );
}
