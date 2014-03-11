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
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.util.Reflectively;
import org.qiweb.spi.http.HttpBuilders;

/**
 * Application SPI.
 * <p>Intended for use by HttpServer implementations and unit tests.</p>
 */
@Reflectively.Loaded( by = "DevShell" )
public interface ApplicationSPI
    extends Application
{
    /**
     * @return Application Global object
     */
    Global global();

    HttpBuilders httpBuilders();

    Outcome handleRequest( Request request );

    Outcome handleError( RequestHeader requestHeader, Throwable cause );

    void onHttpRequestComplete( RequestHeader requestHeader );

    Outcome shuttingDownOutcome( ProtocolVersion version, String requestIdentity );

    /**
     * Reload Application with a new ClassLoader.
     *
     * @param newClassLoader New Application ClassLoader
     */
    @Reflectively.Invoked( by = "DevShell" )
    void reload( ClassLoader newClassLoader );
}
