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
package io.werval.server.netty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import io.werval.api.events.HttpEvent;
import io.werval.spi.events.EventsSPI;
import io.werval.spi.server.HttpServerHelper;
import io.werval.runtime.exceptions.WervalRuntimeException;
import io.werval.util.IdentityGenerator;
import io.werval.util.UUIDIdentityGenerator;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Locale.US;

import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Names.CONTENT_LENGTH;
import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.util.Charsets.US_ASCII;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.removeTransferEncodingChunked;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Aggregate chunked HttpRequest in FullHttpRequest.
 *
 * <p>
 * HTTP decoders always generates multiple message objects per a single HTTP message:</p>
 * <pre>
 *  1       * HttpRequest / HttpResponse
 *  0 - n   * HttpContent
 *  1       * LastHttpContent
 * </pre>
 * <p>
 * This handler aggregate all messages pertaining to a request as a FullHttpRequest. The body chunks, if any, are
 * written to a file thus preventing OOMEs.
 * The file is deleted when the channel is closed.
 * </p>
 */
public class HttpRequestAggregator
    extends MessageToMessageDecoder<HttpObject>
{
    private static final Logger LOG = LoggerFactory.getLogger( HttpRequestAggregator.class );
    private static final ByteBuf HTTP_100_CONTINUE = copiedBuffer( "HTTP/1.1 100 Continue\r\n\r\n", US_ASCII );
    private static final IdentityGenerator TEMP_FILE_ID_GEN = new UUIDIdentityGenerator( "body" );

    private final HttpServerHelper helper;
    private final EventsSPI eventsSpi;
    private final long maxContentLength;
    private final long diskThreshold;
    private final File diskOverflowDirectory;
    private HttpRequest aggregatedRequestHeader;
    private int consumedContentlength = 0;
    private ByteBuf bodyBuf;
    private File bodyFile;

    public HttpRequestAggregator(
        HttpServerHelper helper, EventsSPI eventsSpi,
        long maxContentLength, long diskThreshold, File diskOverflowDirectory
    )
    {
        this.helper = helper;
        this.eventsSpi = eventsSpi;
        this.maxContentLength = maxContentLength;
        this.diskThreshold = diskThreshold;
        this.diskOverflowDirectory = diskOverflowDirectory;
    }

    @Override
    protected void decode( ChannelHandlerContext context, HttpObject msg, List<Object> out )
        throws Exception
    {
        // Handle this HttpObject or not?
        boolean skip = true;
        if( msg instanceof HttpRequest )
        {
            skip = false;
        }
        else if( msg instanceof HttpContent && aggregatedRequestHeader != null )
        {
            skip = false;
        }
        if( skip )
        {
            // Nothing to do with this message
            return;
        }

        if( msg instanceof HttpRequest )
        {
            handleHttpRequest( context, (HttpRequest) msg, out );
        }
        else if( msg instanceof HttpContent )
        {
            handleHttpContent( context, (HttpContent) msg, out );
        }
        else
        {
            throw new WervalRuntimeException( "Unexpected message type in Netty pipeline, something is broken: " + msg );
        }
    }

    private void handleHttpRequest( ChannelHandlerContext context, HttpRequest newRequestHeader, List<Object> out )
        throws IOException
    {
        // Belt and braces
        // Needed as the channel is reused when Keep-Alive play its role
        cleanup();

        // Let's go
        HttpRequest currentRequestHeader = aggregatedRequestHeader;
        assert currentRequestHeader == null;
        assert consumedContentlength == 0;
        // assert bodyFile == null;
        // assert bodyBuf == null;

        if( is100ContinueExpected( newRequestHeader ) )
        {
            context.write( HTTP_100_CONTINUE );
        }

        if( !newRequestHeader.getDecoderResult().isSuccess() )
        {
            removeTransferEncodingChunked( newRequestHeader );
            aggregatedRequestHeader = null;
            out.add( newRequestHeader );
            return;
        }

        // Generate new request identity
        String requestIdentity = helper.generateNewRequestIdentity();

        // Http Request Received Event
        context.channel().attr( Attrs.REQUEST_IDENTITY ).set( requestIdentity );
        eventsSpi.emit(
            new HttpEvent.RequestReceived(
                requestIdentity,
                newRequestHeader.getMethod().name(),
                newRequestHeader.getUri()
            )
        );

        currentRequestHeader = new DefaultHttpRequest( newRequestHeader.getProtocolVersion(),
                                                       newRequestHeader.getMethod(),
                                                       newRequestHeader.getUri() );
        currentRequestHeader.headers().set( newRequestHeader.headers() );

        removeTransferEncodingChunked( currentRequestHeader );

        aggregatedRequestHeader = currentRequestHeader;
    }

    private void handleHttpContent( ChannelHandlerContext context, HttpContent chunk, List<Object> out )
        throws IOException
    {
        HttpRequest currentRequestHeader = aggregatedRequestHeader;
        assert currentRequestHeader != null;

        int readableBytes = chunk.content().readableBytes();
        if( maxContentLength != -1 && consumedContentlength + readableBytes > maxContentLength )
        {
            LOG.warn( "Request Entity is too large, content length exceeded {} bytes.", maxContentLength );
            ByteBuf body = copiedBuffer( "HTTP content length exceeded " + maxContentLength + " bytes.", US_ASCII );
            FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, REQUEST_ENTITY_TOO_LARGE, body );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=" + US_ASCII.name().toLowerCase( US ) );
            response.headers().set( CONTENT_LENGTH, response.content().readableBytes() );
            response.headers().set( CONNECTION, CLOSE );
            context.write( response ).addListener( ChannelFutureListener.CLOSE ); // HERE
            return;
        }

        // Append chunk data to aggregated buffer or file
        if( chunk.content().isReadable() )
        {
            // Test disk threshold
            if( consumedContentlength + readableBytes > diskThreshold )
            {
                // Overflow to disk
                if( bodyFile == null )
                {
                    // Start
                    bodyFile = new File( diskOverflowDirectory, TEMP_FILE_ID_GEN.newIdentity() );
                    try( OutputStream bodyOutputStream = new FileOutputStream( bodyFile ) )
                    {
                        if( bodyBuf != null )
                        {
                            bodyBuf.readBytes( bodyOutputStream, bodyBuf.readableBytes() );
                            bodyBuf.release();
                            bodyBuf = null;
                        }
                        chunk.content().readBytes( bodyOutputStream, readableBytes );
                    }
                }
                else
                {
                    // Continue
                    try( OutputStream bodyOutputStream = new FileOutputStream( bodyFile, true ) )
                    {
                        chunk.content().readBytes( bodyOutputStream, readableBytes );
                    }
                }
            }
            else
            {
                // In-memory
                if( bodyBuf == null )
                {
                    // Start
                    bodyBuf = chunk.content().retain();
                }
                else
                {
                    // Continue
                    bodyBuf = wrappedBuffer( bodyBuf, chunk.content().retain() );
                }
            }
            consumedContentlength += readableBytes;
        }

        // Last Chunk?
        final boolean last;
        if( !chunk.getDecoderResult().isSuccess() )
        {
            currentRequestHeader.setDecoderResult( DecoderResult.failure( chunk.getDecoderResult().cause() ) );
            last = true;
        }
        else
        {
            last = chunk instanceof LastHttpContent;
        }

        if( last )
        {
            // Merge trailing headers
            if( chunk instanceof LastHttpContent )
            {
                currentRequestHeader.headers().add( ( (LastHttpContent) chunk ).trailingHeaders() );
            }

            // Set the 'Content-Length' header
            currentRequestHeader.headers().set( CONTENT_LENGTH, String.valueOf( consumedContentlength ) );

            // Create aggregated request
            FullHttpRequest fullRequest;
            ByteBuf content = null;
            if( bodyFile != null )
            {
                content = new FileByteBuff( bodyFile );
            }
            else if( bodyBuf != null )
            {
                content = bodyBuf.retain();
            }
            if( content != null )
            {
                fullRequest = new DefaultFullHttpRequest( currentRequestHeader.getProtocolVersion(),
                                                          currentRequestHeader.getMethod(),
                                                          currentRequestHeader.getUri(),
                                                          content );
            }
            else
            {
                fullRequest = new DefaultFullHttpRequest( currentRequestHeader.getProtocolVersion(),
                                                          currentRequestHeader.getMethod(),
                                                          currentRequestHeader.getUri() );
            }
            fullRequest.headers().set( currentRequestHeader.headers() );

            // All done
            aggregatedRequestHeader = null;
            consumedContentlength = 0;

            // Fire aggregated request
            out.add( fullRequest );
        }
    }

    @Override
    public void channelActive( ChannelHandlerContext ctx )
        throws Exception
    {
        cleanup();
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx )
        throws Exception
    {
        cleanup();
    }

    private void cleanup()
        throws IOException
    {
        if( bodyBuf != null )
        {
            bodyBuf.release();
            bodyBuf = null;
        }
        if( bodyFile != null )
        {
            Files.deleteIfExists( bodyFile.toPath() );
            bodyFile = null;
        }
    }
}
