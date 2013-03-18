package org.qiweb.devshell;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import java.io.StringWriter;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.util.CharsetUtil.UTF_8;

public class DumbHttpServer
{

    private static class DumbHandler
        extends ChannelInboundMessageHandlerAdapter<FullHttpRequest>
    {

        @Override
        protected void messageReceived( ChannelHandlerContext ctx, FullHttpRequest msg )
            throws Exception
        {
            FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK );
            StringWriter sw = new StringWriter();
            sw.append( "Dumb!" );
            response.data().writeBytes( copiedBuffer( sw.toString(), UTF_8 ) );
            ctx.write( response ).addListener( ChannelFutureListener.CLOSE );
        }
    }
    private final String listenAddress;
    private final int listenPort;
    private ServerBootstrap bootstrap;

    public DumbHttpServer( String listenAddress, int listenPort )
    {
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
    }

    public void start()
        throws InterruptedException
    {
        bootstrap = new ServerBootstrap();
        bootstrap.group( new NioEventLoopGroup(), new NioEventLoopGroup() );
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new ChannelInitializer<Channel>()
        {
            @Override
            protected void initChannel( Channel ch )
                throws Exception
            {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast( "http-codec", new HttpServerCodec() );
                pipeline.addLast( "http-aggregator", new HttpObjectAggregator( 1048576 ) );
                pipeline.addLast( "handler", new DumbHandler() );
            }
        } );
        bootstrap.option( TCP_NODELAY, true );
        bootstrap.option( SO_KEEPALIVE, true );
        bootstrap.localAddress( listenAddress, listenPort );
        bootstrap.bind().sync();
    }

    public void stop()
    {
        bootstrap.shutdown();
    }
}
