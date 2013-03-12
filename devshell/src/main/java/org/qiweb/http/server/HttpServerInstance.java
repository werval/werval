package org.qiweb.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qiweb.http.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

public class HttpServerInstance
    implements HttpServer
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private final String identity;
    private final Routes routes;
    private final ChannelGroup allChannels;
    private HttpServerConfig config;
    private Configuration<HttpServerConfiguration> configuration;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( @This Identity identity,
                               @This Configuration<HttpServerConfiguration> configuration,
                               @Service Routes routes )
    {
        this( identity.identity().get(), HttpServerConfig.of( configuration.get() ), routes );
        this.configuration = configuration;
    }

    public HttpServerInstance( String identity, HttpServerConfig config, Routes routes )
    {
        this.identity = identity;
        this.config = config;
        this.routes = routes;
        this.allChannels = new DefaultChannelGroup( identity );
    }

    @Override
    public void activateService()
        throws Exception
    {
        LOG.info( "[{}] Netty Activation", identity );

        // Configuration refresh?
        if( configuration != null )
        {
            configuration.refresh();
            config = HttpServerConfig.of( configuration.get() );
        }

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them. 
        bootstrap.group( new NioEventLoopGroup(), new NioEventLoopGroup() );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, routes ) );

        // Bind
        bootstrap.option( TCP_NODELAY, true ); // http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( SO_KEEPALIVE, true ); // http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/
        bootstrap.localAddress( config.listenAddress, config.listenPort );
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
