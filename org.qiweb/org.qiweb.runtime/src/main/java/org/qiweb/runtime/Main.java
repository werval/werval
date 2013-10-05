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
package org.qiweb.runtime;

import org.qiweb.api.Application.Mode;
import org.qiweb.runtime.server.HttpServer;
import org.qiweb.runtime.server.HttpServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;

/**
 * QiWeb HTTP Development Kit default main class.
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
            ApplicationInstance app = new ApplicationInstance( Mode.PROD );
            HttpServer server = new HttpServerInstance( "qiweb-http-server", app );
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
