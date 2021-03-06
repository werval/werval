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
package io.werval.server.netty;

import java.net.InetSocketAddress;

import io.werval.api.events.ConnectionEvent;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.dev.DevShellSPI;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;

import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_LOG_LOWLEVEL_ENABLED;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_LOG_LOWLEVEL_LEVEL;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_TIMEOUT_READ;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_TIMEOUT_WRITE;

/* package */ class HttpServerChannelInitializer
    extends ChannelInitializer<Channel>
{
    private final ChannelGroup allChannels;
    private final ApplicationSPI app;
    private final DevShellSPI devSpi;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, ApplicationSPI httpApp, DevShellSPI devSpi )
    {
        this.allChannels = allChannels;
        this.app = httpApp;
        this.devSpi = devSpi;
    }

    @Override
    public void initChannel( Channel channel )
    {
        ChannelPipeline pipeline = channel.pipeline();

        // Connection Events
        String remoteHostString = ( (InetSocketAddress) channel.remoteAddress() ).getHostString();
        app.events().emit( new ConnectionEvent.Opened( remoteHostString ) );
        channel.closeFuture().addListener(
            future -> app.events().emit( new ConnectionEvent.Closed( remoteHostString ) )
        );

        if( app.config().bool( WERVAL_HTTP_LOG_LOWLEVEL_ENABLED ) )
        {
            // Log Netty Bytes
            LogLevel level = LogLevel.valueOf(
                app.config().string( WERVAL_HTTP_LOG_LOWLEVEL_LEVEL ).toUpperCase( US )
            );
            pipeline.addLast( "byte-logging", new LoggingHandler( "io.werval.server.netty.LowLevelLogger", level ) );
        }

        // Read/Write Timeout
        long readTimeout = app.config().seconds( WERVAL_HTTP_TIMEOUT_READ );
        long writeTimeout = app.config().seconds( WERVAL_HTTP_TIMEOUT_WRITE );
        pipeline.addLast( "read-timeout", new ReadTimeoutHandler( readTimeout, SECONDS ) );
        pipeline.addLast( "write-timeout", new WriteTimeoutHandler( writeTimeout, SECONDS ) );

        // HTTP Decoding / Encoding
        // HTTP decoders always generates multiple message objects per a single HTTP message:
        //
        //  1       * HttpRequest / HttpResponse
        //  0 - n   * HttpContent
        //  1       * LastHttpContent
        //
        // or a single FullHttpRequest if a handler ask for it
        pipeline.addLast( "http-codec", new HttpServerCodec() );

        // GZip decompression support
        pipeline.addLast( "http-decompressor", new HttpContentDecompressor() );

        // Allow to send chunked data
        pipeline.addLast( "chunked-write-handler", new ChunkedWriteHandler() );

        // Protocol Switching Handler
        pipeline.addLast( "subprotocol-switcher", new SubProtocolSwitchHandler( allChannels, app, devSpi ) );
    }
}
