package org.qiweb.runtime.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import org.qiweb.api.QiWebApplication;
import org.qiweb.spi.dev.DevShellSPI;
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
    private final EventExecutorGroup httpExecutors;
    private final QiWebApplication httpApp;
    private boolean inBoundMessageBufferUpdated = false;
    private final DevShellSPI devSPI;

    public SubProtocolSwitchHandler( ChannelGroup allChannels, EventExecutorGroup httpExecutors, QiWebApplication httpApp, DevShellSPI devSPI )
    {
        super();
        this.allChannels = allChannels;
        this.httpExecutors = httpExecutors;
        this.httpApp = httpApp;
        this.devSPI = devSPI;
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
        // TODO Request Complete!
        super.channelInactive( context );
    }

    @Override
    public boolean beginMessageReceived( ChannelHandlerContext context )
        throws Exception
    {
        inBoundMessageBufferUpdated = false;
        return super.beginMessageReceived( context );
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, Object message )
        throws Exception
    {
        if( message instanceof FullHttpRequest )
        {
            rebuildIfNeeded();
            FullHttpRequest request = (FullHttpRequest) message;
            LOG.trace( "Received a FullHttpRequest message: {}", request );
            LOG.debug( "Switching to plain HTTP protocol" );
            context.pipeline().addLast( httpExecutors, "router", new HttpRouterHandler( httpApp ) );
            context.pipeline().remove( this );
            request.retain();
            context.nextInboundMessageBuffer().add( request );
            inBoundMessageBufferUpdated = true;
        }
        else if( message instanceof WebSocketFrame )
        {
            rebuildIfNeeded();
            WebSocketFrame frame = (WebSocketFrame) message;
            LOG.trace( "Received a WebSocketFrame message: {}", frame );
            LOG.debug( "Switching to WebSocket protocol" );
            context.pipeline().addLast( "router", new WebSocketFrameHandler( httpApp ) );
            context.pipeline().remove( this );
            frame.retain();
            context.nextInboundMessageBuffer().add( frame );
            inBoundMessageBufferUpdated = true;
        }
        else
        {
            LOG.warn( "Received a message of an unknown type ({}), channel will be closed.", message.getClass() );
            context.channel().close();
        }
    }

    @Override
    public void endMessageReceived( ChannelHandlerContext context )
        throws Exception
    {
        if( inBoundMessageBufferUpdated )
        {
            context.fireInboundBufferUpdated();
        }
        super.endMessageReceived( context );
    }

    private void rebuildIfNeeded()
    {
        if( devSPI != null && devSPI.hasMainChanged() )
        {
            devSPI.rebuildMain();
        }
    }
}
