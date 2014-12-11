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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.werval.api.http.Headers.Names.X_QIWEB_CONTENT_LENGTH;

/**
 * Encode a ChunkedInput&lt;ByteBuf&gt; into HTTP chunks.
 */
public class HttpChunkedBodyEncoder
    implements ChunkedInput<HttpContent>
{
    private final ChunkedInput<ByteBuf> chunkedBody;
    private boolean isLastChunk = false;
    private boolean isLastChunkRead = false;
    private long contentLength = 0;

    public HttpChunkedBodyEncoder( ChunkedInput<ByteBuf> chunkedBody )
    {
        this.chunkedBody = chunkedBody;
    }

    @Override
    public boolean isEndOfInput()
    {
        return isLastChunkRead;
    }

    @Override
    public void close()
        throws Exception
    {
        chunkedBody.close();
    }

    @Override
    public HttpContent readChunk( ChannelHandlerContext context )
        throws Exception
    {
        if( isLastChunkRead )
        {
            return null;
        }
        else
        {
            return nextChunk( context );
        }
    }

    private HttpContent nextChunk( ChannelHandlerContext context )
        throws Exception
    {
        if( isLastChunk )
        {
            isLastChunkRead = true;
            LastHttpContent lastChunk = new DefaultLastHttpContent( EMPTY_BUFFER );
            lastChunk.trailingHeaders().add( X_QIWEB_CONTENT_LENGTH, contentLength );
            return lastChunk;
        }
        ByteBuf buffer = chunkedBody.readChunk( context );
        if( chunkedBody.isEndOfInput() )
        {
            isLastChunk = true;
        }
        contentLength += buffer.readableBytes();
        return new DefaultHttpContent( buffer );
    }
}
