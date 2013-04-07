package org.qiweb.runtime.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.controllers.ContextInstance;
import org.qiweb.runtime.controllers.ContextHelper;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.ChunkedOutcome;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.SimpleOutcome;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.StreamOutcome;
import org.qiweb.runtime.http.ResponseInstance;
import org.qiweb.runtime.util.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.Values.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
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
        if( cause instanceof ReadTimeoutException )
        {
            LOG.debug( "Read timeout, connection has been closed." );
        }
        else if( cause instanceof WriteTimeoutException )
        {
            LOG.debug( "Write timeout, connection has been closed." );
        }
        else
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
        ContextHelper contextHelper = new ContextHelper();
        try
        {
            final Route route = routes.route( requestHeader );
            LOG.debug( "{} Will route request to: {}", requestIdentity, route );

            // Parse route path parameters - TODO Finish this, return a 404 if failed
            List<Object> pathParams = pathParameters( requestHeader, route );

            // TODO Eventually UPGRADE to WebSocket

            // Parse Request
            Request request = requestOf( requestHeader, nettyRequest );

            // Parse Session
            Session session = sessionOf( requestHeader );

            // Prepare Response
            Response response = new ResponseInstance();

            // Set Controller Context
            Context context = new ContextInstance( app, session, request, response );
            contextHelper.setOnCurrentThread( app.classLoader(), context );

            // Lookup Controller
            Object controller = app.classLoader().loadClass( route.controllerType().getName() ).newInstance();

            // Invoke Controller
            LOG.debug( "{} Will invoke controller method: {}", requestIdentity, route.controllerMethod() );
            Outcome outcome = (Outcome) route.controllerMethod().invoke( controller, pathParams.toArray() );

            // == Build the response

            // Status
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( outcome.status() );

            // Headers & Body output
            final HttpResponse nettyResponse;
            final ChannelFuture writeFuture;
            if( outcome instanceof ChunkedOutcome )
            {
                ChunkedOutcome chunkedOutcome = (ChunkedOutcome) outcome;
                nettyResponse = new DefaultHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                applyHeaders( nettyResponse, response, outcome, nettyRequest );
                nettyResponse.headers().set( TRANSFER_ENCODING, CHUNKED );
                // Body
                nettyContext.write( nettyResponse );
                writeFuture = nettyContext.write( chunkedOutcome.chunkedInput() );
            }
            else if( outcome instanceof StreamOutcome )
            {
                StreamOutcome streamOutcome = (StreamOutcome) outcome;
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                applyHeaders( nettyResponse, response, outcome, nettyRequest );
                nettyResponse.headers().set( CONTENT_LENGTH, streamOutcome.contentLength() );
                // Body
                ( (FullHttpResponse) nettyResponse ).data().
                    writeBytes( streamOutcome.bodyInputStream(),
                                new BigDecimal( streamOutcome.contentLength() ).intValueExact() );
                writeFuture = nettyContext.write( nettyResponse );
            }
            else if( outcome instanceof SimpleOutcome )
            {
                SimpleOutcome simpleOutcome = (SimpleOutcome) outcome;
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                applyHeaders( nettyResponse, response, outcome, nettyRequest );
                nettyResponse.headers().set( CONTENT_LENGTH, simpleOutcome.body().readableBytes() );
                // Body
                ( (FullHttpResponse) nettyResponse ).data().writeBytes( simpleOutcome.body() );
                writeFuture = nettyContext.write( nettyResponse );
            }
            else
            {
                LOG.warn( "Unhandled Outcome type '{}', no response body.", outcome.getClass() );
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                applyHeaders( nettyResponse, response, outcome, nettyRequest );
                writeFuture = nettyContext.write( nettyResponse );
            }

            // Close the connection as soon as the response is sent if not keep alive
            if( !isKeepAlive( nettyRequest ) )
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
            contextHelper.clearCurrentThread();
        }
    }

    private List<Object> pathParameters( RequestHeader requestHeader, Route route )
    {
        List<Object> pathParams = new ArrayList<>();
        for( Entry<String, Class<?>> controllerParam : route.controllerParams().entrySet() )
        {
            String paramName = controllerParam.getKey();
            Class<?> paramType = controllerParam.getValue();
            String paramStringValue = route.controllerParamPathValue( paramName, requestHeader.path() );
            pathParams.add( app.pathBinders().bind( paramType, paramName, paramStringValue ) );
        }
        return pathParams;
    }

    private void applyHeaders( HttpResponse nettyResponse, Response response, Outcome outcome, FullHttpRequest nettyRequest )
    {
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
