package org.qiweb.runtime.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.qiweb.runtime.http.HttpApplication;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

public class HttpServerInstance
    implements HttpServer
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private final String identity;
    private final String listenAddress;
    private final int listenPort;
    private final HttpApplication httpApp;
    private final DevShellSPI devSPI;
    private final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( String identity, String listenAddress, int listenPort, HttpApplication httpApp )
    {
        this( identity, listenAddress, listenPort, httpApp, null );
    }

    public HttpServerInstance( String identity, String listenAddress, int listenPort, HttpApplication httpApp, DevShellSPI devSPI )
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
        LOG.info( "[{}] Netty Activation", identity );

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them. 
        bootstrap.group( new NioEventLoopGroup( devSPI == null ? 1 : 0 ), new NioEventLoopGroup( devSPI == null ? 1 : 0 ) );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, httpApp, devSPI ) );

        // Bind
        bootstrap.option( TCP_NODELAY, true ); // http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( SO_KEEPALIVE, true ); // http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/
        bootstrap.localAddress( listenAddress, listenPort );
        allChannels.add( bootstrap.bind().sync().channel() );

        LOG.info( "[{}] Netty Activated", identity );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        LOG.info( "[{}] Netty Passivation", identity );
        // allChannels.close().awaitUninterruptibly(); // Not needed anymore with 4.0
        bootstrap.shutdown();
        LOG.info( "[{}] Netty Passivated", identity );
    }
}
