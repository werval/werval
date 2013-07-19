package org.qiweb.runtime.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distinguish HttpRequests and WebSocketFrames.
 */
public class SubProtocolSwitchHandler
    extends SimpleChannelInboundHandler<Object>
{

    private static final Logger LOG = LoggerFactory.getLogger( SubProtocolSwitchHandler.class );
    private final ChannelGroup allChannels;
    private final EventExecutorGroup httpExecutors;
    private final ApplicationInstance app;
    private final DevShellSPI devSpi;

    public SubProtocolSwitchHandler( ChannelGroup allChannels, EventExecutorGroup httpExecutors,
                                     ApplicationInstance app, DevShellSPI devSpi )
    {
        super();
        this.allChannels = allChannels;
        this.httpExecutors = httpExecutors;
        this.app = app;
        this.devSpi = devSpi;
    }

    @Override
    public void channelActive( ChannelHandlerContext context )
    {
        allChannels.add( context.channel() );
    }

    @Override
    public void channelInactive( ChannelHandlerContext context )
    {
        // TODO Invoke Global.onHttpRequestComplete!
        // We probably need to eventually attach the RequestHeader to the channel
    }

    @Override
    protected void channelRead0( ChannelHandlerContext context, Object message )
        throws Exception
    {
        if( message instanceof HttpRequest )
        {
            rebuildIfNeeded();
            HttpRequest request = (HttpRequest) message;
            LOG.debug( "Switching to plain HTTP protocol" );
            context.pipeline().addLast( httpExecutors, "router", new HttpRouterHandler( app ) );
            context.pipeline().remove( this );
            context.fireChannelRead( request );
        }
        else if( message instanceof WebSocketFrame )
        {
            rebuildIfNeeded();
            WebSocketFrame frame = (WebSocketFrame) message;
            LOG.debug( "Switching to WebSocket protocol" );
            context.pipeline().addLast( "router", new WebSocketFrameHandler( app ) );
            context.pipeline().remove( this );
            frame.retain();
            context.fireChannelRead( frame );
        }
        else
        {
            LOG.warn( "Received a message of an unknown type ({}), channel will be closed.", message.getClass() );
            context.channel().close();
        }
    }

    private void rebuildIfNeeded()
    {
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            devSpi.rebuild();
        }
    }
}
