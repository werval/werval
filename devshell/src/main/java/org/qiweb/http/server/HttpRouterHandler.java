package org.qiweb.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.ArrayList;
import java.util.List;
import org.codeartisans.java.toolbox.Couple;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qiweb.http.controllers.Result;
import org.qiweb.http.routes.Route;
import org.qiweb.http.routes.RouteNotFoundException;
import org.qiweb.http.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * Handle plain HTTP and WebSocket UPGRADE requests.
 */
public class HttpRouterHandler
    extends ChannelInboundMessageHandlerAdapter<FullHttpRequest>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpRouterHandler.class );
    private final Routes routes;

    public HttpRouterHandler( Routes routes )
    {
        super();
        this.routes = routes;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
        throws Exception
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        super.exceptionCaught( context, cause );
        context.channel().close().syncUninterruptibly();
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, FullHttpRequest request )
        throws Exception
    {
        LOG.debug( "Received a FullHttpRequest: {}", request );
        final boolean keepAlive = isKeepAlive( request );
        HttpVersion httpVersion = request.getProtocolVersion();

        FullHttpResponse response;
        try
        {
            final Route route = routes.route( request );
            LOG.debug( "Will route request to: {}", route );

            // Parse path parameters
            List<Object> pathParams = pathParameters( request, route );

            // Assemble and activate a Qi4j Application
            SingletonAssembler assembler = new SingletonAssembler()
            {
                @Override
                public void assemble( ModuleAssembly ma )
                    throws AssemblyException
                {
                    ma.services( route.controllerType() );
                }
            };
            Module module = assembler.module();

            // Lookup Controller Service
            Object controller = module.findService( route.controllerType() ).get();

            // Invoke its method
            LOG.debug( "Will invoke controller method: {}", route.controllerMethod() );
            Result result = (Result) route.controllerMethod().invoke( controller, pathParams.toArray() );

            // Build the response

            // Status - Wait for it to be set by controller
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( result.status() );
            response = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );

            // Headers - Wait for them to be set by controller
            for( String headerName : result.headers().keySet() )
            {
                response.headers().add( headerName, result.headers().get( headerName ) );
            }
            if( keepAlive && httpVersion == HTTP_1_1 )
            {
                response.headers().set( CONNECTION, KEEP_ALIVE );
            }
            // Body - Wait for it to be set by controller
            ByteBuf responseBody = result.body();
            response.data().writeBytes( responseBody );

            // response.data().writeBytes( copiedBuffer( "Route match: " + route.toString() + "\r\n", UTF_8 ) );
            // response.data().writeBytes( copiedBuffer( "Result: " + result + "\r\n", UTF_8 ) );

            // Passivate the Qi4j Application
            assembler.application().passivate();

        }
        catch( RouteNotFoundException ex )
        {
            response = new DefaultFullHttpResponse( HTTP_1_1, NOT_FOUND );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=UTF-8" );
            response.data().writeBytes( copiedBuffer( "404 Route Not Found\nTried:\n" + routes.toString() + "\n", UTF_8 ) );
        }

        // Write response
        ChannelFuture writeFuture = context.write( response );

        // Close the connection as soon as the response is sent if not keep alive
        if( true || !keepAlive )
        {
            writeFuture.addListener( ChannelFutureListener.CLOSE );
        }
    }

    private List<Object> pathParameters( HttpRequest request, Route route )
    {
        String requestPath = new QueryStringDecoder( request.getUri(), UTF_8 ).path();
        List<Object> pathParams = new ArrayList<Object>();
        for( Couple<String, Class<?>> controllerParam : route.controllerParams() )
        {
            String paramName = controllerParam.left();
            Class<?> paramType = controllerParam.right();
            String paramStringValue = route.controllerParamPathValue( paramName, requestPath );
            if( Integer.class.isAssignableFrom( paramType ) )
            {
                pathParams.add( Integer.valueOf( paramStringValue ) );
            }
            else
            {
                pathParams.add( paramStringValue );
            }
        }
        return pathParams;
    }
}
