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
package org.qiweb.server;

import org.qiweb.api.util.Reflectively;

/**
 * QiWeb Http Server.
 */
@Reflectively.Loaded( by = "DevShell" )
public interface HttpServer
{
    /**
     * Activate the QiWeb Http Server.
     * 
     * @throws org.qiweb.api.exceptions.QiWebException if unable to activate HttpServer
     */
    @Reflectively.Invoked( by = "DevShell" )
    void activate();

    /**
     * Passivate the QiWeb Http Server.
     *
     * @throws org.qiweb.api.exceptions.QiWebException if unable to passivate HttpServer
     */
    @Reflectively.Invoked( by = "DevShell" )
    void passivate();

    /**
     * Register a JVM shutdown hook to passivate the HttpServer.
     * <p>This method can be called ony once.</p>
     * @throws IllegalStateException when the passivation shutdown hook has already been registered
     */
    @Reflectively.Invoked( by = "DevShell" )
    void registerPassivationShutdownHook();
}
