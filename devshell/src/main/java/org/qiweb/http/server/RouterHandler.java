package org.qiweb.http.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import org.qiweb.http.routes.Route;
import org.qiweb.http.routes.RouteNotFoundException;
import org.qiweb.http.routes.Routes;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

public class RouterHandler
    extends ChannelInboundMessageHandlerAdapter<HttpRequest>
{

    private final Routes routes;

    public RouterHandler( Routes routes )
    {
        super();
        this.routes = routes;
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, HttpRequest httpRequest )
        throws Exception
    {
        FullHttpResponse response;
        try
        {
            Route route = routes.route( httpRequest );

            response = new DefaultFullHttpResponse( HTTP_1_1, OK );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=UTF-8" );
            response.data().writeBytes( copiedBuffer( "Route match: " + route.toString() + "\r\n", UTF_8 ) );

        }
        catch( RouteNotFoundException ex )
        {
            response = new DefaultFullHttpResponse( HTTP_1_1, NOT_FOUND );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=UTF-8" );
            response.data().writeBytes( copiedBuffer( "404 Route Not Found\nTried:\n" + routes.toString() + "\n", UTF_8 ) );
        }
        // Close the connection as soon as the error message is sent.
        context.write( response ).addListener( new ChannelFutureListener()
        {
            @Override
            public void operationComplete( ChannelFuture future )
                throws Exception
            {
                future.channel().close();
            }
        } );
    }
}
