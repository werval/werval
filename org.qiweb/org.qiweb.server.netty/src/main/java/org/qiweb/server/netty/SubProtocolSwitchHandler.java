/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.dev.DevShellSPI;
import io.werval.spi.server.HttpServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_REQUESTS_BODY_DISK_THRESHOLD;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_REQUESTS_BODY_MAX_SIZE;

/**
 * Distinguish HttpRequests and WebSocketFrames and setup the pipeline accordingly.
 */
public class SubProtocolSwitchHandler
    extends SimpleChannelInboundHandler<Object>
{
    private static final Logger LOG = LoggerFactory.getLogger( SubProtocolSwitchHandler.class );
    private final ChannelGroup allChannels;
    private final ApplicationSPI app;
    private final DevShellSPI devSpi;
    private final HttpServerHelper helper = new HttpServerHelper();

    public SubProtocolSwitchHandler( ChannelGroup allChannels, ApplicationSPI app, DevShellSPI devSpi )
    {
        super();
        this.allChannels = allChannels;
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
            ChannelPipeline pipeline = context.pipeline();

            int maxBodySize = app.config().intNumber( WERVAL_HTTP_REQUESTS_BODY_MAX_SIZE );
            int diskThreshold = app.config().intNumber( WERVAL_HTTP_REQUESTS_BODY_DISK_THRESHOLD );
            pipeline.addLast(
                "http-aggregator",
                new HttpRequestAggregator( helper, app.events(), maxBodySize, diskThreshold, app.tmpdir() )
            );
            pipeline.addLast(
                "qiweb-http",
                new QiWebHttpHandler( app, devSpi )
            );

            pipeline.remove( this );

            context.fireChannelRead( request );
        }
        else if( message instanceof WebSocketFrame )
        {
            WebSocketFrame frame = (WebSocketFrame) message;
            LOG.trace( "Switching to WebSocket protocol" );
            ChannelPipeline pipeline = context.pipeline();

            pipeline.addLast(
                "qiweb-websocket",
                new QiWebSocketHandler( app, devSpi )
            );

            pipeline.remove( this );

            frame.retain(); // TODO Check this
            context.fireChannelRead( frame );
        }
        else
        {
            LOG.warn( "Received a message of an unknown type ({}), channel will be closed.", message.getClass() );
            context.channel().close();
        }
    }
}
