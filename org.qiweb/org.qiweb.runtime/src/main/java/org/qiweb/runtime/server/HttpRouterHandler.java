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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.QiWebApplication;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteNotFoundException;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.controllers.Outcomes.ChunkedOutcome;
import org.qiweb.runtime.controllers.Outcomes.SimpleOutcome;
import org.qiweb.runtime.controllers.Outcomes.StreamOutcome;
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
import static org.qiweb.runtime.http.HttpFactories.bodyOf;
import static org.qiweb.runtime.http.HttpFactories.contextOf;
import static org.qiweb.runtime.http.HttpFactories.requestHeaderOf;
import static org.qiweb.runtime.http.HttpFactories.requestOf;

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
 * <p>On FullHttpRequests:</p>
 * <ul>
 *     <li>Generate a new request identity</li>
 *     <li>If the request is a WebSocket UPGRADE, process and stop here</li>
 *     <li>Build the QiWeb RequestHeader</li>
 *     <li>Route the request, return a 404 if no route were found</li>
 *     <li>Parse route path parameters and validate them, return a 4xx if something is wrong (should be 404)</li>
 *     <li>Lookup the controller instance that will process the request, return a 4xx if somethings goes wrong</li>
 *     <li>Consume and parse the whole RequestBody</li>
 *     <li>MultipartFormData, ie. file upload should overflow to disk</li>
 *     <li>Invoke the controller method</li>
 * </ul>
 * 
 * <h4>WebSocket UPGRADE Requests</h4>
 * 
 * <p>TODO</p>
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
public class HttpRouterHandler
    extends ChannelInboundMessageHandlerAdapter<FullHttpRequest>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpRouterHandler.class );
    private static final String REQUEST_IDENTITY_PREFIX = UUID.randomUUID().toString() + "-";
    private static final AtomicLong REQUEST_IDENTITY_COUNT = new AtomicLong();

    private static String generateNewRequestIdentity()
    {
        return REQUEST_IDENTITY_PREFIX + REQUEST_IDENTITY_COUNT.getAndIncrement();
    }
    private final QiWebApplication httpApp;
    private String requestIdentity;

    public HttpRouterHandler( QiWebApplication httpApp )
    {
        super();
        this.httpApp = httpApp;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext nettyContext, Throwable cause )
        throws Exception
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
        throws Exception
    {
        // Generate a unique identifier per request
        requestIdentity = generateNewRequestIdentity();
        return super.beginMessageReceived( nettyContext );
    }

    @Override
    public void messageReceived( ChannelHandlerContext nettyContext, FullHttpRequest nettyRequest )
        throws Exception
    {
        LOG.debug( "{} Received a FullHttpRequest: {}", requestIdentity, nettyRequest );

        // Parse RequestHeader
        RequestHeader requestHeader = requestHeaderOf( requestIdentity, nettyRequest );

        // Route the request
        Routes routes = httpApp.routes();
        try
        {
            final Route route = routes.route( requestHeader );
            LOG.debug( "{} Will route request to: {}", requestIdentity, route );

            // Eventually UPGRADE to WebSocket
            // TODO

            // Parse route path parameters - TODO Finish this, return a 404 if failed
            List<Object> pathParams = pathParameters( requestHeader, route );

            // Parse Request
            RequestBody body = bodyOf( requestHeader, nettyRequest );
            Request request = requestOf( requestHeader, body );
            Context context = contextOf( request, null, null );

            // Set Controller Context
            setCurrentThreadContext( httpApp.classLoader(), context );

            // Lookup Controller
            Object controller = httpApp.classLoader().loadClass( route.controllerType().getName() ).newInstance();

            // Invoke Controller
            LOG.debug( "{} Will invoke controller method: {}", requestIdentity, route.controllerMethod() );
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
            if( isKeepAlive( nettyRequest ) && nettyRequest.getProtocolVersion() == HTTP_1_1 )
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

            // Write response
            ChannelFuture writeFuture = nettyContext.write( response );

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
            // Clear Controller Context
            clearCurrentThreadContext();
        }
    }
    private ClassLoader originalLoader = null;

    private void setCurrentThreadContext( ClassLoader loader, Context context )
    {
        originalLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( loader );
        getControllerContextThreadLocal().set( context );
    }

    private void clearCurrentThreadContext()
    {
        Thread.currentThread().setContextClassLoader( originalLoader );
        originalLoader = null;
        getControllerContextThreadLocal().remove();
    }

    @SuppressWarnings( "unchecked" )
    private ThreadLocal<Context> getControllerContextThreadLocal()
    {
        try
        {
            Field field = Controller.class.getDeclaredField( "CONTEXT_THREAD_LOCAL" );
            if( !field.isAccessible() )
            {
                field.setAccessible( true );
            }
            return (ThreadLocal<Context>) field.get( null );
        }
        catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
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
