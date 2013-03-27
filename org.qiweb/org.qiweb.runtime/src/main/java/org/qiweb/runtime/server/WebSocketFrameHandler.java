package org.qiweb.runtime.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.qiweb.api.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketFrameHandler
    extends ChannelInboundMessageHandlerAdapter<WebSocketFrame>
{

    private static final Logger LOG = LoggerFactory.getLogger( WebSocketFrameHandler.class );
    private final Application app;

    public WebSocketFrameHandler( Application app )
    {
        super();
        this.app = app;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        context.channel().close();
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, WebSocketFrame frame )
    {
        LOG.debug( "Received a WebSocketFrame: {}", frame );
        context.channel().close();
    }
}
