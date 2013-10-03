/**
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    protected void channelRead0( ChannelHandlerContext context, Object message )
        throws Exception
    {
        if( message instanceof HttpRequest )
        {
            HttpRequest request = (HttpRequest) message;
            LOG.trace( "Switching to plain HTTP protocol" );
            context.pipeline().addLast( "http-aggregator", new HttpOnDiskRequestAggregator( app, -1 ) );
            context.pipeline().addLast( httpExecutors, "router", new HttpRequestRouterHandler( app, devSpi ) );
            context.pipeline().remove( this );
            context.fireChannelRead( request );
        }
        else if( message instanceof WebSocketFrame )
        {
            WebSocketFrame frame = (WebSocketFrame) message;
            LOG.trace( "Switching to WebSocket protocol" );
            context.pipeline().addLast( "router", new WebSocketRouterHandler( app, devSpi ) );
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
}
