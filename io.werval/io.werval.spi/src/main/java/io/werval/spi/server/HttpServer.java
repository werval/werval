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
package io.werval.spi.server;

import io.werval.spi.ApplicationSPI;
import io.werval.spi.dev.DevShellSPI;
import io.werval.util.Reflectively;

/**
 * Werval Http Server.
 *
 * @navcomposed 1 - 1 ApplicationSPI
 * @navcomposed 1 - 1 DevShellSPI
 */
@Reflectively.Loaded( by = "DevShell" )
public interface HttpServer
{
    /**
     * Set the Application SPI.
     *
     * @param application ApplicationSPI
     */
    @Reflectively.Invoked( by = "DevShell" )
    void setApplicationSPI( ApplicationSPI application );

    /**
     * Set the Development Shell SPI.
     *
     * @param devSpi DevShell SPI
     */
    @Reflectively.Invoked( by = "DevShell" )
    void setDevShellSPI( DevShellSPI devSpi );

    /**
     * Activate the Werval Http Server.
     *
     * @throws IllegalStateException                   if ApplicationSPI is not set
     * @throws io.werval.api.exceptions.WervalException if unable to activate HttpServer
     */
    @Reflectively.Invoked( by = "DevShell" )
    void activate();

    /**
     * Passivate the Werval Http Server.
     *
     * @throws io.werval.api.exceptions.WervalException if unable to passivate HttpServer
     */
    @Reflectively.Invoked( by = "DevShell" )
    void passivate();

    /**
     * Register a JVM shutdown hook to passivate the HttpServer.
     * <p>
     * This method can be called ony once.
     *
     * @throws IllegalStateException when the passivation shutdown hook has already been registered
     */
    @Reflectively.Invoked( by = "DevShell" )
    void registerPassivationShutdownHook();
}
