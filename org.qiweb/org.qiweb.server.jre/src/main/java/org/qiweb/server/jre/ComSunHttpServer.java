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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.spi.server.HttpServerAdapter;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_EXECUTORS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_TIMEOUT;

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
            throw new QiWebRuntimeException( "Unable to create HttpServer", ex );
        }

        final Executor executor;
        if( devSpi != null )
        {
            executor = null;
        }
        else if( app.config().has( QIWEB_HTTP_EXECUTORS ) )
        {
            int executors = app.config().intNumber( QIWEB_HTTP_EXECUTORS );
            if( executors <= 0 )
            {
                // Config set to 0, no controller executors
                executor = null;
            }
            else
            {
                // Configured controller executors count
                executor = Executors.newFixedThreadPool( executors );
            }
        }
        else
        {
            executor = null;
        }
        server.setExecutor( executor );

        server.createContext( "/", new QiWebHttpHandler( app, devSpi ) );

        String address = app.config().string( QIWEB_HTTP_ADDRESS );
        int port = app.config().intNumber( QIWEB_HTTP_PORT );

        try
        {
            server.bind( new InetSocketAddress( address, port ), 0 );
            server.start();
        }
        catch( IOException ex )
        {
            throw new QiWebRuntimeException( "Unable to bind to http(s)://" + address + ":" + port + "/ "
                                             + "Port already in use?", ex );
        }
    }

    @Override
    protected void passivateHttpServer()
    {
        if( server != null )
        {
            // app.config() can be null if activation failed, allow gracefull shutdown
            long shutdownTimeout = app.config() == null ? 5 : app.config().seconds( QIWEB_SHUTDOWN_TIMEOUT );
            server.stop( (int) shutdownTimeout );
        }
    }
}