package org.qiweb.runtime.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedMessageInput;

import static io.netty.buffer.Unpooled.*;

public class HttpChunkedBodyEncoder
    implements ChunkedMessageInput<HttpContent>
{

    private final ChunkedInput<ByteBuf> chunkedBody;
    private final int chunkSize;
    private boolean isLastChunk = false;
    private boolean isLastChunkSent = false;

    public HttpChunkedBodyEncoder( ChunkedInput<ByteBuf> chunkedBody, int chunkSize )
    {
        this.chunkedBody = chunkedBody;
        this.chunkSize = chunkSize;
    }

    @Override
    public boolean isEndOfInput()
        throws Exception
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
    public boolean readChunk( MessageBuf<HttpContent> buffer )
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
            return new DefaultLastHttpContent( EMPTY_BUFFER );
        }
        ByteBuf buffer = Unpooled.buffer( chunkSize );
        if( !chunkedBody.readChunk( buffer ) )
        {
            isLastChunk = true;
        }
        return new DefaultHttpContent( buffer );
    }
}
