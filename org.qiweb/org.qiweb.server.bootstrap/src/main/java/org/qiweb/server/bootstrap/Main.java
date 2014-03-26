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
package org.qiweb.server.bootstrap;

import org.qiweb.api.Mode;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.server.netty.NettyServer;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;

/**
 * QiWeb HTTP Server Bootstrap Main Class.
 */
public final class Main
{
    private static final Logger LOG = LoggerFactory.getLogger( Main.class );

    public static void main( String[] args )
    {
        long start = System.currentTimeMillis();
        LOG.debug( "Starting QiWeb!" );
        try
        {
            ApplicationSPI app = new ApplicationInstance( Mode.PROD );
            HttpServer server = new NettyServer( app );
            server.registerPassivationShutdownHook();
            server.activate();
            if( LOG.isInfoEnabled() )
            {
                String address = app.config().string( QIWEB_HTTP_ADDRESS );
                int port = app.config().intNumber( QIWEB_HTTP_PORT );
                LOG.info( "Ready for requests on http(s)://{}:{} - Took {}ms",
                          address, port, System.currentTimeMillis() - start );
            }
        }
        catch( Exception ex )
        {
            LOG.error( "Unable to start application.", ex );
            System.exit( 2 );
        }
    }

    private Main()
    {
    }
}
