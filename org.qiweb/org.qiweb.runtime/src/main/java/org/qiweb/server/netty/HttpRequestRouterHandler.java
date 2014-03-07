/**
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.server.netty;

import io.netty.buffer.ByteBufHolder;
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
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import org.qiweb.api.Error;
import org.qiweb.api.Mode;
import org.qiweb.api.exceptions.ParameterBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.ResponseHeader;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.http.RequestInstance;
import org.qiweb.runtime.outcomes.ChunkedInputOutcome;
import org.qiweb.runtime.outcomes.InputStreamOutcome;
import org.qiweb.runtime.outcomes.SimpleOutcome;
import org.qiweb.runtime.util.Stacktraces;
import org.qiweb.server.HttpServerHelper;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.Locale.US;
import static org.qiweb.api.http.Headers.Names.CONNECTION;
import static org.qiweb.api.http.Headers.Names.CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.RETRY_AFTER;
import static org.qiweb.api.http.Headers.Names.SET_COOKIE;
import static org.qiweb.api.http.Headers.Names.TRAILER;
import static org.qiweb.api.http.Headers.Names.TRANSFER_ENCODING;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_REQUEST_ID;
import static org.qiweb.api.http.Headers.Values.CHUNKED;
import static org.qiweb.api.http.Headers.Values.CLOSE;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_FORMS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_QUERYSTRING_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_UPLOADS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_RETRYAFTER;
import static org.qiweb.server.netty.NettyHttpFactories.asNettyCookie;
import static org.qiweb.server.netty.NettyHttpFactories.bodyOf;
import static org.qiweb.server.netty.NettyHttpFactories.remoteAddressOf;
import static org.qiweb.server.netty.NettyHttpFactories.requestHeaderOf;

/**
 * Handle plain HTTP and WebSocket UPGRADE requests.
 * 
 * <p><strong>HTTP Requests</strong></p>
 * <p>
 *     Any HTTP request message is allowed to contain a message body, and thus must be parsed with that in mind.
 *     This implementation consume the request body for any requests methods but it is only parsed for POST, PUT
 *     and PATCH methods. Parsing is done only for URL-encoded forms and multipart form data. For other request body
 *     types, it's the application responsibility to do the parsing.
 * </p>
 * 
 * <p><strong>WebSocket UPGRADE Requests</strong></p>
 * 
 * <p>TODO WebSocket UPGRADE</p>
 */
public final class HttpRequestRouterHandler
    extends SimpleChannelInboundHandler<FullHttpRequest>
{
    private static final Logger LOG = LoggerFactory.getLogger( HttpRequestRouterHandler.class );

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

    private final ApplicationSPI app;
    private final DevShellSPI devSpi;
    private final HttpServerHelper helper = new HttpServerHelper();
    private String requestIdentity;

    public HttpRequestRouterHandler( ApplicationSPI app, DevShellSPI devSpi )
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
        // Generate a unique identifier per request
        requestIdentity = helper.generateNewRequestIdentity();
        LOG.debug( "{} Received a FullHttpRequest:\n{}", requestIdentity, nettyRequest.toString() );

        // Return 503 to incoming requests while shutting down
        if( nettyContext.executor().isShuttingDown() )
        {
            FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, SERVICE_UNAVAILABLE,
                copiedBuffer( "<html><body><h1>Service is shutting down</h1></body></html>", app.defaultCharset() )
            );
            response.headers().set( X_QIWEB_REQUEST_ID, requestIdentity );
            response.headers().set( CONTENT_TYPE, "text/html; charset=" + app.defaultCharset().name().toLowerCase( US ) );
            response.headers().set( CONTENT_LENGTH, response.content().readableBytes() );
            response.headers().set( CONNECTION, CLOSE );
            // By default, no Retry-After, only if defined in configuration
            if( app.config().has( QIWEB_SHUTDOWN_RETRYAFTER ) )
            {
                response.headers().set(
                    RETRY_AFTER,
                    String.valueOf( app.config().seconds( QIWEB_SHUTDOWN_RETRYAFTER ) )
                );
            }
            nettyContext.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
            return;
        }

        // In development mode, rebuild application source if needed
        rebuildIfNeeded();

        // Parse RequestHeader
        RequestHeader requestHeader = requestHeaderOf(
            requestIdentity, nettyRequest,
            remoteAddressOf( nettyContext.channel() ),
            app.config().bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED ),
            app.config().bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK ),
            app.config().stringList( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED ),
            app.defaultCharset(),
            app.config().bool( QIWEB_HTTP_QUERYSTRING_MULTIVALUED ),
            app.config().bool( QIWEB_HTTP_HEADERS_MULTIVALUED )
        );

        // Parse Request
        RequestBody requestBody = bodyOf(
            requestHeader,
            nettyRequest,
            app.defaultCharset(),
            app.config().bool( QIWEB_HTTP_HEADERS_MULTIVALUED ),
            app.config().bool( QIWEB_HTTP_FORMS_MULTIVALUED ),
            app.config().bool( QIWEB_HTTP_UPLOADS_MULTIVALUED )
        );
        Request request = new RequestInstance( requestHeader, requestBody );

        // Handle Request
        Outcome outcome = app.handleRequest( request );

        // == Build the Netty Response
        ResponseHeader responseHeader = outcome.responseHeader();

        // Netty Version & Status
        HttpVersion responseVersion = HttpVersion.valueOf( responseHeader.version().toString() );
        HttpResponseStatus responseStatus = HttpResponseStatus.valueOf( responseHeader.status().code() );

        // Netty Headers & Body output
        final HttpResponse nettyResponse;
        final ChannelFuture writeFuture;
        if( outcome instanceof ChunkedInputOutcome )
        {
            ChunkedInputOutcome chunkedOutcome = (ChunkedInputOutcome) outcome;
            nettyResponse = new DefaultHttpResponse( responseVersion, responseStatus );
            // Headers
            applyResponseHeader( responseHeader, nettyResponse );
            nettyResponse.headers().set( TRANSFER_ENCODING, CHUNKED );
            nettyResponse.headers().set( TRAILER, X_QIWEB_CONTENT_LENGTH );
            // Body
            nettyContext.write( nettyResponse );
            writeFuture = nettyContext.writeAndFlush(
                new HttpChunkedBodyEncoder(
                    new ChunkedStream( chunkedOutcome.inputStream(), chunkedOutcome.chunkSize() )
                )
            );
        }
        else if( outcome instanceof InputStreamOutcome )
        {
            InputStreamOutcome streamOutcome = (InputStreamOutcome) outcome;
            nettyResponse = new DefaultFullHttpResponse( responseVersion, responseStatus );
            // Headers
            applyResponseHeader( responseHeader, nettyResponse );
            nettyResponse.headers().set( CONTENT_LENGTH, streamOutcome.contentLength() );
            // Body
            ( (ByteBufHolder) nettyResponse ).content().writeBytes(
                streamOutcome.bodyInputStream(),
                new BigDecimal( streamOutcome.contentLength() ).intValueExact()
            );
            writeFuture = nettyContext.writeAndFlush( nettyResponse );
        }
        else if( outcome instanceof SimpleOutcome )
        {
            SimpleOutcome simpleOutcome = (SimpleOutcome) outcome;
            byte[] body = simpleOutcome.body().asBytes();
            nettyResponse = new DefaultFullHttpResponse( responseVersion, responseStatus );
            // Headers
            applyResponseHeader( responseHeader, nettyResponse );
            nettyResponse.headers().set( CONTENT_LENGTH, body.length );
            // Body
            ( (ByteBufHolder) nettyResponse ).content().writeBytes( body );
            writeFuture = nettyContext.writeAndFlush( nettyResponse );
        }
        else
        {
            LOG.warn( "{} Unhandled Outcome type '{}', no response body.", request.identity(), outcome.getClass() );
            nettyResponse = new DefaultFullHttpResponse( responseVersion, responseStatus );
            applyResponseHeader( responseHeader, nettyResponse );
            writeFuture = nettyContext.writeAndFlush( nettyResponse );
        }

        LOG.trace( "{} Sent a HttpResponse:\n{}", request.identity(), nettyResponse.toString() );

        // Listen to request completion
        writeFuture.addListener( new HttpRequestCompleteChannelFutureListener( requestHeader ) );

        // Close the connection as soon as the response is sent if not keep alive
        if( !outcome.responseHeader().isKeepAlive() || nettyContext.executor().isShuttingDown() )
        {
            writeFuture.addListener( ChannelFutureListener.CLOSE );
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
     * Apply Headers and Cookies into Netty HttpResponse.
     * @param response QiWeb ResponseHeader
     * @param nettyResponse Netty HttpResponse
     */
    private void applyResponseHeader( ResponseHeader response, HttpResponse nettyResponse )
    {
        for( String name : response.headers().names() )
        {
            nettyResponse.headers().add(
                name,
                response.headers().values( name )
            );
        }
        for( Cookie cookie : response.cookies() )
        {
            nettyResponse.headers().add(
                SET_COOKIE,
                ServerCookieEncoder.encode( asNettyCookie( cookie ) )
            );
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
            body.append( "<html>\n<head><title>404 Route Not Found</title></head>\n<body>\n<h1>404 Route Not Found</h1>\n" );
            if( app.mode() == Mode.DEV )
            {
                body.append( "<p>Tried:</p>\n<pre>\n" );
                for( Route route : app.routes() )
                {
                    if( !route.path().startsWith( "/@" ) )
                    {
                        body.append( route.toString() ).append( "\n" );
                    }
                }
                body.append( "</pre>\n" );
            }
            body.append( "</body>\n</html>\n" );
            sendError( nettyContext, NOT_FOUND, body.toString() );
        }
        else if( cause instanceof ParameterBinderException )
        {
            LOG.warn( "{} ParameterBinderException, will return 400.", requestIdentity, cause );
            sendError( nettyContext, BAD_REQUEST, "400 BAD REQUEST " + cause.getMessage() );
        }
        else
        {
            // We want nice stack traces
            // TODO Make stack-trace reduction pluggable
            if( cause instanceof QiWebException && cause.getCause() instanceof InvocationTargetException )
            {
                // WARN This depends on and work only with DefaultControllerInvocation!
                cause = cause.getCause().getCause();
            }

            if( cause instanceof BadRequestException )
            {
                // Handle HTTP Exceptions
                LOG.warn( "{} BadRequestException, will return 400.", requestIdentity, cause );
                sendError( nettyContext, BAD_REQUEST, "400 BAD REQUEST " + cause.getMessage() );
            }
            else
            {
                // Handle Unexpected Exceptions
                LOG.error(
                    "{} Exception caught: {}( {} )",
                    requestIdentity, cause.getClass().getSimpleName(), cause.getMessage(),
                    cause
                );

                // Build body
                StringWriter body = new StringWriter();
                body.append( "<html><head><title>500 Internal Server Error</title></head><body><h1>500 Internal Server Error</h1>\n" );
                if( app.mode() == Mode.DEV )
                {
                    body.append( Stacktraces.toHtml( cause, devSpi::sourceURL ) );
                }
                body.append( "</body></html>\n" );
                String bodyString = body.toString();

                // Record Error
                Error error = app.errors().record( requestIdentity, bodyString, cause );

                // Notifies Application's Global
                app.global().onHttpRequestError( app, error );

                // Send Error to client
                sendError( nettyContext, INTERNAL_SERVER_ERROR, bodyString );
            }
        }
    }

    private void sendError( ChannelHandlerContext context, HttpResponseStatus status, String body )
    {
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, status, copiedBuffer( body, app.defaultCharset() ) );
        response.headers().set( X_QIWEB_REQUEST_ID, requestIdentity );
        response.headers().set( CONTENT_TYPE, "text/html; charset=" + app.defaultCharset().name().toLowerCase( US ) );
        response.headers().set( CONTENT_LENGTH, response.content().readableBytes() );
        response.headers().set( CONNECTION, CLOSE );
        context.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }
}
