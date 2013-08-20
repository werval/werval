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
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.qiweb.api.Application;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketRouterHandler
    extends SimpleChannelInboundHandler<WebSocketFrame>
{

    private static final Logger LOG = LoggerFactory.getLogger( WebSocketRouterHandler.class );
    private final Application app;
    private final DevShellSPI devSpi;

    public WebSocketRouterHandler( Application app, DevShellSPI devSpi )
    {
        super();
        this.app = app;
        this.devSpi = devSpi;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        context.channel().close();
    }

    @Override
    protected void channelRead0( ChannelHandlerContext context, WebSocketFrame frame )
    {
        LOG.debug( "Received a WebSocketFrame: {}", frame );
        context.channel().close();
    }
}
