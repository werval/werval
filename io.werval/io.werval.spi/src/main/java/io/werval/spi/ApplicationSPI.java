/*
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
package io.werval.spi;

import io.werval.api.Application;
import io.werval.api.Global;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.Request;
import io.werval.api.http.RequestHeader;
import io.werval.api.outcomes.Outcome;
import io.werval.util.Reflectively;
import io.werval.spi.events.EventsSPI;
import io.werval.spi.http.HttpBuildersSPI;
import java.util.concurrent.CompletableFuture;

/**
 * Application SPI.
 * <p>
 * Intended for use by HttpServer implementations, the DevShell and Application unit tests.
 * <p>
 * Don't use in your Application code.
 *
 * @navcomposed 1 - 1 Global
 * @navcomposed 1 - 1 EventsSPI
 * @navcomposed 1 - 1 HttpBuildersSPI
 */
@Reflectively.Loaded( by = "DevShell" )
public interface ApplicationSPI
    extends Application
{
    /**
     * Application Global object.
     * <p>
     * The Application Global object should not be accessed by Application code,
     * that's why this accessor is in ApplicationSPI only.
     *
     * @return Application Global object
     */
    Global global();

    /**
     * Application Events SPI.
     *
     * @return Application Events SPI
     */
    @Override
    EventsSPI events();

    /**
     * HTTP API Objects Builders SPI.
     * <p>
     * Use this to create instances of HTTP API Objects found in the {@literal io.werval.api.http} package.
     * All builders are immutable and reusable.
     *
     * @return HTTP API Objects Builders SPI
     */
    @Override
    HttpBuildersSPI httpBuilders();

    /**
     * Handle a HTTP Request.
     *
     * @param request HTTP Request
     *
     * @return Future of Outcome
     */
    CompletableFuture<Outcome> handleRequest( Request request );

    /**
     * Handle an exception throwed in a HTTP Request context.
     *
     * @param requestHeader HTTP Request Header
     * @param cause         Exception throwed
     *
     * @return Error Outcome
     */
    Outcome handleError( RequestHeader requestHeader, Throwable cause );

    /**
     * Callback for completed HTTP requests.
     * <p>
     * Called once the whole response has been sent to the client.
     *
     * @param requestHeader Original request header
     */
    void onHttpRequestComplete( RequestHeader requestHeader );

    /**
     * Build the Outcome of any request happening while shutting down.
     * <p>
     * This should return a {@literal 503 Service Unavailable} status
     * and a wisely choosen {@literal Retry-After} header.
     *
     * @param version         Protocol version of the HTTP request
     * @param requestIdentity Identity of the HTTP request
     *
     * @return Future of shutting down Outcome
     */
    CompletableFuture<Outcome> shuttingDownOutcome( ProtocolVersion version, String requestIdentity );

    /**
     * Reload Application with a new ClassLoader.
     *
     * @param newClassLoader New Application ClassLoader
     */
    @Reflectively.Invoked( by = "DevShell" )
    void reload( ClassLoader newClassLoader );
}
