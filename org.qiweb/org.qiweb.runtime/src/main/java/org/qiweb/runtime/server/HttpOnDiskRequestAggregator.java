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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.qiweb.api.Application;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.runtime.util.FileByteBuff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.removeTransferEncodingChunked;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.qiweb.api.http.Headers.Names.CONNECTION;
import static org.qiweb.api.http.Headers.Names.CONTENT_LENGTH;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Values.CLOSE;
import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Aggregate chunked HttpRequest in FullHttpRequest.
 * 
 * <p>HTTP decoders always generates multiple message objects per a single HTTP message:</p>
 * <pre>
 *  1       * HttpRequest / HttpResponse
 *  0 - n   * HttpContent
 *  1       * LastHttpContent
 * </pre>
 * <p>
 *     This handler aggregate all messages pertaining to a request as a FullHttpRequest. The body chunks are written
 *     to a file thus preventing OOME. The file is deleted when the channel is closed.
 * </p>
 */
// TODO Add in-memory buffering and overflow to disk according to a threshold
// TODO Remove temporary files !
public class HttpOnDiskRequestAggregator
    extends MessageToMessageDecoder<HttpObject>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpOnDiskRequestAggregator.class );
    private static final ByteBuf CONTINUE = copiedBuffer( "HTTP/1.1 100 Continue\r\n\r\n", UTF_8 );
    private final Application app;
    private final int maxContentLength;
    private HttpRequest aggregatedRequestHeader;
    private File bodyFile;
    private OutputStream bodyOutputStream;

    public HttpOnDiskRequestAggregator( Application app, int maxContentLength )
    {
        this.app = app;
        this.maxContentLength = maxContentLength;
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
            throw new QiWebRuntimeException( "Unexpected message type in Netty pipeline, something is broken: " + msg );
        }
    }

    private void handleHttpRequest( ChannelHandlerContext context, HttpRequest newRequestHeader, List<Object> out )
        throws IOException
    {
        HttpRequest currentRequestHeader = aggregatedRequestHeader;
        assert currentRequestHeader == null;

        if( is100ContinueExpected( newRequestHeader ) )
        {
            context.write( CONTINUE.duplicate() );
        }

        if( !newRequestHeader.getDecoderResult().isSuccess() )
        {
            removeTransferEncodingChunked( newRequestHeader );
            aggregatedRequestHeader = null;
            out.add( newRequestHeader );
            return;
        }
        currentRequestHeader = new DefaultHttpRequest( newRequestHeader.getProtocolVersion(),
                                                       newRequestHeader.getMethod(),
                                                       newRequestHeader.getUri() );
        currentRequestHeader.headers().set( newRequestHeader.headers() );
        removeTransferEncodingChunked( currentRequestHeader );

        aggregatedRequestHeader = currentRequestHeader;
        // TODO Generate request identity sooner so temporary files use the id in their name to ease debugging
        bodyFile = new File( app.tmpdir(), "body_" + UUID.randomUUID().toString() );
        bodyOutputStream = new FileOutputStream( bodyFile, true );

        LOG.trace( "Aggregating request ({} {}) to {}", aggregatedRequestHeader.getMethod(), aggregatedRequestHeader.getUri(), bodyFile );
    }

    private void handleHttpContent( ChannelHandlerContext context, HttpContent chunk, List<Object> out )
        throws IOException
    {
        HttpRequest currentRequestHeader = aggregatedRequestHeader;
        assert currentRequestHeader != null;

        if( maxContentLength != -1 && bodyFile.length() > maxContentLength - chunk.content().readableBytes() )
        {
            LOG.warn( "Request Entity is too large, content length exceeded {} bytes.", maxContentLength );
            ByteBuf body = copiedBuffer( "HTTP content length exceeded " + maxContentLength + " bytes.", UTF_8 );
            FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, REQUEST_ENTITY_TOO_LARGE, body );
            response.headers().set( CONTENT_TYPE, "text/plain; charset=utf-8" );
            response.headers().set( CONTENT_LENGTH, response.content().readableBytes() );
            response.headers().set( CONNECTION, CLOSE );
            context.write( response ).addListener( ChannelFutureListener.CLOSE );
            return;
        }

        // Append chunk data to aggregated File
        if( chunk.content().isReadable() )
        {
            chunk.content().readBytes( bodyOutputStream, chunk.content().readableBytes() );
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
            bodyOutputStream.flush();
            bodyOutputStream.close();

            // Merge trailing headers into the message.
            if( chunk instanceof LastHttpContent )
            {
                currentRequestHeader.headers().add( ( (LastHttpContent) chunk ).trailingHeaders() );
            }

            // Set the 'Content-Length' header.
            currentRequestHeader.headers().set( CONTENT_LENGTH, String.valueOf( bodyFile.length() ) );

            FullHttpRequest fullRequest = new DefaultFullHttpRequest( currentRequestHeader.getProtocolVersion(),
                                                                      currentRequestHeader.getMethod(),
                                                                      currentRequestHeader.getUri(),
                                                                      new FileByteBuff( bodyFile ) );
            fullRequest.headers().set( currentRequestHeader.headers() );

            // All done
            aggregatedRequestHeader = null;
            bodyOutputStream = null;

            out.add( fullRequest );
        }
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx )
        throws IOException
    {
        if( bodyFile != null )
        {
            Files.deleteIfExists( bodyFile.toPath() );
        }
    }
}
