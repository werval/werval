package org.qiweb.runtime.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;

/**
 * Encode a ChunkedInput&lt;ByteBuf&gt; into HTTP chunks.
 */
public class HttpChunkedBodyEncoder
    implements ChunkedInput<HttpContent>
{

    public static final String CONTENT_LENGTH_TRAILER = "X-QiWeb-Content-Length";
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
            lastChunk.trailingHeaders().add( CONTENT_LENGTH_TRAILER, contentLength );
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
