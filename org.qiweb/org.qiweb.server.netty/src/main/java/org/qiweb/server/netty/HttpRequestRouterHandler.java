/*
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
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.ResponseHeader;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.http.RequestInstance;
import org.qiweb.runtime.outcomes.ChunkedInputOutcome;
import org.qiweb.runtime.outcomes.InputStreamOutcome;
import org.qiweb.runtime.outcomes.SimpleOutcome;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.server.HttpServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.http.Headers.Names.CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Names.SET_COOKIE;
import static org.qiweb.api.http.Headers.Names.TRAILER;
import static org.qiweb.api.http.Headers.Names.TRANSFER_ENCODING;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Values.CHUNKED;
import static org.qiweb.server.netty.NettyHttpFactories.asNettyCookie;
import static org.qiweb.server.netty.NettyHttpFactories.bodyOf;
import static org.qiweb.server.netty.NettyHttpFactories.remoteAddressOf;
import static org.qiweb.server.netty.NettyHttpFactories.requestHeaderOf;

/**
 * Handle plain HTTP and WebSocket UPGRADE requests.
 *
 * <strong>HTTP Requests</strong>
 * <p>
 * Any HTTP request message is allowed to contain a message body, and thus must be parsed with that in mind.
 * This implementation consume the request body for any requests methods but it is only parsed for POST, PUT
 * and PATCH methods. Parsing is done only for URL-encoded forms and multipart form data. For other request body
 * types, it's the application responsibility to do the parsing.
 * <p>
 * <strong>WebSocket UPGRADE Requests</strong>
 * <p>
 * TODO WebSocket UPGRADE
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
                app.onHttpRequestComplete( requestHeader );
            }
        }
    }

    private final ApplicationSPI app;
    private final DevShellSPI devSpi;
    private final HttpServerHelper helper = new HttpServerHelper();
    private String requestIdentity;
    private RequestHeader requestHeader;

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
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "{} Received a FullHttpRequest:\n{}", requestIdentity, nettyRequest.toString() );
        }

        // Return 503 to incoming requests while shutting down
        if( nettyContext.executor().isShuttingDown() )
        {
            writeOutcome(
                nettyContext,
                app.shuttingDownOutcome(
                    ProtocolVersion.valueOf( nettyRequest.getProtocolVersion().text() ),
                    requestIdentity
                )
            );
            return;
        }

        // In development mode, rebuild application source if needed
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            devSpi.rebuild();
        }

        // Parse RequestHeader
        // Can throw exceptions
        requestHeader = requestHeaderOf(
            app.httpBuilders(),
            requestIdentity,
            nettyRequest,
            remoteAddressOf( nettyContext.channel() ),
            app.defaultCharset()
        );

        // Parse RequestBody
        // Can throw exceptions
        RequestBody requestBody = bodyOf(
            app.httpBuilders(),
            requestHeader,
            nettyRequest,
            app.defaultCharset()
        );

        // Create Request Instance
        Request request = new RequestInstance( requestHeader, requestBody );

        // Handle Request
        Outcome outcome = app.handleRequest( request );

        // Write Outcome
        ChannelFuture writeFuture = writeOutcome( nettyContext, outcome );

        // Listen to request completion
        writeFuture.addListener( new HttpRequestCompleteChannelFutureListener( requestHeader ) );
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext nettyContext, Throwable cause )
        throws IOException
    {
        if( cause instanceof ReadTimeoutException )
        {
            LOG.debug( "{} Read timeout, connection has been closed.", requestIdentity );
        }
        else if( cause instanceof WriteTimeoutException )
        {
            LOG.debug( "{} Write timeout, connection has been closed.", requestIdentity );
        }
        else if( requestHeader != null )
        {
            Outcome errorOutcome = app.handleError( requestHeader, cause );
            writeOutcome( nettyContext, errorOutcome );
        }
        else
        {
            LOG.error(
                "HTTP Server encountered an unexpected error, please raise an issue with the complete stacktrace",
                cause
            );
            nettyContext.close();
        }
    }

    private ChannelFuture writeOutcome( ChannelHandlerContext nettyContext, Outcome outcome )
        throws IOException
    {
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
            LOG.warn( "{} Unhandled Outcome type '{}', no response body.", requestIdentity, outcome.getClass() );
            nettyResponse = new DefaultFullHttpResponse( responseVersion, responseStatus );
            applyResponseHeader( responseHeader, nettyResponse );
            writeFuture = nettyContext.writeAndFlush( nettyResponse );
        }

        if( LOG.isTraceEnabled() )
        {
            LOG.trace( "{} Sent a HttpResponse:\n{}", requestIdentity, nettyResponse.toString() );
        }

        // Close the connection as soon as the response is sent if not keep alive
        if( !outcome.responseHeader().isKeepAlive() || nettyContext.executor().isShuttingDown() )
        {
            writeFuture.addListener( ChannelFutureListener.CLOSE );
        }

        // Done!
        return writeFuture;
    }

    /**
     * Apply Headers and Cookies into Netty HttpResponse.
     *
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
}
