package org.qiweb.runtime.controllers;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
        public final Headers headers()
        {
            return headers;
        }

        @SuppressWarnings( "unchecked" )
        public final T withHeader( String name, String value )
        {
            headers.with( name, value );
            return (T) this;
        }

        @SuppressWarnings( "unchecked" )
        public final T as( String contentType )
        {
            headers.withSingle( CONTENT_TYPE, contentType );
            return (T) this;
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

        private ByteBuf entity = EMPTY_BUFFER;

        public SimpleOutcome( int status )
        {
            super( status );
        }

        private SimpleOutcome( HttpResponseStatus status )
        {
            super( status.code() );
        }

        public ByteBuf entity()
        {
            return entity;
        }

        public final SimpleOutcome withoutEntity()
        {
            entity = EMPTY_BUFFER;
            return this;
        }

        public final SimpleOutcome withEntity( ByteBuf entity )
        {
            this.entity = entity;
            return this;
        }

        public final SimpleOutcome withEntity( String entity )
        {
            this.entity = copiedBuffer( entity, UTF_8 );
            return this;
        }

        public final ChunkedOutcome withEntity( InputStream input )
        {
            ChunkedOutcome outcome = new ChunkedOutcome( status() );
            outcome.headers.withAll( headers );
            return outcome.withEntity( input );
        }
    }

    public static class StreamOutcome
        extends AbstractOutcome<StreamOutcome>
    {

        private final InputStream bodyInputStream;
        private final int contentLength;

        public StreamOutcome( int status, InputStream bodyInputStream, int contentLength )
        {
            super( status );
            this.bodyInputStream = bodyInputStream;
            this.contentLength = contentLength;
            withHeader( CONTENT_LENGTH, String.valueOf( contentLength ) );
        }

        private StreamOutcome( HttpResponseStatus status, InputStream input, int contentLength )
        {
            this( status.code(), input, contentLength );
        }

        public final InputStream bodyInputStream()
        {
            return bodyInputStream;
        }

        public final int contentLength()
        {
            return contentLength;
        }
    }

    public static class ChunkedOutcome
        extends AbstractOutcome<ChunkedOutcome>
    {

        private ChunkedInput<ByteBuf> input = new ChunkedStream( new ByteArrayInputStream( "".getBytes( UTF_8 ) ) );

        public ChunkedOutcome( int status )
        {
            super( status );
        }

        private ChunkedOutcome( HttpResponseStatus status )
        {
            super( status.code() );
        }

        public ChunkedInput<ByteBuf> chunkedInput()
        {
            return input;
        }

        /* package */ ChunkedOutcome withEntity( InputStream input )
        {
            this.input = new ChunkedStream( input );
            return this;
        }
    }
    private final int status;
    private final MutableHeaders headers;
    private final MutableCookies cookies;
    private Object body = EMPTY_BUFFER;
    private int length = 0;

    /* package */ OutcomeBuilderInstance( int status, MutableHeaders headers, MutableCookies cookies )
    {
        this.status = status;
        this.headers = headers;
        this.cookies = cookies;
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
        headers.withSingle( "Content-Type", contentType );
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
    public OutcomeBuilder withBody( InputStream bodyInputStream, int bodyLength )
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
            return new ChunkedOutcome( status ).withEntity( bodyInputStream );
        }
        throw new UnsupportedOperationException( "Unsupported body type ( " + body.getClass() + " ) " + body );
    }
}
