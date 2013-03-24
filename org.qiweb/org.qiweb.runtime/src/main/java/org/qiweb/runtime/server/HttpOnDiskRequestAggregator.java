package org.qiweb.runtime.server;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.BufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.UUID;
import org.codeartisans.java.toolbox.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.removeTransferEncodingChunked;
import static io.netty.util.CharsetUtil.US_ASCII;

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
public class HttpOnDiskRequestAggregator
    extends MessageToMessageDecoder<HttpObject>
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpOnDiskRequestAggregator.class );
    private static final ByteBuf CONTINUE = copiedBuffer( "HTTP/1.1 100 Continue\r\n\r\n", US_ASCII );
    private final int maxContentLength;
    private HttpRequest aggregatedRequestHeader;
    private File bodyFile;
    private OutputStream bodyOutputStream;

    public HttpOnDiskRequestAggregator( int maxContentLength )
    {
        this.maxContentLength = maxContentLength;
    }

    @Override
    protected Object decode( ChannelHandlerContext ctx, HttpObject msg )
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
            return msg; // Nothing to do with this message
        }

        HttpRequest currentRequestHeader = aggregatedRequestHeader;

        if( msg instanceof HttpRequest )
        {
            assert currentRequestHeader == null;

            HttpRequest newRequestHeader = (HttpRequest) msg;

            if( is100ContinueExpected( newRequestHeader ) )
            {
                ctx.write( CONTINUE.duplicate() );
            }

            if( !newRequestHeader.getDecoderResult().isSuccess() )
            {
                removeTransferEncodingChunked( newRequestHeader );
                aggregatedRequestHeader = null;
                BufUtil.retain( newRequestHeader );
                return newRequestHeader;
            }
            currentRequestHeader = new DefaultHttpRequest( newRequestHeader.getProtocolVersion(),
                                                           newRequestHeader.getMethod(),
                                                           newRequestHeader.getUri() );
            currentRequestHeader.headers().set( newRequestHeader.headers() );
            removeTransferEncodingChunked( currentRequestHeader );

            aggregatedRequestHeader = currentRequestHeader;
            bodyFile = new File( new File( System.getProperty( "java.io.tmpdir" ) ), UUID.randomUUID().toString() );
            bodyOutputStream = new FileOutputStream( bodyFile, true );

            LOG.debug( "Aggregating request ({} {}) to {}", aggregatedRequestHeader.getMethod(), aggregatedRequestHeader.getUri(), bodyFile );

            return null;
        }
        else if( msg instanceof HttpContent )
        {
            assert currentRequestHeader != null;

            HttpContent chunk = (HttpContent) msg;

            if( maxContentLength != -1 && bodyFile.length() > maxContentLength - chunk.data().readableBytes() )
            {
                // TODO: Respond with 413 Request Entity Too Large
                //   and  close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw new TooLongFrameException( "HTTP content length exceeded " + maxContentLength + " bytes." );
            }

            // Append chunk data to aggregated File
            if( chunk.data().isReadable() )
            {
                chunk.data().readBytes( bodyOutputStream, chunk.data().readableBytes() );
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

                return fullRequest;
            }
            else
            {
                // Continue
                return null;
            }
        }
        else
        {
            throw new Error();
        }
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx )
        throws Exception
    {
        Files.delete( bodyFile );
        super.channelInactive( ctx );
    }

    /**
     * Read-only ByteBuff wrapping a File.
     */
    private static class FileByteBuff
        extends AbstractByteBuf
    {

        private final RandomAccessFile file;
        private final long length;

        private FileByteBuff( File file )
            throws FileNotFoundException
        {
            super( Integer.MAX_VALUE );
            this.file = new RandomAccessFile( file, "r" );
            this.length = file.length();
        }

        @Override
        protected byte _getByte( int index )
        {
            try
            {
                file.seek( index );
                return file.readByte();
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        protected short _getShort( int index )
        {
            try
            {
                file.seek( index );
                return file.readShort();
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        protected int _getUnsignedMedium( int index )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        protected int _getInt( int index )
        {
            try
            {
                file.seek( index );
                return file.readInt();
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        protected long _getLong( int index )
        {
            try
            {
                file.seek( index );
                return file.readLong();
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        protected void _setByte( int index, int value )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        protected void _setShort( int index, int value )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        protected void _setMedium( int index, int value )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        protected void _setInt( int index, int value )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        protected void _setLong( int index, long value )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public int capacity()
        {
            if( length < Integer.MIN_VALUE || length > Integer.MAX_VALUE )
            {
                throw new IllegalArgumentException( length + " cannot be cast to int without changing its value." );
            }
            return (int) length;
        }

        @Override
        public ByteBuf capacity( int newCapacity )
        {
            throw new UnsupportedOperationException( "Read Only." );
        }

        @Override
        public ByteBufAllocator alloc()
        {
            return null;
        }

        @Override
        public ByteOrder order()
        {
            return ByteOrder.nativeOrder();
        }

        @Override
        public ByteBuf unwrap()
        {
            return null;
        }

        @Override
        public boolean isDirect()
        {
            return true;
        }

        @Override
        public ByteBuf getBytes( int index, ByteBuf dst, int dstIndex, int length )
        {
            checkIndex( index, length );
            checkDstIndex( index, length, dstIndex, dst.capacity() );
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public ByteBuf getBytes( int index, byte[] dst, int dstIndex, int length )
        {
            checkIndex( index, length );
            checkDstIndex( index, length, dstIndex, dst.length );
            try
            {
                file.seek( index );
                file.read( dst, dstIndex, length );
                return this;
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        public ByteBuf getBytes( int index, ByteBuffer dst )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public ByteBuf getBytes( int index, OutputStream out, int length )
            throws IOException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public int getBytes( int index, GatheringByteChannel out, int length )
            throws IOException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public ByteBuf setBytes( int index, ByteBuf src, int srcIndex, int length )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public ByteBuf setBytes( int index, byte[] src, int srcIndex, int length )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public ByteBuf setBytes( int index, ByteBuffer src )
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public int setBytes( int index, InputStream in, int length )
            throws IOException
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public int setBytes( int index, ScatteringByteChannel in, int length )
            throws IOException
        {
            throw new UnsupportedOperationException( "Read Only" );
        }

        @Override
        public ByteBuf copy( int index, int length )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public int nioBufferCount()
        {
            return -1;
        }

        @Override
        public ByteBuffer nioBuffer( int index, int length )
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public ByteBuffer[] nioBuffers( int index, int length )
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public boolean hasArray()
        {
            return false;
        }

        @Override
        public byte[] array()
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public int arrayOffset()
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public boolean hasMemoryAddress()
        {
            return false;
        }

        @Override
        public long memoryAddress()
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public ByteBuf suspendIntermediaryDeallocations()
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public ByteBuf resumeIntermediaryDeallocations()
        {
            throw new UnsupportedOperationException( "Not supported." );
        }

        @Override
        public ByteBuf retain( int increment )
        {
            return this;
        }

        @Override
        public ByteBuf retain()
        {
            return this;
        }

        @Override
        public int refCnt()
        {
            return 1;
        }

        @Override
        public boolean release()
        {
            return false;
        }

        @Override
        public boolean release( int decrement )
        {
            return false;
        }
    }
}
