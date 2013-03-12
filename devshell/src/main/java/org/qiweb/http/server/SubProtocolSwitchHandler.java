package org.qiweb.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.qiweb.http.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distinguish HttpRequests and WebSocketFrames.
 */
public class SubProtocolSwitchHandler
    extends ChannelInboundMessageHandlerAdapter<Object>
{

    private static final Logger LOG = LoggerFactory.getLogger( SubProtocolSwitchHandler.class );
    private final ChannelGroup allChannels;
    private final Routes routes;
    private boolean inBoundMessageBufferUpdated = false;

    public SubProtocolSwitchHandler( ChannelGroup allChannels, Routes routes )
    {
        super();
        this.allChannels = allChannels;
        this.routes = routes;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
        throws Exception
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        super.exceptionCaught( context, cause );
        context.channel().close().syncUninterruptibly();
    }

    @Override
    public void channelActive( ChannelHandlerContext context )
        throws Exception
    {
        super.channelActive( context );
        allChannels.add( context.channel() );
    }

    @Override
    public void channelInactive( ChannelHandlerContext context )
        throws Exception
    {
        // Request Complete!
        super.channelInactive( context );
    }

    @Override
    protected boolean beginMessageReceived( ChannelHandlerContext context )
        throws Exception
    {
        inBoundMessageBufferUpdated = false;
        return super.beginMessageReceived( context );
    }

    @Override
    protected void messageReceived( ChannelHandlerContext context, Object message )
        throws Exception
    {
        if( message instanceof FullHttpRequest )
        {
            FullHttpRequest request = (FullHttpRequest) message;
            LOG.trace( "Received a FullHttpRequest message: {}", request );
            LOG.debug( "Switching to plain HTTP protocol" );
            context.pipeline().addLast( "router", new HttpRouterHandler( routes ) );
            context.pipeline().remove( this );
            request.retain();
            context.nextInboundMessageBuffer().add( request );
            inBoundMessageBufferUpdated = true;
        }
        else if( message instanceof WebSocketFrame )
        {
            WebSocketFrame frame = (WebSocketFrame) message;
            LOG.trace( "Received a WebSocketFrame message: {}", frame );
            LOG.debug( "Switching to WebSocket protocol" );
            context.pipeline().addLast( "router", new WebSocketFrameHandler( routes ) );
            context.pipeline().remove( this );
            frame.retain();
            context.nextInboundMessageBuffer().add( frame );
            inBoundMessageBufferUpdated = true;
        }
        else
        {
            LOG.error( "Received a message of an unknown type, will return: {}", message );
            //LOG.error( "Received a message of an unknown type (channel will be closed): {}", message );
            //context.channel().close();
        }
    }

    @Override
    protected void endMessageReceived( ChannelHandlerContext context )
        throws Exception
    {
        if( inBoundMessageBufferUpdated )
        {
            context.fireInboundBufferUpdated();
        }
        super.endMessageReceived( context );
    }
}
