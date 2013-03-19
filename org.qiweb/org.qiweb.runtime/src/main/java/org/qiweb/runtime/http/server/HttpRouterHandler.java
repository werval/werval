package org.qiweb.runtime.http.server;

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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.codeartisans.java.toolbox.Couple;
import org.qiweb.runtime.http.HttpApplication;
import org.qiweb.api.http.Result;
import org.qiweb.runtime.http.controllers.Results.ChunkedResult;
import org.qiweb.runtime.http.controllers.Results.SimpleResult;
import org.qiweb.runtime.http.controllers.Results.StreamResult;
import org.qiweb.runtime.http.routes.Route;
import org.qiweb.runtime.http.routes.RouteNotFoundException;
import org.qiweb.runtime.util.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
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
    private final HttpApplication httpApp;

    public HttpRouterHandler( HttpApplication httpApp )
    {
        super();
        this.httpApp = httpApp;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
        throws Exception
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, INTERNAL_SERVER_ERROR );
        response.headers().set( CONTENT_TYPE, "text/plain; charset=UTF-8" );
        StringWriter sw = new StringWriter();
        sw.append( "500 Internal Server Error\n" ).append( cause.getMessage() ).append( "\n" );
        cause.printStackTrace( new PrintWriter( sw ) );
        sw.append( "\n\nClassLoaders state:\n\n" );
        ClassLoaders.printLoadedClasses( Thread.currentThread().getContextClassLoader(), new PrintWriter( sw ) );
        sw.append( "\n" );
        response.data().writeBytes( copiedBuffer( sw.toString(), UTF_8 ) );
        context.write( response ).addListener( ChannelFutureListener.CLOSE );
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, FullHttpRequest request )
        throws Exception
    {

        LOG.debug( "In Thread '{}' using classloader '{}'",
                   Thread.currentThread().getName(),
                   Thread.currentThread().getContextClassLoader() );

        // Set the dreaded Thread Context ClassLoader to the HttpApplication ClassLoader
        Thread.currentThread().setContextClassLoader( httpApp.classLoader() );

        LOG.debug( "Received a FullHttpRequest: {}", request );
        final boolean keepAlive = isKeepAlive( request );
        HttpVersion httpVersion = request.getProtocolVersion();

        FullHttpResponse response;
        try
        {
            final Route route = httpApp.routes().route( request );
            LOG.debug( "Will route request to: {}", route );

            // Parse path parameters
            List<Object> pathParams = pathParameters( request, route );


            // Assemble and activate a Qi4j Application
//            Object qi4jRuntime = httpApp.classLoader().loadClass( "org.qi4j.runtime.Qi4jRuntimeImpl" ).newInstance();
//            Application qi4jApp = new Energy4Java( (Qi4jRuntime) qi4jRuntime ).newApplication( new ApplicationAssembler()
//            Application qi4jApp = new Energy4Java().newApplication( new ApplicationAssembler()
//            {
//                @Override
//                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
//                    throws AssemblyException
//                {
//                    ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
//                    assembly.layer( "main" ).module( "main" ).transients( route.controllerType() );
//                    return assembly;
//                }
//            } );
//            qi4jApp.activate();
//            Module module = qi4jApp.findModule( "main", "main" );

            // Lookup Controller Service
            // Object controller = module.newTransient( route.controllerType() );
            // Object controller = module.findService( route.controllerType() ).get();

            Object controller = httpApp.classLoader().loadClass( route.controllerType().getName() ).newInstance();

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
            if( result instanceof ChunkedResult )
            {
                ChunkedResult chunkedResult = (ChunkedResult) result;
                // TODO
            }
            else if( result instanceof StreamResult )
            {
                StreamResult streamResult = (StreamResult) result;
                response.data().writeBytes( streamResult.entityInput(), streamResult.contentLength() );
            }
            else if( result instanceof SimpleResult )
            {
                SimpleResult simpleResult = (SimpleResult) result;
                response.data().writeBytes( simpleResult.entity() );
            }

            // response.data().writeBytes( copiedBuffer( "Route match: " + route.toString() + "\r\n", UTF_8 ) );
            // response.data().writeBytes( copiedBuffer( "Result: " + result + "\r\n", UTF_8 ) );

            // Passivate the Qi4j Application
            // qi4jApp.passivate();
        }
        catch( RouteNotFoundException ex )
        {
            response = new DefaultFullHttpResponse( HTTP_1_1, NOT_FOUND );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=UTF-8" );
            response.data().writeBytes( copiedBuffer( "404 Route Not Found\nTried:\n" + httpApp.routes().toString() + "\n", UTF_8 ) );
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
        List<Object> pathParams = new ArrayList<>();
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
