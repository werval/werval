package org.qiweb.runtime.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.Application;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteNotFoundException;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.controllers.ControllerContext;
import org.qiweb.runtime.controllers.Outcomes.ChunkedOutcome;
import org.qiweb.runtime.controllers.Outcomes.SimpleOutcome;
import org.qiweb.runtime.controllers.Outcomes.StreamOutcome;
import org.qiweb.runtime.http.ResponseInstance;
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
import static org.qiweb.runtime.http.HttpFactories.contextOf;
import static org.qiweb.runtime.http.HttpFactories.requestHeaderOf;
import static org.qiweb.runtime.http.HttpFactories.requestOf;
import static org.qiweb.runtime.http.HttpFactories.sessionOf;

/**
 * Handle plain HTTP and WebSocket UPGRADE requests.
 * 
 * <h4>HTTP Requests</h4>
 * <p>
 *     Any HTTP request message is allowed to contain a message body, and thus must be parsed with that in mind.
 *     This implementation consume the request body for any requests methods but it is only parsed for POST, PUT
 *     and PATCH methods. Parsing is done only for URL-encoded forms and multipart form data. For other request body
 *     types, it's the application responsibility to do the parsing.
 * </p>
 * 
 * <h4>WebSocket UPGRADE Requests</h4>
 * 
 * <p>TODO WebSocket</p>
 */
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
// /* ... */
// Passivate the Qi4j Application
// qi4jApp.passivate();
public final class HttpRouterHandler
    extends ChannelInboundMessageHandlerAdapter<FullHttpRequest>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpRouterHandler.class );
    private static final String REQUEST_IDENTITY_PREFIX = UUID.randomUUID().toString() + "-";
    private static final AtomicLong REQUEST_IDENTITY_COUNT = new AtomicLong();

    private static String generateNewRequestIdentity()
    {
        return REQUEST_IDENTITY_PREFIX + REQUEST_IDENTITY_COUNT.getAndIncrement();
    }
    private final Application app;
    private String requestIdentity;

    public HttpRouterHandler( Application app )
    {
        super();
        this.app = app;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext nettyContext, Throwable cause )
    {
        LOG.warn( "{} Exception caught: {}( {} )",
                  requestIdentity, cause.getClass().getSimpleName(), cause.getMessage(),
                  cause );
        StringWriter sw = new StringWriter();
        sw.append( "500 Internal Server Error\n" ).append( cause.getMessage() ).append( "\n" );
        cause.printStackTrace( new PrintWriter( sw ) );
        sw.append( "\n\nCurrent Thread Context ClassLoader state:\n\n" );
        ClassLoaders.printLoadedClasses( Thread.currentThread().getContextClassLoader(), new PrintWriter( sw ) );
        sw.append( "\n" );
        sendError( nettyContext, INTERNAL_SERVER_ERROR, sw.toString() );
    }

    @Override
    public boolean beginMessageReceived( ChannelHandlerContext nettyContext )
    {
        // Generate a unique identifier per request
        requestIdentity = generateNewRequestIdentity();
        return true;
    }

    @Override
    public void messageReceived( ChannelHandlerContext nettyContext, FullHttpRequest nettyRequest )
        throws ClassNotFoundException, InstantiationException,
               IllegalAccessException, InvocationTargetException,
               IOException
    {
        LOG.debug( "{} Received a FullHttpRequest: {}", requestIdentity, nettyRequest );

        // Parse RequestHeader
        RequestHeader requestHeader = requestHeaderOf( requestIdentity, nettyRequest );

        // Route the request
        Routes routes = app.routes();

        // Prepare Controller Context
        ControllerContext controllerContext = new ControllerContext();
        try
        {
            final Route route = routes.route( requestHeader );
            LOG.debug( "{} Will route request to: {}", requestIdentity, route );

            // Parse route path parameters - TODO Finish this, return a 404 if failed
            List<Object> pathParams = pathParameters( requestHeader, route );

            // Eventually UPGRADE to WebSocket
            // TODO WebSocket

            // Parse Request
            Request request = requestOf( requestHeader, nettyRequest );

            // Parse Session & Flash
            Session session = sessionOf( requestHeader );
            Flash flash = null; // TODO Parse Flash

            // Prepare Response
            Response response = new ResponseInstance();

            // Set Controller Context
            Context context = contextOf( app, session, request, response, flash );
            controllerContext.setOnCurrentThread( app.classLoader(), context );

            // Lookup Controller
            Object controller = app.classLoader().loadClass( route.controllerType().getName() ).newInstance();

            // Invoke Controller
            LOG.debug( "{} Will invoke controller method: {}", requestIdentity, route.controllerMethod() );
            Outcome outcome = (Outcome) route.controllerMethod().invoke( controller, pathParams.toArray() );

            // == Build the response

            // Status
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( outcome.status() );
            FullHttpResponse nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );

            // Headers
            for( String headerName : response.headers().names() )
            {
                nettyResponse.headers().add( headerName, response.headers().valueOf( headerName ) );
            }
            for( String headerName : outcome.headers().names() )
            {
                nettyResponse.headers().add( headerName, outcome.headers().valueOf( headerName ) );
            }
            if( isKeepAlive( nettyRequest ) && nettyRequest.getProtocolVersion() == HTTP_1_1 )
            {
                nettyResponse.headers().set( CONNECTION, KEEP_ALIVE );
            }

            // Body
            if( outcome instanceof ChunkedOutcome )
            {
                ChunkedOutcome chunkedOutcome = (ChunkedOutcome) outcome;
                // TODO ChunkedOutcome
            }
            else if( outcome instanceof StreamOutcome )
            {
                StreamOutcome streamOutcome = (StreamOutcome) outcome;
                nettyResponse.data().writeBytes( streamOutcome.bodyInputStream(), streamOutcome.contentLength() );
            }
            else if( outcome instanceof SimpleOutcome )
            {
                SimpleOutcome simpleOutcome = (SimpleOutcome) outcome;
                nettyResponse.headers().set( CONTENT_LENGTH, simpleOutcome.entity().readableBytes() );
                nettyResponse.data().writeBytes( simpleOutcome.entity() );
            }

            // Write response
            ChannelFuture writeFuture = nettyContext.write( nettyResponse );

            // Close the connection as soon as the response is sent if not keep alive
            if( true || !isKeepAlive( nettyRequest ) ) // FIXME We don't KEEP ALIVE
            {
                writeFuture.addListener( ChannelFutureListener.CLOSE );
            }
        }
        catch( RouteNotFoundException ex )
        {
            LOG.trace( "{} " + ex.getMessage() + " will return a 404 Not Found error", requestIdentity );
            sendError( nettyContext, NOT_FOUND, "404 Route Not Found\nTried:\n" + routes.toString() + "\n\n" );
        }
        finally
        {
            controllerContext.clearCurrentThread();
        }
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

    private static void sendError( ChannelHandlerContext context, HttpResponseStatus status, String body )
    {
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, status, copiedBuffer( body, UTF_8 ) );
        response.headers().set( CONTENT_TYPE, "text/plain; charset=utf-8" );
        response.headers().set( CONTENT_LENGTH, response.data().readableBytes() );
        response.headers().set( CONNECTION, CLOSE );
        context.write( response ).addListener( ChannelFutureListener.CLOSE );
    }
}
