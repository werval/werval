package org.qiweb.runtime.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.QiWebApplication;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.runtime.controllers.Outcomes.ChunkedOutcome;
import org.qiweb.runtime.controllers.Outcomes.SimpleOutcome;
import org.qiweb.runtime.controllers.Outcomes.StreamOutcome;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteNotFoundException;
import org.qiweb.runtime.http.HttpFactories;
import org.qiweb.runtime.util.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
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
    private final QiWebApplication httpApp;

    public HttpRouterHandler( QiWebApplication httpApp )
    {
        super();
        this.httpApp = httpApp;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext context, Throwable cause )
        throws Exception
    {
        LOG.warn( "Exception caught: {}( {} )", cause.getClass().getSimpleName(), cause.getMessage(), cause );
        StringWriter sw = new StringWriter();
        sw.append( "500 Internal Server Error\n" ).append( cause.getMessage() ).append( "\n" );
        cause.printStackTrace( new PrintWriter( sw ) );
        sw.append( "\n\nCurrent Thread Context ClassLoader state:\n\n" );
        ClassLoaders.printLoadedClasses( Thread.currentThread().getContextClassLoader(), new PrintWriter( sw ) );
        sw.append( "\n" );
        sendError( context, INTERNAL_SERVER_ERROR, sw.toString() );
    }

    @Override
    public void messageReceived( ChannelHandlerContext context, FullHttpRequest request )
        throws Exception
    {


        LOG.debug( "Received a FullHttpRequest: {}", request );

        // Generate a unique identifier per request
        String requestIdentity = UUID.randomUUID().toString();

        MutableHeaders headers = HttpFactories.headersOf( request );
        RequestHeader requestHeader = HttpFactories.requestHeaderOf( requestIdentity, headers, request );

        // Set the dreaded Thread Context ClassLoader to the HttpApplication ClassLoader
        Thread.currentThread().setContextClassLoader( httpApp.classLoader() );

        try
        {
            final Route route = httpApp.routes().route( requestHeader );
            LOG.debug( "Will route request to: {}", route );

            // Parse route path parameters
            List<Object> pathParams = pathParameters( requestHeader, route );


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

            // Lookup Controller
            // Object controller = module.newTransient( route.controllerType() );
            // Object controller = module.findService( route.controllerType() ).get();

            Object controller = httpApp.classLoader().loadClass( route.controllerType().getName() ).newInstance();

            // Invoke Controller
            LOG.debug( "Will invoke controller method: {}", route.controllerMethod() );
            Outcome outcome = (Outcome) route.controllerMethod().invoke( controller, pathParams.toArray() );

            // == Build the response

            // Status
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( outcome.status() );
            FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );

            // Headers
            for( String headerName : outcome.headers().names() )
            {
                response.headers().add( headerName, outcome.headers().valueOf( headerName ) );
            }
            if( isKeepAlive( request ) && request.getProtocolVersion() == HTTP_1_1 )
            {
                response.headers().set( CONNECTION, KEEP_ALIVE );
            }

            // Body
            if( outcome instanceof ChunkedOutcome )
            {
                ChunkedOutcome chunkedOutcome = (ChunkedOutcome) outcome;
                // TODO
            }
            else if( outcome instanceof StreamOutcome )
            {
                StreamOutcome streamOutcome = (StreamOutcome) outcome;
                response.data().writeBytes( streamOutcome.entityInput(), streamOutcome.contentLength() );
            }
            else if( outcome instanceof SimpleOutcome )
            {
                SimpleOutcome simpleOutcome = (SimpleOutcome) outcome;
                response.headers().set( CONTENT_LENGTH, simpleOutcome.entity().readableBytes() );
                response.data().writeBytes( simpleOutcome.entity() );
            }

            // response.data().writeBytes( copiedBuffer( "Route match: " + route.toString() + "\r\n", UTF_8 ) );
            // response.data().writeBytes( copiedBuffer( "Outcome: " + outcome + "\r\n", UTF_8 ) );

            // Passivate the Qi4j Application
            // qi4jApp.passivate();

            // Write response
            ChannelFuture writeFuture = context.write( response );

            // Close the connection as soon as the response is sent if not keep alive
            if( true || !isKeepAlive( request ) ) // FIXME We don't KEEP ALIVE
            {
                writeFuture.addListener( ChannelFutureListener.CLOSE );
            }
        }
        catch( RouteNotFoundException ex )
        {
            LOG.trace( ex.getMessage() + " will return a 404 Not Found error" );
            sendError( context, NOT_FOUND, "404 Route Not Found\nTried:\n" + httpApp.routes().toString() + "\n\n" );
        }
    }

    private static void sendError( ChannelHandlerContext context, HttpResponseStatus status, String body )
    {
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, status, copiedBuffer( body, UTF_8 ) );
        response.headers().set( CONTENT_TYPE, "text/plain; charset=utf-8" );
        response.headers().set( CONTENT_LENGTH, response.data().readableBytes() );
        response.headers().set( CONNECTION, CLOSE );
        context.write( response ).addListener( ChannelFutureListener.CLOSE );
    }

    private static List<Object> pathParameters( RequestHeader requestHeader, Route route )
    {
        List<Object> pathParams = new ArrayList<>();
        for( Entry<String, Class<?>> controllerParam : route.controllerParams().entrySet() )
        {
            String paramName = controllerParam.getKey();
            Class<?> paramType = controllerParam.getValue();
            String paramStringValue = route.controllerParamPathValue( paramName, requestHeader.path() );
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
