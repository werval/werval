package org.qiweb.runtime.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

public class HttpServerInstance
    implements HttpServer
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
    private final String identity;
    private final ApplicationInstance app;
    private final DevShellSPI devSpi;
    private final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( String identity, ApplicationInstance app )
    {
        this( identity, app, null );
    }

    public HttpServerInstance( String identity, ApplicationInstance app, DevShellSPI devSpi )
    {
        this.identity = identity;
        this.app = app;
        this.devSpi = devSpi;
        this.allChannels = new DefaultChannelGroup(identity,null);
    }

    @Override
    public void activate()
        throws QiWebRuntimeException
    {
        app.global().beforeHttpBind( app );

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them.
        int acceptors = app.config().has( "qiweb.http.acceptors" )
                        ? app.config().intNumber( "qiweb.http.acceptors" )
                        : DEFAULT_POOL_SIZE;
        int iothreads = app.config().has( "qiweb.http.iothreads" )
                        ? app.config().intNumber( "qiweb.http.iothreads" )
                        : DEFAULT_POOL_SIZE;
        bootstrap.group( new NioEventLoopGroup( devSpi == null ? acceptors : 1 ),
                         new NioEventLoopGroup( devSpi == null ? iothreads : 1 ) );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, app, devSpi ) );

        // See http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( TCP_NODELAY, true );
        // See http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/
        bootstrap.option( SO_KEEPALIVE, true );

        // Bind
        String address = app.config().string( "qiweb.http.address" );
        int port = app.config().intNumber( "qiweb.http.port" );
        try
        {
            bootstrap.localAddress( address, port );
            allChannels.add( bootstrap.bind().sync().channel() );
        }
        catch( InterruptedException ex )
        {
            throw new QiWebRuntimeException( "Unable to bind to http://" + address + ":" + port + "/ "
                                             + "Port already in use?", ex );
        }

        LOG.debug( "[{}] Http Service Listening on http(s)://{}:{}", identity, address, port );

        app.global().afterHttpBind( app );
    }

    @Override
    public void passivate()
    {
        app.global().beforeHttpUnbind( app );

        // Unbind Netty
        bootstrap.group().shutdownGracefully();
        allChannels.clear();

        LOG.debug( "[{}] Http Service Passivated", identity );

        app.global().afterHttpUnbind( app );
    }
}
