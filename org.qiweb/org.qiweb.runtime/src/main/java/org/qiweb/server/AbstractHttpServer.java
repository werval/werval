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

import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;

/**
 * Base class to write HttpServer implementations.
 * <p>See {@link HttpServerHelper} for composable helper to use in implementations.</p>
 */
public abstract class AbstractHttpServer
    implements HttpServer
{
    private static final class ShutdownHook
        implements Runnable
    {
        private final HttpServer server;

        private ShutdownHook( HttpServer server )
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            server.passivate();
        }
    }

    protected final String identity;
    protected final ApplicationSPI app;
    protected final DevShellSPI devSpi;
    private final Thread shutdownHook;

    protected AbstractHttpServer( String identity, ApplicationSPI app, DevShellSPI devSpi )
    {
        this.identity = identity;
        this.app = app;
        this.devSpi = devSpi;
        this.shutdownHook = new Thread( new ShutdownHook( this ), "qiweb-shutdown" );
    }

    @Override
    public final void registerPassivationShutdownHook()
    {
        try
        {
            Runtime.getRuntime().addShutdownHook( shutdownHook );
        }
        catch( IllegalArgumentException ex )
        {
            throw new IllegalStateException( "HttpServer passivation hook previously registered", ex );
        }
    }
}
