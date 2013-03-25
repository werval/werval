package org.qiweb.runtime.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.http.RequestBody;
import org.qiweb.runtime.server.FileByteBuff;

public class RequestBodyInstance
    implements RequestBody
{

    private final ByteBuf byteBuf;
    private final Map<String, List<String>> attributes;
    private final Map<String, List<Upload>> uploads;

    /**
     * Create a new EMPTY RequestBody.
     */
    public RequestBodyInstance()
    {
        this.byteBuf = null;
        this.attributes = Collections.emptyMap();
        this.uploads = Collections.emptyMap();
    }

    public RequestBodyInstance( ByteBuf byteBuf )
    {
        this.byteBuf = byteBuf;
        this.attributes = Collections.emptyMap();
        this.uploads = Collections.emptyMap();
    }

    public RequestBodyInstance( Map<String, List<String>> attributes, Map<String, List<Upload>> uploads )
    {
        this.byteBuf = null;
        this.attributes = attributes;
        this.uploads = uploads;
    }

    @Override
    public Map<String, List<String>> formAttributes()
    {
        return Collections.unmodifiableMap( attributes );
    }

    @Override
    public Map<String, List<Upload>> formUploads()
    {
        return Collections.unmodifiableMap( uploads );
    }

    @Override
    public InputStream asStream()
    {
        if( byteBuf != null )
        {
            if( byteBuf instanceof FileByteBuff )
            {
                return ( (FileByteBuff) byteBuf ).getInputStream();
            }
            return new ByteBufInputStream( byteBuf );
        }
        return new ByteArrayInputStream( new byte[ 0 ] );
    }

    @Override
    public byte[] asBytes()
    {
        if( byteBuf != null )
        {
            if( byteBuf instanceof FileByteBuff )
            {
                return ( (FileByteBuff) byteBuf ).readAllBytes();
            }
            byte[] bytes = new byte[ byteBuf.readableBytes() ];
            byteBuf.readBytes( bytes, 0, byteBuf.readableBytes() );
            return bytes;
        }
        return new byte[ 0 ];
    }

    @Override
    public String asString()
    {
        if( byteBuf != null )
        {
            return byteBuf.toString( Charset.forName( "UTF-8" ) );
        }
        return Strings.EMPTY;
    }

    @Override
    public String asString( Charset charset )
    {
        if( byteBuf != null )
        {
            return byteBuf.toString( charset );
        }
        return Strings.EMPTY;
    }

    public static class UploadInstance
        implements Upload
    {

        private final String contentType;
        private final Charset charset;
        private final String filename;
        private final File temporaryFile;

        public UploadInstance( String contentType, Charset charset, String filename, File temporaryFile )
        {
            this.contentType = contentType;
            this.charset = charset;
            this.filename = filename;
            this.temporaryFile = temporaryFile;
        }

        @Override
        public String contentType()
        {
            return contentType;
        }

        @Override
        public Charset charset()
        {
            return charset;
        }

        @Override
        public String filename()
        {
            return filename;
        }

        @Override
        public long length()
        {
            return temporaryFile.length();
        }

        @Override
        public InputStream asStream()
        {
            try
            {
                return new FileInputStream( temporaryFile );
            }
            catch( FileNotFoundException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        public byte[] asBytes()
        {
            try
            {
                return Files.readAllBytes( temporaryFile.toPath() );
            }
            catch( IOException ex )
            {
                throw new RuntimeException( ex.getMessage(), ex );
            }
        }

        @Override
        public String asString()
        {
            return new String( asBytes(), Charset.forName( "UTF-8" ) );
        }

        @Override
        public String asString( Charset charset )
        {
            return new String( asBytes(), charset );
        }

        @Override
        public boolean renameTo( File destination )
        {
            return temporaryFile.renameTo( destination );
        }
    }
}
