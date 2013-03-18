package org.qiweb.runtime.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.qiweb.runtime.http.HttpApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketFrameHandler
    extends ChannelInboundMessageHandlerAdapter<WebSocketFrame>
{

    private static final Logger LOG = LoggerFactory.getLogger( WebSocketFrameHandler.class );
    private final HttpApplication httpApp;

    public WebSocketFrameHandler( HttpApplication httpApp )
    {
        super();
        this.httpApp = httpApp;
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
    protected void messageReceived( ChannelHandlerContext context, WebSocketFrame frame )
        throws Exception
    {
        LOG.debug( "Received a WebSocketFrame: {}", frame );
    }
}
