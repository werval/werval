package org.qiweb.runtime.util;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.file.Files;
import org.qiweb.api.exceptions.QiWebException;

/**
 * Read-only ByteBuff wrapping a File.
 * <p>Used by {@link HttpOnDiskRequestAggregator} to aggregate requests bodies.</p>
 * <p>Used by {@link HttpRouterHandler} to parse requests bodies.</p>
 */
// CHECKSTYLE:OFF
public final class FileByteBuff
    extends AbstractByteBuf
{

    private static final String NOT_SUPPORTED = "Not supported";
    private static final String READ_ONLY = "Read Only.";
    private final File file;
    private final RandomAccessFile raf;
    private final long length;

    public FileByteBuff( File file )
        throws FileNotFoundException
    {
        super( Integer.MAX_VALUE );
        this.file = file;
        this.raf = new RandomAccessFile( file, "r" );
        this.length = file.length();
    }

    public InputStream getInputStream()
    {
        try
        {
            return new FileInputStream( file );
        }
        catch( FileNotFoundException ex )
        {
            throw new QiWebException( "File " + file + " backing FileByteBuff disapeared! " + ex.getMessage(), ex );
        }
    }

    public byte[] readAllBytes()
    {
        try
        {
            return Files.readAllBytes( file.toPath() );
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    protected byte _getByte( int index )
    {
        try
        {
            raf.seek( index );
            return raf.readByte();
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    protected short _getShort( int index )
    {
        try
        {
            raf.seek( index );
            return raf.readShort();
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    protected int _getUnsignedMedium( int index )
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    protected int _getInt( int index )
    {
        try
        {
            raf.seek( index );
            return raf.readInt();
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    protected long _getLong( int index )
    {
        try
        {
            raf.seek( index );
            return raf.readLong();
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    protected void _setByte( int index, int value )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    protected void _setShort( int index, int value )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    protected void _setMedium( int index, int value )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    protected void _setInt( int index, int value )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    protected void _setLong( int index, long value )
    {
        throw new UnsupportedOperationException( READ_ONLY );
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
        throw new UnsupportedOperationException( READ_ONLY );
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
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public ByteBuf getBytes( int index, byte[] dst, int dstIndex, int length )
    {
        checkIndex( index, length );
        checkDstIndex( index, length, dstIndex, dst.length );
        try
        {
            raf.seek( index );
            raf.read( dst, dstIndex, length );
            return this;
        }
        catch( IOException ex )
        {
            throw new QiWebException( ex.getMessage(), ex );
        }
    }

    @Override
    public ByteBuf getBytes( int index, ByteBuffer dst )
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public ByteBuf getBytes( int index, OutputStream out, int length )
        throws IOException
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public int getBytes( int index, GatheringByteChannel out, int length )
        throws IOException
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public ByteBuf setBytes( int index, ByteBuf src, int srcIndex, int length )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    public ByteBuf setBytes( int index, byte[] src, int srcIndex, int length )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    public ByteBuf setBytes( int index, ByteBuffer src )
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    public int setBytes( int index, InputStream in, int length )
        throws IOException
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    public int setBytes( int index, ScatteringByteChannel in, int length )
        throws IOException
    {
        throw new UnsupportedOperationException( READ_ONLY );
    }

    @Override
    public ByteBuf copy( int index, int length )
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public int nioBufferCount()
    {
        return -1;
    }

    @Override
    public ByteBuffer nioBuffer( int index, int length )
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public ByteBuffer[] nioBuffers( int index, int length )
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public boolean hasArray()
    {
        return false;
    }

    @Override
    public byte[] array()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public int arrayOffset()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public boolean hasMemoryAddress()
    {
        return false;
    }

    @Override
    public long memoryAddress()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
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

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }
}
// CHECKSTYLE:ON
