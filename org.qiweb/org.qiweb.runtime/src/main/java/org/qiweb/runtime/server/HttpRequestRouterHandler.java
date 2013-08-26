/**
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.runtime.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.Application.Mode;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.ParameterBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.controllers.ContextInstance;
import org.qiweb.runtime.controllers.ContextHelper;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.ChunkedOutcome;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.SimpleOutcome;
import org.qiweb.runtime.controllers.OutcomeBuilderInstance.StreamOutcome;
import org.qiweb.runtime.filters.FilterChainFactory;
import org.qiweb.runtime.http.ResponseInstance;
import org.qiweb.runtime.http.SessionInstance;
import org.qiweb.runtime.util.Stacktraces;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRAILER;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.Values.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_ONLYIFCHANGED;
import static org.qiweb.runtime.server.NettyHttpFactories.asNettyCookie;
import static org.qiweb.runtime.server.NettyHttpFactories.requestHeaderOf;
import static org.qiweb.runtime.server.NettyHttpFactories.requestOf;

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
 * <p>TODO WebSocket UPGRADE</p>
 */
public final class HttpRequestRouterHandler
    extends SimpleChannelInboundHandler<FullHttpRequest>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpRequestRouterHandler.class );
    private static final String REQUEST_IDENTITY_PREFIX = UUID.randomUUID().toString() + "-";
    private static final AtomicLong REQUEST_IDENTITY_COUNT = new AtomicLong();

    private static String generateNewRequestIdentity()
    {
        return REQUEST_IDENTITY_PREFIX + REQUEST_IDENTITY_COUNT.getAndIncrement();
    }

    private final class HttpRequestCompleteChannelFutureListener
        implements ChannelFutureListener
    {

        private final RequestHeader requestHeader;

        private HttpRequestCompleteChannelFutureListener( RequestHeader requestHeader )
        {
            this.requestHeader = requestHeader;
        }

        @Override
        public void operationComplete( ChannelFuture future )
            throws Exception
        {
            if( future.isSuccess() )
            {
                LOG.debug( "{} Request completed successfully", requestIdentity );
                app.global().onHttpRequestComplete( app, requestHeader );
            }
        }
    }
    private final ApplicationInstance app;
    private final DevShellSPI devSpi;
    private String requestIdentity;

    public HttpRequestRouterHandler( ApplicationInstance app, DevShellSPI devSpi )
    {
        super();
        this.app = app;
        this.devSpi = devSpi;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext nettyContext, FullHttpRequest nettyRequest )
        throws ClassNotFoundException, InstantiationException,
               IllegalAccessException, InvocationTargetException,
               IOException
    {
        rebuildIfNeeded();

        // Generate a unique identifier per request
        requestIdentity = generateNewRequestIdentity();
        LOG.debug( "{} Received a FullHttpRequest:\n{}", requestIdentity,
                   Strings.indentTwoSpaces( nettyRequest.toString(), 2 ) );

        // Parse RequestHeader
        RequestHeader requestHeader = requestHeaderOf( requestIdentity, nettyRequest );

        // Route the request
        Routes routes = app.routes();

        // Prepare Controller Context
        ContextHelper contextHelper = new ContextHelper();
        try
        {
            final Route route = routes.route( requestHeader );
            LOG.debug( "{} Routing request to: {}", requestIdentity, route );

            // Bind parameters
            Map<String, Object> parameters = route.bindParameters( app.parameterBinders(),
                                                                   requestHeader.path(),
                                                                   requestHeader.queryString() );

            // TODO Eventually UPGRADE to WebSocket

            // Parse Session Cookie
            Session session = new SessionInstance(
                app.config(), app.crypto(),
                requestHeader.cookies().get( app.config().string( APP_SESSION_COOKIE_NAME ) ) );

            // Parse Request
            Request request = requestOf( requestHeader, parameters, nettyRequest );

            // Prepare Response
            Response response = new ResponseInstance();

            // Set Controller Context
            Context context = new ContextInstance( app, session, route, request, response );
            contextHelper.setOnCurrentThread( app.classLoader(), context );

            // Invoke Controller FilterChain, ended by Controller Method Invokation
            LOG.trace( "{} Invoking controller method: {}", requestIdentity, route.controllerMethod() );
            Outcome outcome = new FilterChainFactory().buildFilterChain( app, app.global(), context ).next( context );

            // == Build the response

            // Status
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( outcome.status() );

            // Headers & Body output
            final HttpResponse nettyResponse;
            final ChannelFuture writeFuture;
            final boolean forceClose;
            if( outcome instanceof ChunkedOutcome )
            {
                ChunkedOutcome chunkedOutcome = (ChunkedOutcome) outcome;
                nettyResponse = new DefaultHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                forceClose = applyResponseHeader( nettyRequest, session, response, outcome, nettyResponse );
                nettyResponse.headers().set( TRANSFER_ENCODING, CHUNKED );
                nettyResponse.headers().set( TRAILER, HttpChunkedBodyEncoder.CONTENT_LENGTH_TRAILER );
                // Body
                nettyContext.write( nettyResponse );
                writeFuture = nettyContext.writeAndFlush( new HttpChunkedBodyEncoder( chunkedOutcome.chunkedInput() ) );
            }
            else if( outcome instanceof StreamOutcome )
            {
                StreamOutcome streamOutcome = (StreamOutcome) outcome;
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                forceClose = applyResponseHeader( nettyRequest, session, response, outcome, nettyResponse );
                nettyResponse.headers().set( CONTENT_LENGTH, streamOutcome.contentLength() );
                // Body
                ( (FullHttpResponse) nettyResponse ).content().
                    writeBytes( streamOutcome.bodyInputStream(),
                                new BigDecimal( streamOutcome.contentLength() ).intValueExact() );
                writeFuture = nettyContext.writeAndFlush( nettyResponse );
            }
            else if( outcome instanceof SimpleOutcome )
            {
                SimpleOutcome simpleOutcome = (SimpleOutcome) outcome;
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                // Headers
                forceClose = applyResponseHeader( nettyRequest, session, response, outcome, nettyResponse );
                nettyResponse.headers().set( CONTENT_LENGTH, simpleOutcome.body().readableBytes() );
                // Body
                ( (FullHttpResponse) nettyResponse ).content().writeBytes( simpleOutcome.body() );
                writeFuture = nettyContext.writeAndFlush( nettyResponse );
            }
            else
            {
                LOG.warn( "Unhandled Outcome type '{}', no response body.", outcome.getClass() );
                nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, responseStatus );
                forceClose = applyResponseHeader( nettyRequest, session, response, outcome, nettyResponse );
                writeFuture = nettyContext.writeAndFlush( nettyResponse );
            }

            // Listen to request completion
            writeFuture.addListener( new HttpRequestCompleteChannelFutureListener( requestHeader ) );

            // Close the connection as soon as the response is sent if not keep alive
            if( forceClose || !isKeepAlive( nettyRequest ) )
            {
                writeFuture.addListener( ChannelFutureListener.CLOSE );
            }
        }
        finally
        {
            contextHelper.clearCurrentThread();
        }
    }

    private void rebuildIfNeeded()
    {
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            devSpi.rebuild();
        }
    }

    /**
     * @return TRUE if the channel should be closed, otherwise return FALSE
     */
    private boolean applyResponseHeader( FullHttpRequest nettyRequest, Session session, Response response, Outcome outcome, HttpResponse nettyResponse )
    {
        applyHttpHeaders( response.headers(), nettyResponse );
        applyHttpHeaders( outcome.headers(), nettyResponse );
        boolean forceClose = applyKeepAliveHttpHeaders( nettyRequest, outcome, nettyResponse );
        applySession( session, nettyResponse );
        applyCookies( response.cookies(), nettyResponse );
        return forceClose;
    }

    private void applyHttpHeaders( Headers headers, HttpResponse nettyResponse )
    {
        for( String name : headers.names() )
        {
            nettyResponse.headers().add( name, headers.valueOf( name ) );
        }
    }

    /**
     * @return TRUE if the channel should be closed, otherwise return FALSE
     */
    private boolean applyKeepAliveHttpHeaders( FullHttpRequest nettyRequest, Outcome outcome, HttpResponse nettyResponse )
    {
        switch( outcome.statusClass() )
        {
            case CLIENT_ERROR:
            case SERVER_ERROR:
            case UNKNOWN:
                // Apply Keep-Alive response headers if needed
                if( isKeepAlive( nettyRequest ) && nettyRequest.getProtocolVersion() == HTTP_1_1 )
                {
                    nettyResponse.headers().set( CONNECTION, CLOSE );
                }
                // Always close on errors or unknown statuses
                return true;
            case INFORMATIONAL:
            case SUCCESS:
            case REDIRECTION:
            default:
                // Apply Keep-Alive response headers if needed
                if( isKeepAlive( nettyRequest ) && nettyRequest.getProtocolVersion() == HTTP_1_1 )
                {
                    nettyResponse.headers().set( CONNECTION, KEEP_ALIVE );
                }
                // Don't close on informational, success or redirection statuses
                return false;
        }
    }

    private void applySession( Session session, HttpResponse nettyResponse )
    {
        if( !app.config().bool( APP_SESSION_COOKIE_ONLYIFCHANGED ) || session.hasChanged() )
        {
            nettyResponse.headers().add( SET_COOKIE,
                                         ServerCookieEncoder.encode( asNettyCookie( session.signedCookie() ) ) );
        }
    }

    private void applyCookies( Cookies cookies, HttpResponse nettyResponse )
    {
        for( Cookie cookie : cookies )
        {
            nettyResponse.headers().add( SET_COOKIE,
                                         ServerCookieEncoder.encode( asNettyCookie( cookie ) ) );
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext nettyContext, Throwable cause )
    {
        if( cause instanceof ReadTimeoutException )
        {
            LOG.debug( "{} Read timeout, connection has been closed.", requestIdentity );
        }
        else if( cause instanceof WriteTimeoutException )
        {
            LOG.debug( "{} Write timeout, connection has been closed.", requestIdentity );
        }
        else if( cause instanceof RouteNotFoundException )
        {
            LOG.trace( "{} " + cause.getMessage() + " will return 404.", requestIdentity );
            StringWriter body = new StringWriter();
            body.append( "404 Route Not Found\n" );
            if( app.mode() == Mode.DEV )
            {
                body.append( "Tried:\n" ).append( app.routes().toString() ).append( "\n\n" );
            }
            sendError( nettyContext, NOT_FOUND, body.toString() );
        }
        else if( cause instanceof ParameterBinderException )
        {
            // QUID BadRequest instead?
            LOG.warn( "{} ParameterBinderException, will return 404.", requestIdentity, cause );
            sendError( nettyContext, NOT_FOUND, cause.getMessage() );
        }
        else
        {
            LOG.error( "{} Exception caught: {}( {} )",
                       requestIdentity, cause.getClass().getSimpleName(), cause.getMessage(),
                       cause );
            StringWriter body = new StringWriter();
            body.append( "<html><head><title>500 Internal Server Error</title></head><body><h1>500 Internal Server Error</h1>\n" );
            if( app.mode() == Mode.DEV )
            {
                // In development mode we want nice stack traces
                if( cause instanceof QiWebException && cause.getCause() instanceof InvocationTargetException )
                {
                    // WARN This depends on and work only with DefaultControllerInvocation!
                    cause = cause.getCause().getCause();
                }
                body.append( Stacktraces.toHtml( cause, new Stacktraces.FileURLGenerator()
                {
                    @Override
                    public String urlFor( String filename, int line )
                    {
                        return devSpi.sourceURL( filename, line );
                    }
                } ) );

                body.append( "</body></html>\n" );
            }
            sendError( nettyContext, INTERNAL_SERVER_ERROR, body.toString() );
        }
    }

    private static void sendError( ChannelHandlerContext context, HttpResponseStatus status, String body )
    {
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, status, copiedBuffer( body, UTF_8 ) );
        response.headers().set( CONTENT_TYPE, "text/html; charset=utf-8" );
        response.headers().set( CONTENT_LENGTH, response.content().readableBytes() );
        response.headers().set( CONNECTION, CLOSE );
        context.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }
}
