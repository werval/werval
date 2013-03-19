package org.qiweb.runtime.http.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.runtime.http.HttpApplication;
import org.qiweb.spi.dev.DevShellSPI;

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
    private final HttpApplication httpApp;
    private final DevShellSPI devSPI;
    private final EventExecutorGroup httpExecutors;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, HttpApplication httpApp, DevShellSPI devSPI )
    {
        this.allChannels = allChannels;
        this.httpApp = httpApp;
        this.devSPI = devSPI;
        // TODO Executors count configuration
        this.httpExecutors = new DefaultEventExecutorGroup( devSPI == null ? 0 : 1, new ExecutorsThreadFactory() );
    }

    @Override
    public void initChannel( Channel channel )
        throws Exception
    {
        ChannelPipeline pipeline = channel.pipeline();

        // HTTP Decoding / Encoding
        // HTTP decoders always generates multiple message objects per a single HTTP message:
        //
        //  1       * HttpRequest / HttpResponse
        //  0 - n   * HttpContent
        //  1       * LastHttpContent
        //
        // or a single FullHttpRequest if a handler ask for it
        pipeline.addLast( "http-codec", new HttpServerCodec() );

        // Transform multiple messages into a single FullHttpRequest or FullHttpResponse. 
        pipeline.addLast( "http-aggregator", new HttpObjectAggregator( 1048576 ) );

        // Get the hand to the Router
        pipeline.addLast( "subprotocol-switcher", new SubProtocolSwitchHandler( allChannels, httpExecutors, httpApp, devSPI ) );
    }
}
