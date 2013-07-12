package org.qiweb.runtime.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedMessageInput;

import static io.netty.buffer.Unpooled.*;
import io.netty.channel.MessageList;

/**
 * Encode a ChunkedInput&lt;ByteBuf&gt; into HTTP chunks.
 * <p>
 *     Please note that it should use the very same chunk size as the provided ChunkedInput, otherwise behaviour is not
 *     guaranteed.
 * </p>
 */
public class HttpChunkedBodyEncoder
    implements ChunkedMessageInput<HttpContent>
{

    public static final String CONTENT_LENGTH_TRAILER = "X-QiWeb-Content-Length";
    private final ChunkedInput<ByteBuf> chunkedBody;
    private final int chunkSize;
    private boolean isLastChunk = false;
    private boolean isLastChunkSent = false;
    private long contentLength = 0;

    public HttpChunkedBodyEncoder( ChunkedInput<ByteBuf> chunkedBody, int chunkSize )
    {
        this.chunkedBody = chunkedBody;
        this.chunkSize = chunkSize;
    }

    @Override
    public boolean isEndOfInput()
    {
        return isLastChunkSent;
    }

    @Override
    public void close()
        throws Exception
    {
        chunkedBody.close();
    }

    @Override
    public boolean readChunk( MessageList<HttpContent> buffer )
        throws Exception
    {
        if( isLastChunkSent )
        {
            return false;
        }
        else
        {
            buffer.add( nextChunk() );
            return true;
        }
    }

    private HttpContent nextChunk()
        throws Exception
    {
        if( isLastChunk )
        {
            isLastChunkSent = true;
            LastHttpContent lastChunk = new DefaultLastHttpContent( EMPTY_BUFFER );
            lastChunk.trailingHeaders().add( CONTENT_LENGTH_TRAILER, contentLength );
            return lastChunk;
        }
        ByteBuf buffer = Unpooled.buffer( chunkSize );
        if( !chunkedBody.readChunk( buffer ) )
        {
            isLastChunk = true;
        }
        contentLength += buffer.readableBytes();
        return new DefaultHttpContent( buffer );
    }
}
