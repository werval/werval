package org.qiweb.runtime.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.qiweb.api.Application;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.util.concurrent.MultithreadEventExecutorGroup.DEFAULT_POOL_SIZE;

public class HttpServerInstance
    implements HttpServer
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private final String identity;
    private final Application app;
    private final DevShellSPI devSpi;
    private final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( String identity, Application app )
    {
        this( identity, app, null );
    }

    public HttpServerInstance( String identity, Application app, DevShellSPI devSpi )
    {
        this.identity = identity;
        this.app = app;
        this.devSpi = devSpi;
        this.allChannels = new DefaultChannelGroup( identity );
    }

    @Override
    public void activateService()
        throws InterruptedException
    {
        LOG.debug( "[{}] Netty Activation", identity );

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them.
        int acceptors = app.config().has( "qiweb.http.acceptors" )
                        ? app.config().getInteger( "qiweb.http.acceptors" )
                        : DEFAULT_POOL_SIZE;
        int iothreads = app.config().has( "qiweb.http.iothreads" )
                        ? app.config().getInteger( "qiweb.http.iothreads" )
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
        bootstrap.localAddress( app.config().getString( "qiweb.http.address" ),
                                app.config().getInteger( "qiweb.http.port" ) );
        allChannels.add( bootstrap.bind().sync().channel() );

        LOG.debug( "[{}] Netty Activated", identity );
    }

    @Override
    public void passivateService()
    {
        LOG.debug( "[{}] Netty Passivation", identity );
        bootstrap.shutdown();
        allChannels.clear();
        LOG.debug( "[{}] Netty Passivated", identity );
    }
}
