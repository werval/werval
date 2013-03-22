package org.qiweb.runtime.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.QiWebApplication;
import org.qiweb.spi.dev.DevShellSPI;

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
    private final QiWebApplication httpApp;
    private final DevShellSPI devSPI;
    private final EventExecutorGroup httpExecutors;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, QiWebApplication httpApp, DevShellSPI devSPI )
    {
        this.allChannels = allChannels;
        this.httpApp = httpApp;
        this.devSPI = devSPI;
        // TODO Executors count configuration
        this.httpExecutors = new DefaultEventExecutorGroup( devSPI == null ? DEFAULT_POOL_SIZE : 1,
                                                            new ExecutorsThreadFactory() );
    }

    @Override
    public void initChannel( Channel channel )
        throws Exception
    {
        ChannelPipeline pipeline = channel.pipeline();

        // Log Netty Bytes
        // pipeline.addLast( "byte-logging", new ByteLoggingHandler());

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

        // Transform multiple messages into a single FullHttpRequest or FullHttpResponse.
        // TODO FIXME This should be removed to support more protocols
        pipeline.addLast( "http-aggregator", new HttpObjectAggregator( 1048576 ) );

        // Log Netty Messages
        // pipeline.addLast( "message-logging", new MessageLoggingHandler() );

        // GZip compression support
        pipeline.addLast( "http-compressor", new HttpContentCompressor() );

        // Allow to send chunked data
        pipeline.addLast( "chunked-write-handler", new ChunkedWriteHandler() );

        // Protocol Switching Handler
        pipeline.addLast( "subprotocol-switcher", new SubProtocolSwitchHandler( allChannels, httpExecutors, httpApp, devSPI ) );
    }
}
