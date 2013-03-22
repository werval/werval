package org.qiweb.runtime.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.qiweb.api.QiWebApplication;
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
    private final String listenAddress;
    private final int listenPort;
    private final QiWebApplication httpApp;
    private final DevShellSPI devSPI;
    private final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( String identity, String listenAddress, int listenPort, QiWebApplication httpApp )
    {
        this( identity, listenAddress, listenPort, httpApp, null );
    }

    public HttpServerInstance( String identity, String listenAddress, int listenPort, QiWebApplication httpApp, DevShellSPI devSPI )
    {
        this.identity = identity;
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
        this.httpApp = httpApp;
        this.allChannels = new DefaultChannelGroup( identity );
        this.devSPI = devSPI;
    }

    @Override
    public void activateService()
        throws Exception
    {
        LOG.debug( "[{}] Netty Activation", identity );

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them. 
        bootstrap.group( new NioEventLoopGroup( devSPI == null ? DEFAULT_POOL_SIZE : 1 ),
                         new NioEventLoopGroup( devSPI == null ? DEFAULT_POOL_SIZE : 1 ) );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, httpApp, devSPI ) );

        // Configuration
        bootstrap.option( TCP_NODELAY, true ); // http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( SO_KEEPALIVE, true ); // http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/

        // Bind
        bootstrap.localAddress( listenAddress, listenPort );
        allChannels.add( bootstrap.bind().sync().channel() );

        LOG.debug( "[{}] Netty Activated", identity );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        LOG.debug( "[{}] Netty Passivation", identity );
        bootstrap.shutdown();
        allChannels.clear();
        LOG.debug( "[{}] Netty Passivated", identity );
    }
}
