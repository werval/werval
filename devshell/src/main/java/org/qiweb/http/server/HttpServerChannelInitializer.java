package org.qiweb.http.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.qiweb.http.routes.Routes;

/* package */ class HttpServerChannelInitializer
    extends ChannelInitializer<Channel>
{

    private final ChannelGroup allChannels;
    private final Routes routes;

    /* package */ HttpServerChannelInitializer( ChannelGroup allChannels, Routes routes )
    {
        this.allChannels = allChannels;
        this.routes = routes;
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
        pipeline.addLast( "http-codec", new HttpServerCodec() );

        // Transform multiple messages into a single FullHttpRequest or FullHttpResponse. 
        pipeline.addLast( "http-aggregator", new HttpObjectAggregator( 1048576 ) );

        // Get the hand to the Router
        // EventExecutorGroup executors = new DefaultEventExecutorGroup( 0 ); // You'll want to raise this!
        pipeline.addLast( "subprotocol-switcher", new SubProtocolSwitchHandler( allChannels, routes ) );
    }
}
