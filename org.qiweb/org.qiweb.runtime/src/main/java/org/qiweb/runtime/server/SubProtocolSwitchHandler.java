package org.qiweb.runtime.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpRequest;
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
    {
        allChannels.add( context.channel() );
    }

    @Override
    public void channelInactive( ChannelHandlerContext context )
    {
        // TODO Request Complete!
    }

    @Override
    public boolean beginMessageReceived( ChannelHandlerContext context )
    {
        inBoundMessageBufferUpdated = false;
        return true;
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, Object message )
    {
        if( message instanceof HttpRequest )
        {
            rebuildIfNeeded();
            HttpRequest request = (HttpRequest) message;
            LOG.debug( "Switching to plain HTTP protocol" );
            context.pipeline().addLast( httpExecutors, "router", new HttpRouterHandler( httpApp ) );
            context.pipeline().remove( this );
            context.nextInboundMessageBuffer().add( request );
            inBoundMessageBufferUpdated = true;
        }
        else if( message instanceof WebSocketFrame )
        {
            rebuildIfNeeded();
            WebSocketFrame frame = (WebSocketFrame) message;
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
    {
        if( inBoundMessageBufferUpdated )
        {
            context.fireInboundBufferUpdated();
        }
    }

    private void rebuildIfNeeded()
    {
        if( devSPI != null && devSPI.hasMainChanged() )
        {
            devSPI.rebuildMain();
        }
    }
}
