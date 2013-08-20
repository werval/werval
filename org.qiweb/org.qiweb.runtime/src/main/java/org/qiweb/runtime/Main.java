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
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServer;
import org.qiweb.runtime.server.HttpServerInstance;

/**
 * QiWeb HTTP Development Kit default main class.
 */
public final class Main
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

    public static void main( String[] args )
    {
        System.out.println( "QiWeb!" );
        try
        {
            RoutesProvider routesProvider = new RoutesConfProvider();
            ApplicationInstance application = new ApplicationInstance( Mode.PROD, routesProvider );
            final HttpServer server = new HttpServerInstance( "qiweb-http-server", application );
            Runtime.getRuntime().addShutdownHook( new Thread( new ShutdownHook( server ), "qiweb-shutdown" ) );
            server.activate();
        }
        catch( Exception ex )
        {
            System.err.println( "Unable to start application." );
            ex.printStackTrace( System.err );
            System.exit( 2 );
        }
    }

    private Main()
    {
    }
}
