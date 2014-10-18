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
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.ResponseHeader;
import org.qiweb.api.http.Status;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.outcomes.ChunkedInputOutcome;
import org.qiweb.runtime.outcomes.InputStreamOutcome;
import org.qiweb.runtime.outcomes.SimpleOutcome;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellRebuildException;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.api.events.HttpEvent;
import org.qiweb.spi.events.EventsSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.qiweb.api.http.Headers.Names.CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Names.TRAILER;
import static org.qiweb.api.http.Headers.Names.TRANSFER_ENCODING;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Values.CHUNKED;
import static org.qiweb.server.netty.NettyHttpFactories.remoteAddressOf;
import static org.qiweb.server.netty.NettyHttpFactories.requestOf;
import static org.qiweb.util.Charsets.UTF_8;

/**
 * Handle HTTP Requests.
 *
 * Any HTTP request message is allowed to contain a message body, and thus must be parsed with that in mind.
 * This implementation consume the request body for any requests methods but it is only parsed for POST, PUT
 * and PATCH methods. Parsing is done only for URL-encoded forms and multipart form data. For other request body
 * types, it's the application responsibility to do the parsing.
 */
// TODO WebSocket UPGRADE
public final class QiWebHttpHandler
    extends SimpleChannelInboundHandler<FullHttpRequest>
{
    private static final Logger LOG = LoggerFactory.getLogger( QiWebHttpHandler.class );

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
    private String requestIdentity;
    private RequestHeader requestHeader;

    public QiWebHttpHandler( ApplicationSPI app, DevShellSPI devSpi )
    {
        super();
        this.app = app;
        this.devSpi = devSpi;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext nettyContext, FullHttpRequest nettyRequest )
        throws Exception
    {
        // Get the request unique identifier
        requestIdentity = nettyContext.channel().attr( Attrs.REQUEST_IDENTITY ).get();
        assert requestIdentity != null;
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "{} Received a FullHttpRequest:\n{}", requestIdentity, nettyRequest.toString() );
        }

        // Return 503 to incoming requests while shutting down
        if( nettyContext.executor().isShuttingDown() )
        {
            app.shuttingDownOutcome(
                ProtocolVersion.valueOf( nettyRequest.getProtocolVersion().text() ),
                requestIdentity
            ).thenAcceptAsync(
                shuttingDownOutcome ->
                {
                    writeOutcome( nettyContext, shuttingDownOutcome )
                    .addListeners(
                        new HttpRequestCompleteChannelFutureListener( requestHeader ),
                        f -> app.events().emit(
                            new HttpEvent.ResponseSent( requestIdentity, shuttingDownOutcome.responseHeader().status() )
                        )
                    );
                },
                app.executor()
            );
            return;
        }

        // In development mode, rebuild application source if needed
        if( devSpi != null && devSpi.isSourceChanged() )
        {
            devSpi.rebuild();
        }

        // Create Request Instance
        // Can throw exceptions
        Request request = requestOf(
            app.defaultCharset(),
            app.httpBuilders(),
            remoteAddressOf( nettyContext.channel() ),
            requestIdentity,
            nettyRequest
        );
        requestHeader = request;

        // Handle Request
        app.handleRequest( request ).thenAcceptAsync(
            outcome ->
            {
                // Write Outcome
                ChannelFuture writeFuture = writeOutcome( nettyContext, outcome );
                // Listen to request completion
                writeFuture.addListeners(
                    f -> app.events().emit(
                        new HttpEvent.ResponseSent( requestIdentity, outcome.responseHeader().status() )
                    ),
                    new HttpRequestCompleteChannelFutureListener( requestHeader )
                );
            },
            app.executor()
        );
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext nettyContext, Throwable cause )
        throws IOException
    {
        if( cause instanceof ReadTimeoutException )
        {
            LOG.trace( "{} Read timeout, connection has been closed.", requestIdentity );
        }
        else if( cause instanceof WriteTimeoutException )
        {
            LOG.trace( "{} Write timeout, connection has been closed.", requestIdentity );
        }
        else if( cause instanceof DevShellRebuildException )
        {
            byte[] htmlErrorPage = ( (DevShellRebuildException) cause ).htmlErrorPage().getBytes( UTF_8 );
            DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse( HTTP_1_1, INTERNAL_SERVER_ERROR );
            nettyResponse.headers().set( CONTENT_LENGTH, htmlErrorPage.length );
            ( (ByteBufHolder) nettyResponse ).content().writeBytes( htmlErrorPage );
            nettyContext.writeAndFlush( nettyResponse )
                .addListeners(
                    f -> app.events().emit(
                        new HttpEvent.ResponseSent( requestIdentity, Status.INTERNAL_SERVER_ERROR )
                    ),
                    new HttpRequestCompleteChannelFutureListener( requestHeader ),
                    ChannelFutureListener.CLOSE
                );
        }
        else if( requestHeader != null )
        {
            // Write Outcome
            Outcome errorOutcome = app.handleError( requestHeader, cause );
            ChannelFuture writeFuture = writeOutcome( nettyContext, errorOutcome );
            // Listen to request completion
            writeFuture.addListeners(
                f -> app.events().emit(
                    new HttpEvent.ResponseSent( requestIdentity, errorOutcome.responseHeader().status() )
                ),
                new HttpRequestCompleteChannelFutureListener( requestHeader )
            );
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
            try
            {
                ( (ByteBufHolder) nettyResponse ).content().writeBytes(
                    streamOutcome.bodyInputStream(),
                    new BigDecimal( streamOutcome.contentLength() ).intValueExact()
                );
            }
            catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
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
     * @param response      QiWeb ResponseHeader
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
    }
}
