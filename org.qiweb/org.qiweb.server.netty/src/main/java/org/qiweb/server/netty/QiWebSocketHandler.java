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
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellRebuildException;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle WebSockets.
 */
public class QiWebSocketHandler
    extends SimpleChannelInboundHandler<WebSocketFrame>
{
    private static final Logger LOG = LoggerFactory.getLogger( QiWebSocketHandler.class );
    private final ApplicationSPI app;
    private final DevShellSPI devSpi;

    public QiWebSocketHandler( ApplicationSPI app, DevShellSPI devSpi )
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
        rebuildIfNeeded();
        LOG.warn( "Received a WebSocketFrame, unsupported, closing connection: {}", frame );
        context.channel().close();
    }

    private void rebuildIfNeeded()
    {
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            try
            {
                devSpi.rebuild();
            }
            catch( Exception ex )
            {
                throw new DevShellRebuildException( ex );
            }
        }
    }
}
