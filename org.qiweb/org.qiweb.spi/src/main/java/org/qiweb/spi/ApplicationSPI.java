/**
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.spi;

import org.qiweb.api.Application;
import org.qiweb.api.Global;
import org.qiweb.api.util.Reflectively;

/**
 * Application SPI.
 */
@Reflectively.Loaded( by = "DevShell" )
public interface ApplicationSPI
    extends Application
{
    /**
     * @return Application Global object
     */
    Global global();

    /**
     * Reload Application with a new ClassLoader.
     * <p>Called reflectively by {@literal org.qiweb.devshell.DevShell}</p>
     *
     * @param newClassLoader New Application ClassLoader
     */
    void reload( ClassLoader newClassLoader );

}
