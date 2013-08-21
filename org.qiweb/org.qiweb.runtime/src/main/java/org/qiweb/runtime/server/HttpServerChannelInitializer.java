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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.spi.dev.DevShellSPI;

import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_EXECUTORS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_LOG_LOWLEVEL_ENABLED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_LOG_LOWLEVEL_LEVEL;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_TIMEOUT_READ;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_TIMEOUT_WRITE;

/* package */ class HttpServerChannelInitializer
    extends ChannelInitializer<Channel>
{

    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;

    private static class ExecutorsThreadFactory
        implements ThreadFactory
    {

        private final AtomicLong count = new AtomicLong( 0L );

        @Override
        public Thread newThread( Runnable runnable )
        {
            return new Thread( runnable, "http-executor-" + count.getAndIncrement() );
        }
    }
    private final ChannelGroup allChannels;
    private final ApplicationInstance app;
    private final DevShellSPI devSpi;
    private final EventExecutorGroup httpExecutors;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, ApplicationInstance httpApp, DevShellSPI devSpi )
    {
        this.allChannels = allChannels;
        this.app = httpApp;
        this.devSpi = devSpi;
        int executors = app.config().has( QIWEB_HTTP_EXECUTORS )
                        ? app.config().intNumber( QIWEB_HTTP_EXECUTORS )
                        : DEFAULT_POOL_SIZE;
        this.httpExecutors = new DefaultEventExecutorGroup( devSpi == null ? executors : 1,
                                                            new ExecutorsThreadFactory() );
    }

    @Override
    public void initChannel( Channel channel )
    {
        ChannelPipeline pipeline = channel.pipeline();

        if( app.config().bool( QIWEB_HTTP_LOG_LOWLEVEL_ENABLED ) )
        {
            // Log Netty Bytes
            LogLevel level = LogLevel.valueOf( app.config().string( QIWEB_HTTP_LOG_LOWLEVEL_LEVEL ).toUpperCase( US ) );
            pipeline.addLast( "byte-logging", new LoggingHandler( level ) );
        }

        // Read/Write Timeout
        long readTimeout = app.config().seconds( QIWEB_HTTP_TIMEOUT_READ );
        long writeTimeout = app.config().seconds( QIWEB_HTTP_TIMEOUT_WRITE );
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

        // Aggregate chunked HttpRequests to disk
        // TODO Move the aggregator to SubProtocolSwitchHandler or ensure it won't mangle with WebSockets
        pipeline.addLast( httpExecutors, "http-aggregator", new HttpOnDiskRequestAggregator( app, -1 ) );

        // Allow to send chunked data
        pipeline.addLast( "chunked-write-handler", new ChunkedWriteHandler() );

        // Protocol Switching Handler
        pipeline.addLast( "subprotocol-switcher", new SubProtocolSwitchHandler( allChannels, httpExecutors, app, devSpi ) );
    }
}
