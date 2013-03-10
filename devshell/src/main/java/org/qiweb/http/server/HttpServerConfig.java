package org.qiweb.http.server;

import org.codeartisans.java.toolbox.exceptions.NullArgumentException;

/**
 * Configuration for HttpServer.
 */
public class HttpServerConfig
{

    /* package */ final String listenAddress;
    /* package */ final int listenPort;

    public HttpServerConfig( String listenAddress, int listenPort )
    {
        this( listenAddress, listenPort, true );
    }

    private HttpServerConfig( String listenAddress, int listenPort, boolean validate )
    {
        if( validate )
        {
            NullArgumentException.ensureNotEmpty( "Listen address", listenAddress );
            if( listenPort < 1 || listenPort > 65535 )
            {
                throw new IllegalArgumentException( "Invalid listen port: " + listenPort );
            }
        }
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
    }

    /* package */ static HttpServerConfig of( HttpServerConfiguration configuration )
    {
        return new HttpServerConfig( configuration.listenAddress().get(), configuration.listenPort().get(), false );
    }
}
