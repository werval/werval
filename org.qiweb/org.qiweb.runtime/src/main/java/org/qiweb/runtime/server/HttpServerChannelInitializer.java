package org.qiweb.runtime.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.ByteLoggingHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.Application;
import org.qiweb.spi.dev.DevShellSPI;

import static java.util.concurrent.TimeUnit.*;
import static io.netty.util.concurrent.MultithreadEventExecutorGroup.DEFAULT_POOL_SIZE;

/* package */ class HttpServerChannelInitializer
    extends ChannelInitializer<Channel>
{

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
    private final Application app;
    private final DevShellSPI devSpi;
    private final EventExecutorGroup httpExecutors;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, Application httpApp, DevShellSPI devSpi )
    {
        this.allChannels = allChannels;
        this.app = httpApp;
        this.devSpi = devSpi;
        int executors = app.config().has( "qiweb.http.executors" )
                        ? app.config().intNumber( "qiweb.http.executors" )
                        : DEFAULT_POOL_SIZE;
        this.httpExecutors = new DefaultEventExecutorGroup( devSpi == null ? executors : 1,
                                                            new ExecutorsThreadFactory() );
    }

    @Override
    public void initChannel( Channel channel )
    {
        ChannelPipeline pipeline = channel.pipeline();

        if( app.config().bool( "qiweb.http.log.low-level.enabled" ) )
        {
            // Log Netty Bytes
            LogLevel level = LogLevel.valueOf(
                app.config().string( "qiweb.http.log.low-level.level" ).toUpperCase( Locale.US ) );
            pipeline.addLast( "byte-logging", new ByteLoggingHandler( level ) );
        }

        // Read/Write Timeout
        long readTimeout = app.config().seconds( "qiweb.http.timeout.read" );
        long writeTimeout = app.config().seconds( "qiweb.http.timeout.write" );
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
