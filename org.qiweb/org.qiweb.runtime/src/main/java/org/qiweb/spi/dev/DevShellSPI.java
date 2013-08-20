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

import java.net.URL;

/**
 * Development Shell Service Provider Interface.
 * <p>Methods of this class are used as extension points by the QiWeb Runtime in Development Mode.</p>
 */
public interface DevShellSPI
{

    URL[] classPath();

    /**
     * Build Application source file URL if it exists.
     * @return URL to the Application source file or null if not present.
     */
    String sourceURL( String fileName, int lineNumber );

    /**
     * @return Return true if source has changed since last call to {@link #rebuild()}, false otherwise
     */
    boolean isSourceChanged();

    /**
     * Rebuild the Application sources.
     */
    void rebuild();
}
