package org.qiweb.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qiweb.http.controllers.Result;
import org.qiweb.http.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

public class HttpServerInstance
    implements HttpServer
{

    public static interface FakeController
    {

        Result test();

        Result another( String id, Integer slug );

        Result index();

        Result foo();

        Result bar();
    }

    private static class HttpServerChannelInitializer
        extends ChannelInitializer<Channel>
    {

        private final Routes routes;

        private HttpServerChannelInitializer( Routes routes )
        {
            this.routes = routes;
        }

        @Override
        public void initChannel( Channel channel )
            throws Exception
        {
            ChannelPipeline pipeline = channel.pipeline();

            // HTTP Decoding / Encoding
            pipeline.addLast( "decoder", new HttpRequestDecoder( 4096, 8192, 8192 ) );
            pipeline.addLast( "encoder", new HttpResponseEncoder() );
            // pipeline.addLast("chunked-writer", new ChunkedWriteHandler());

            // Get the hand to the Router
            pipeline.addLast( "handler", new RouterHandler( routes ) );
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private final String identity;
    private final Routes routes;
    private final ChannelGroup allChannels;
    private HttpServerConfig config;
    private Configuration<HttpServerConfiguration> configuration;

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
        ServerBootstrap bootstrap = new ServerBootstrap();

        // Event Loops
        EventLoopGroup acceptorEventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup clientsEventLoopGroup = new NioEventLoopGroup();
        bootstrap.group( acceptorEventLoopGroup, clientsEventLoopGroup );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( routes ) );

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
        allChannels.close().awaitUninterruptibly();
        LOG.info( "[{}] Netty Passivated", identity );
    }
}
