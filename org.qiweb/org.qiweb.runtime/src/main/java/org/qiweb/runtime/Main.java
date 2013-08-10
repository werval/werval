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

    private static class ShutdownHook
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
            ApplicationInstance application = new ApplicationInstance( Mode.prod, routesProvider );
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
