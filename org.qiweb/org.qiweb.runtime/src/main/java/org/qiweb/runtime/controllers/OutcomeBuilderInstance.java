package org.qiweb.runtime.controllers;

import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.qiweb.api.Config;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.controllers.OutcomeBuilder;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableCookies;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.runtime.http.HeadersInstance;

import static io.netty.buffer.Unpooled.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * Outcome Builder instance.
 */
public class OutcomeBuilderInstance
    implements OutcomeBuilder
{

    private abstract static class AbstractOutcome<T extends AbstractOutcome<?>>
        implements Outcome
    {

        private final int status;
        protected final MutableHeaders headers = new HeadersInstance();

        private AbstractOutcome( int status )
        {
            this.status = status;
        }

        @Override
        public final int status()
        {
            return status;
        }

        @Override
        public StatusClass statusClass()
        {
            return StatusClass.valueOf( status );
        }

        @Override
        public final Headers headers()
        {
            return headers;
        }

        @Override
        public String toString()
        {
            return status + ", " + headers;
        }
    }

    public static class SimpleOutcome
        extends AbstractOutcome<SimpleOutcome>
    {

        private ByteBuf body = EMPTY_BUFFER;

        /* package */ SimpleOutcome( int status )
        {
            super( status );
        }

        public ByteBuf body()
        {
            return body;
        }

        /* package */ final SimpleOutcome withEntity( ByteBuf body )
        {
            this.body = body;
            return this;
        }
    }

    public static class StreamOutcome
        extends AbstractOutcome<StreamOutcome>
    {

        private final InputStream bodyInputStream;
        private final long contentLength;

        /* package */ StreamOutcome( int status, InputStream bodyInputStream, long contentLength )
        {
            super( status );
            this.bodyInputStream = bodyInputStream;
            this.contentLength = contentLength;
            this.headers.with( CONTENT_LENGTH, String.valueOf( contentLength ) );
        }

        public final InputStream bodyInputStream()
        {
            return bodyInputStream;
        }

        public final long contentLength()
        {
            return contentLength;
        }
    }

    public static class ChunkedOutcome
        extends AbstractOutcome<ChunkedOutcome>
    {

        private ChunkedInput<ByteBuf> input = new ChunkedStream( new ByteArrayInputStream( "".getBytes( UTF_8 ) ) );

        /* package */ ChunkedOutcome( int status, InputStream input, int chunkSize )
        {
            super( status );
            this.input = new ChunkedStream( input, chunkSize );
        }

        public ChunkedInput<ByteBuf> chunkedInput()
        {
            return input;
        }
    }
    private final int status;
    private final MutableHeaders headers;
    private final MutableCookies cookies;
    private Object body = EMPTY_BUFFER;
    private long length = 0;
    private int chunkSize;


    /* package */ OutcomeBuilderInstance( int status, Config config, MutableHeaders headers, MutableCookies cookies )
    {
        this.status = status;
        this.headers = headers;
        this.cookies = cookies;
        this.chunkSize = config.intNumber( "qiweb.http.chunksize" );
    }

    @Override
    public OutcomeBuilder withHeader( String name, String value )
    {
        headers.with( name, value );
        return this;
    }

    @Override
    public OutcomeBuilder as( String contentType )
    {
        headers.withSingle( CONTENT_TYPE, contentType );
        return this;
    }

    @Override
    public OutcomeBuilder withBody( String bodyString )
    {
        ByteBuf buffer = copiedBuffer( bodyString, UTF_8 );
        body = buffer;
        length = buffer.readableBytes();
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream )
    {
        body = bodyInputStream;
        length = -1;
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream, int overridenChunkSize )
    {
        body = bodyInputStream;
        length = -1;
        chunkSize = overridenChunkSize;
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream, long bodyLength )
    {
        body = bodyInputStream;
        length = bodyLength;
        return this;
    }

    @Override
    public Outcome build()
    {
        if( body == null )
        {
            return new SimpleOutcome( status );
        }
        if( body instanceof ByteBuf )
        {
            ByteBuf bodyByteBuf = (ByteBuf) body;
            return new SimpleOutcome( status ).withEntity( bodyByteBuf );
        }
        if( body instanceof InputStream )
        {
            InputStream bodyInputStream = (InputStream) body;
            if( length != -1 )
            {
                return new StreamOutcome( status, bodyInputStream, length );
            }
            return new ChunkedOutcome( status, bodyInputStream, chunkSize );
        }
        throw new UnsupportedOperationException( "Unsupported body type ( " + body.getClass() + " ) " + body );
    }
}
