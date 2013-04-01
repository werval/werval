package org.qiweb.runtime.controllers;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.qiweb.api.QiWebException;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.runtime.http.HeadersInstance;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.util.CharsetUtil.UTF_8;

public final class Outcomes
{

    /**
     * @return a 200 OK Outcome
     */
    public static SimpleOutcome ok()
    {
        return new SimpleOutcome( OK );
    }

    /**
     * @return a 201 CREATED Outcome
     */
    public static SimpleOutcome created()
    {
        return new SimpleOutcome( CREATED );
    }

    /**
     * @return a 202 ACCEPTED Outcome
     */
    public static SimpleOutcome accepted()
    {
        return new SimpleOutcome( ACCEPTED );
    }

    /**
     * @return a 203 NON_AUTHORITATIVE_INFORMATION Outcome
     */
    public static SimpleOutcome nonAuthoritativeInformation()
    {
        return new SimpleOutcome( NON_AUTHORITATIVE_INFORMATION );
    }

    /**
     * @return a 204 NO_CONTENT Outcome
     */
    public static SimpleOutcome noContent()
    {
        return new SimpleOutcome( NO_CONTENT );
    }

    /**
     * @return a 205 RESET_CONTENT Outcome
     */
    public static SimpleOutcome resetContent()
    {
        return new SimpleOutcome( RESET_CONTENT );
    }

    /**
     * @return a 206 PARTIAL_CONTENT Outcome
     */
    public static SimpleOutcome partialContent()
    {
        return new SimpleOutcome( PARTIAL_CONTENT );
    }

    /**
     * @return a 207 MULTI_STATUS Outcome
     */
    public static SimpleOutcome multiStatus()
    {
        return new SimpleOutcome( MULTI_STATUS );
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public static SimpleOutcome found( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), FOUND.code() );
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public static SimpleOutcome found( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, FOUND.code() );
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public static SimpleOutcome seeOther( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), SEE_OTHER.code() );
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public static SimpleOutcome seeOther( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, SEE_OTHER.code() );
    }

    /**
     * @return a 304 NOT_MODIFIED Outcome
     */
    public static SimpleOutcome notModified()
    {
        return new SimpleOutcome( NOT_MODIFIED );
    }

    /**
     * @return a 307 TEMPORARY_REDIRECT Outcome
     */
    public static SimpleOutcome temporaryRedirect( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), TEMPORARY_REDIRECT.code() );
    }

    /**
     * @return a 307 TEMPORARY_REDIRECT Outcome
     */
    public static SimpleOutcome temporaryRedirect( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, TEMPORARY_REDIRECT.code() );
    }

    /**
     * @return a 400 BAD_REQUEST Outcome
     */
    public static SimpleOutcome badRequest()
    {
        return new SimpleOutcome( BAD_REQUEST );
    }

    /**
     * @return a 401 UNAUTHORIZED Outcome
     */
    public static SimpleOutcome unauthorized()
    {
        return new SimpleOutcome( UNAUTHORIZED );
    }

    /**
     * @return a 403 FORBIDDEN Outcome
     */
    public static SimpleOutcome forbidden()
    {
        return new SimpleOutcome( FORBIDDEN );
    }

    /**
     * @return a 404 NOT_FOUND Outcome
     */
    public static SimpleOutcome notFound()
    {
        return new SimpleOutcome( NOT_FOUND );
    }

    /**
     * @return a 405 METHOD_NOT_ALLOWED Outcome
     */
    public static SimpleOutcome methodNotAllowed()
    {
        return new SimpleOutcome( METHOD_NOT_ALLOWED );
    }

    /**
     * @return a 406 NOT_ACCEPTABLE Outcome
     */
    public static SimpleOutcome notAcceptable()
    {
        return new SimpleOutcome( NOT_ACCEPTABLE );
    }

    /**
     * @return a 408 REQUEST_TIMEOUT Outcome
     */
    public static SimpleOutcome requestTimeout()
    {
        return new SimpleOutcome( REQUEST_TIMEOUT );
    }

    /**
     * @return a 409 CONFLICT Outcome
     */
    public static SimpleOutcome conflict()
    {
        return new SimpleOutcome( CONFLICT );
    }

    /**
     * @return a 410 GONE Outcome
     */
    public static SimpleOutcome gone()
    {
        return new SimpleOutcome( GONE );
    }

    /**
     * @return a 412 PRECONDITION_FAILED Outcome
     */
    public static SimpleOutcome preconditionFailed()
    {
        return new SimpleOutcome( PRECONDITION_FAILED );
    }

    /**
     * @return a 413 REQUEST_ENTITY_TOO_LARGE Outcome
     */
    public static SimpleOutcome requestEntityTooLarge()
    {
        return new SimpleOutcome( REQUEST_ENTITY_TOO_LARGE );
    }

    /**
     * @return a 414 REQUEST_URI_TOO_LONG Outcome
     */
    public static SimpleOutcome requestUriTooLong()
    {
        return new SimpleOutcome( REQUEST_URI_TOO_LONG );
    }

    /**
     * @return a 415 UNSUPPORTED_MEDIA_TYPE Outcome
     */
    public static SimpleOutcome unsupportedMediaType()
    {
        return new SimpleOutcome( UNSUPPORTED_MEDIA_TYPE );
    }

    /**
     * @return a 417 EXPECTATION_FAILED Outcome
     */
    public static SimpleOutcome expectationFailed()
    {
        return new SimpleOutcome( EXPECTATION_FAILED );
    }

    /**
     * @return a 422 UNPROCESSABLE_ENTITY Outcome
     */
    public static SimpleOutcome unprocessableEntity()
    {
        return new SimpleOutcome( UNPROCESSABLE_ENTITY );
    }

    /**
     * @return a 423 LOCKED Outcome
     */
    public static SimpleOutcome locked()
    {
        return new SimpleOutcome( LOCKED );
    }

    /**
     * @return a 424 FAILED_DEPENDENCY Outcome
     */
    public static SimpleOutcome failedDependency()
    {
        return new SimpleOutcome( FAILED_DEPENDENCY );
    }

    /**
     * @return a 429 TOO_MANY_REQUEST Outcome
     */
    public static SimpleOutcome tooManyRequest()
    {
        return new SimpleOutcome( 429 );
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Outcome
     */
    public static SimpleOutcome internalServerError()
    {
        return new SimpleOutcome( INTERNAL_SERVER_ERROR );
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Outcome
     */
    public static SimpleOutcome notImplemented()
    {
        return new SimpleOutcome( NOT_IMPLEMENTED );
    }

    /**
     * @return a 502 BAD_GATEWAY Outcome
     */
    public static SimpleOutcome badGateway()
    {
        return new SimpleOutcome( BAD_GATEWAY );
    }

    /**
     * @return a 503 SERVICE_UNAVAILABLE Outcome
     */
    public static SimpleOutcome serviceUnavailable()
    {
        return new SimpleOutcome( SERVICE_UNAVAILABLE );
    }

    /**
     * @return a 504 GATEWAY_TIMEOUT Outcome
     */
    public static SimpleOutcome gatewayTimeout()
    {
        return new SimpleOutcome( GATEWAY_TIMEOUT );
    }

    /**
     * @return a 505 HTTP_VERSION_NOT_SUPPORTED Outcome
     */
    public static SimpleOutcome httpVersionNotSupported()
    {
        return new SimpleOutcome( HTTP_VERSION_NOT_SUPPORTED );
    }

    /**
     * @return a 507 INSUFFICIENT_STORAGE Outcome
     */
    public static SimpleOutcome insufficientStorage()
    {
        return new SimpleOutcome( INSUFFICIENT_STORAGE );
    }

    public static SimpleOutcome redirect( String url, Map<String, List<String>> queryString, int status )
    {
        try
        {
            // FIXME Move URL generation out of Outcomes!
            StringBuilder fullUrl = new StringBuilder( url );
            Iterator<Entry<String, List<String>>> itKey = queryString.entrySet().iterator();
            if( itKey.hasNext() )
            {
                fullUrl.append( url.contains( "?" ) ? '&' : '?' );
                while( itKey.hasNext() )
                {
                    Entry<String, List<String>> entry = itKey.next();
                    String paramName = entry.getKey();
                    for( Iterator<String> itVal = entry.getValue().iterator(); itVal.hasNext(); )
                    {
                        String paramValue = itVal.next();
                        fullUrl.append( paramName ).append( "=" ).append( URLEncoder.encode( paramValue, "utf-8" ) ).append( itVal.hasNext() ? "&" : "" );
                    }
                    fullUrl.append( itKey.hasNext() ? "&" : "" );
                }
            }
            return new SimpleOutcome( status ).withHeader( HttpHeaders.Names.LOCATION, fullUrl.toString() );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new QiWebException( "UTF-8 Charset is not supported oO", ex );
        }
    }

    private abstract static class AbstractOutcome<T extends AbstractOutcome<?>>
        implements Outcome
    {

        private final int status;
        private final MutableHeaders headers = new HeadersInstance();

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
    }

    public abstract static class StreamOutcome
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

    public abstract static class ChunkedOutcome
        extends AbstractOutcome<ChunkedOutcome>
    {

        public ChunkedOutcome( int status )
        {
            super( status );
        }

        private ChunkedOutcome( HttpResponseStatus status )
        {
            super( status.code() );
        }
    }

    private Outcomes()
    {
    }
}
