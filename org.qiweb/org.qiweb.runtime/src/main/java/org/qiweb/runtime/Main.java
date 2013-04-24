package org.qiweb.runtime;

import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.Application.Mode;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServer;
import org.qiweb.runtime.server.HttpServerInstance;

/**
 * QiWeb HTTP Development Kit default main class.
 */
public final class Main
{

    public static void main( String[] args )
    {
        System.out.println( "QiWeb!" );
        URL routesUrl = Main.class.getClassLoader().getResource( "routes.conf" );
        if( routesUrl == null )
        {
            System.err.println( "No routes.conf file found at classpath root." );
            System.exit( 1 );
        }
        try( InputStream input = routesUrl.openStream() )
        {
            RoutesProvider routesProvider = new RoutesParserProvider(
                Strings.toString( new InputStreamReader( input, CharsetUtil.UTF_8 ) ) );
            final HttpServer server = new HttpServerInstance( "qiweb-http-server",
                                                              new ApplicationInstance( Mode.prod, routesProvider ) );
            Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    server.passivate();
                }
            }, "qiweb-shutdown" ) );
            server.activate();
        }
        catch( IOException ex )
        {
            System.err.println( "Unable to read routes.conf." );
            ex.printStackTrace( System.err );
            System.exit( 2 );
        }

    }

    private Main()
    {
    }
}
