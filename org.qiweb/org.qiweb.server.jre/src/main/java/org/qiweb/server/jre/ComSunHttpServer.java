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
package org.qiweb.server.jre;

import com.sun.net.httpserver.HttpServer;
import io.werval.runtime.exceptions.WervalRuntimeException;
import io.werval.spi.server.HttpServerAdapter;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.dev.DevShellSPI;
import java.io.IOException;
import java.net.InetSocketAddress;

import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ADDRESS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_PORT;
import static io.werval.runtime.ConfigKeys.WERVAL_SHUTDOWN_TIMEOUT;

/**
 * com.sun.net HTTP Server.
 *
 * Support synchronous HTTP controllers only.
 * <p>
 * Work in progress!
 * <p>
 * Use at your own risk!
 */
public class ComSunHttpServer
    extends HttpServerAdapter
{
    private HttpServer server;

    public ComSunHttpServer()
    {
        super();
    }

    public ComSunHttpServer( ApplicationSPI app, DevShellSPI devSpi )
    {
        super( app, devSpi );
    }

    @Override
    protected void activateHttpServer()
    {
        try
        {
            server = HttpServer.create();
        }
        catch( IOException ex )
        {
            throw new WervalRuntimeException( "Unable to create HttpServer", ex );
        }

        server.createContext( "/", new QiWebHttpHandler( app, devSpi ) );

        String address = app.config().string( WERVAL_HTTP_ADDRESS );
        int port = app.config().intNumber( WERVAL_HTTP_PORT );

        try
        {
            server.bind( new InetSocketAddress( address, port ), 0 );
            server.start();
        }
        catch( IOException ex )
        {
            throw new WervalRuntimeException(
                "Unable to bind to http(s)://" + address + ":" + port + "/ " + "Port already in use?",
                ex
            );
        }
    }

    @Override
    protected void passivateHttpServer()
    {
        if( server != null )
        {
            // app.config() can be null if activation failed, allow gracefull shutdown
            long shutdownTimeout = app.config() == null ? 5 : app.config().seconds( WERVAL_SHUTDOWN_TIMEOUT );
            server.stop( (int) shutdownTimeout );
        }
    }
}
