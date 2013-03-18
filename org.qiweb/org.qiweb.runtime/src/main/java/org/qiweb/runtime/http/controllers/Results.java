package org.qiweb.runtime.http.controllers;

import org.qiweb.api.http.Result;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.util.CharsetUtil.UTF_8;

public class Results
{

    /**
     * @return a 200 OK Result
     */
    public static SimpleResult ok()
    {
        return new SimpleResult( OK );
    }

    /**
     * @return a 201 CREATED Result
     */
    public static SimpleResult created()
    {
        return new SimpleResult( CREATED );
    }

    /**
     * @return a 202 ACCEPTED Result
     */
    public static SimpleResult accepted()
    {
        return new SimpleResult( ACCEPTED );
    }

    /**
     * @return a 203 NON_AUTHORITATIVE_INFORMATION Result
     */
    public static SimpleResult nonAuthoritativeInformation()
    {
        return new SimpleResult( NON_AUTHORITATIVE_INFORMATION );
    }

    /**
     * @return a 204 NO_CONTENT Result
     */
    public static SimpleResult noContent()
    {
        return new SimpleResult( NO_CONTENT );
    }

    /**
     * @return a 205 RESET_CONTENT Result
     */
    public static SimpleResult resetContent()
    {
        return new SimpleResult( RESET_CONTENT );
    }

    /**
     * @return a 206 PARTIAL_CONTENT Result
     */
    public static SimpleResult partialContent()
    {
        return new SimpleResult( PARTIAL_CONTENT );
    }

    /**
     * @return a 207 MULTI_STATUS Result
     */
    public static SimpleResult multiStatus()
    {
        return new SimpleResult( MULTI_STATUS );
    }

    /**
     * @return a 302 FOUND Result
     */
    public static SimpleResult found( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), FOUND.code() );
    }

    /**
     * @return a 302 FOUND Result
     */
    public static SimpleResult found( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, FOUND.code() );
    }

    /**
     * @return a 303 SEE_OTHER Result
     */
    public static SimpleResult seeOther( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), SEE_OTHER.code() );
    }

    /**
     * @return a 303 SEE_OTHER Result
     */
    public static SimpleResult seeOther( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, SEE_OTHER.code() );
    }

    /**
     * @return a 304 NOT_MODIFIED Result
     */
    public static SimpleResult notModified()
    {
        return new SimpleResult( NOT_MODIFIED );
    }

    /**
     * @return a 307 TEMPORARY_REDIRECT Result
     */
    public static SimpleResult temporaryRedirect( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), TEMPORARY_REDIRECT.code() );
    }

    /**
     * @return a 307 TEMPORARY_REDIRECT Result
     */
    public static SimpleResult temporaryRedirect( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, TEMPORARY_REDIRECT.code() );
    }

    /**
     * @return a 400 BAD_REQUEST Result
     */
    public static SimpleResult badRequest()
    {
        return new SimpleResult( BAD_REQUEST );
    }

    /**
     * @return a 401 UNAUTHORIZED Result
     */
    public static SimpleResult unauthorized()
    {
        return new SimpleResult( UNAUTHORIZED );
    }

    /**
     * @return a 403 FORBIDDEN Result
     */
    public static SimpleResult forbidden()
    {
        return new SimpleResult( FORBIDDEN );
    }

    /**
     * @return a 404 NOT_FOUND Result
     */
    public static SimpleResult notFound()
    {
        return new SimpleResult( NOT_FOUND );
    }

    /**
     * @return a 405 METHOD_NOT_ALLOWED Result
     */
    public static SimpleResult methodNotAllowed()
    {
        return new SimpleResult( METHOD_NOT_ALLOWED );
    }

    /**
     * @return a 406 NOT_ACCEPTABLE Result
     */
    public static SimpleResult notAcceptable()
    {
        return new SimpleResult( NOT_ACCEPTABLE );
    }

    /**
     * @return a 408 REQUEST_TIMEOUT Result
     */
    public static SimpleResult requestTimeout()
    {
        return new SimpleResult( REQUEST_TIMEOUT );
    }

    /**
     * @return a 409 CONFLICT Result
     */
    public static SimpleResult conflict()
    {
        return new SimpleResult( CONFLICT );
    }

    /**
     * @return a 410 GONE Result
     */
    public static SimpleResult gone()
    {
        return new SimpleResult( GONE );
    }

    /**
     * @return a 412 PRECONDITION_FAILED Result
     */
    public static SimpleResult preconditionFailed()
    {
        return new SimpleResult( PRECONDITION_FAILED );
    }

    /**
     * @return a 413 REQUEST_ENTITY_TOO_LARGE Result
     */
    public static SimpleResult requestEntityTooLarge()
    {
        return new SimpleResult( REQUEST_ENTITY_TOO_LARGE );
    }

    /**
     * @return a 414 REQUEST_URI_TOO_LONG Result
     */
    public static SimpleResult requestUriTooLong()
    {
        return new SimpleResult( REQUEST_URI_TOO_LONG );
    }

    /**
     * @return a 415 UNSUPPORTED_MEDIA_TYPE Result
     */
    public static SimpleResult unsupportedMediaType()
    {
        return new SimpleResult( UNSUPPORTED_MEDIA_TYPE );
    }

    /**
     * @return a 417 EXPECTATION_FAILED Result
     */
    public static SimpleResult expectationFailed()
    {
        return new SimpleResult( EXPECTATION_FAILED );
    }

    /**
     * @return a 422 UNPROCESSABLE_ENTITY Result
     */
    public static SimpleResult unprocessableEntity()
    {
        return new SimpleResult( UNPROCESSABLE_ENTITY );
    }

    /**
     * @return a 423 LOCKED Result
     */
    public static SimpleResult locked()
    {
        return new SimpleResult( LOCKED );
    }

    /**
     * @return a 424 FAILED_DEPENDENCY Result
     */
    public static SimpleResult failedDependency()
    {
        return new SimpleResult( FAILED_DEPENDENCY );
    }

    /**
     * @return a 429 TOO_MANY_REQUEST Result
     */
    public static SimpleResult tooManyRequest()
    {
        return new SimpleResult( 429 );
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Result
     */
    public static SimpleResult internalServerError()
    {
        return new SimpleResult( INTERNAL_SERVER_ERROR );
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Result
     */
    public static SimpleResult notImplemented()
    {
        return new SimpleResult( NOT_IMPLEMENTED );
    }

    /**
     * @return a 502 BAD_GATEWAY Result
     */
    public static SimpleResult badGateway()
    {
        return new SimpleResult( BAD_GATEWAY );
    }

    /**
     * @return a 503 SERVICE_UNAVAILABLE Result
     */
    public static SimpleResult serviceUnavailable()
    {
        return new SimpleResult( SERVICE_UNAVAILABLE );
    }

    /**
     * @return a 504 GATEWAY_TIMEOUT Result
     */
    public static SimpleResult gatewayTimeout()
    {
        return new SimpleResult( GATEWAY_TIMEOUT );
    }

    /**
     * @return a 505 HTTP_VERSION_NOT_SUPPORTED Result
     */
    public static SimpleResult httpVersionNotSupported()
    {
        return new SimpleResult( HTTP_VERSION_NOT_SUPPORTED );
    }

    /**
     * @return a 507 INSUFFICIENT_STORAGE Result
     */
    public static SimpleResult insufficientStorage()
    {
        return new SimpleResult( INSUFFICIENT_STORAGE );
    }

    public static SimpleResult redirect( String url, Map<String, List<String>> queryString, int status )
    {
        try
        {
            String fullUrl = url;
            Iterator<String> itNames = queryString.keySet().iterator();
            if( itNames.hasNext() )
            {
                fullUrl += url.contains( "?" ) ? '&' : '?';
                while( itNames.hasNext() )
                {
                    String paramName = itNames.next();
                    Iterator<String> itValues = queryString.get( paramName ).iterator();
                    while( itValues.hasNext() )
                    {
                        String paramValue = itValues.next();
                        fullUrl += paramName + "=" + URLEncoder.encode( paramValue, "utf-8" )
                                   + ( itValues.hasNext() ? "&" : "" );

                    }
                    fullUrl += itNames.hasNext() ? "&" : "";
                }
            }
            return new SimpleResult( status ).withHeader( HttpHeaders.Names.LOCATION, fullUrl );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new RuntimeException( "UTF-8 Charset is not supported oO", ex );
        }
    }

    private static abstract class AbstractResult<ResultType extends AbstractResult<?>>
        implements Result
    {

        private final int status;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();

        private AbstractResult( int status )
        {
            this.status = status;
        }

        @Override
        public final int status()
        {
            return status;
        }

        @Override
        public final Map<String, List<String>> headers()
        {
            return Collections.unmodifiableMap( headers );
        }

        public final ResultType withHeader( String name, String value )
        {
            if( headers.get( name ) == null )
            {
                headers.put( name, new ArrayList<String>() );
            }
            headers.get( name ).add( value );
            return (ResultType) this;
        }

        public final ResultType as( String contentType )
        {
            if( headers.containsKey( CONTENT_TYPE ) )
            {
                headers.get( CONTENT_TYPE ).clear();
            }
            else
            {
                headers.put( CONTENT_TYPE, new ArrayList<String>() );
            }
            headers.get( CONTENT_TYPE ).add( contentType );
            return (ResultType) this;
        }

        @Override
        public String toString()
        {
            return status + ", " + headers;
        }
    }

    public static class SimpleResult
        extends AbstractResult<SimpleResult>
    {

        private ByteBuf body = EMPTY_BUFFER;

        public SimpleResult( int status )
        {
            super( status );
        }

        private SimpleResult( HttpResponseStatus status )
        {
            super( status.code() );
        }

        public ByteBuf body()
        {
            return body;
        }

        public final SimpleResult withoutBody()
        {
            body = EMPTY_BUFFER;
            return this;
        }

        public final SimpleResult withBody( ByteBuf body )
        {
            this.body = body;
            return this;
        }

        public final SimpleResult withBody( String body )
        {
            this.body = copiedBuffer( body, UTF_8 );
            return this;
        }
    }

    public static abstract class StreamResult
        extends AbstractResult<StreamResult>
    {

        private final InputStream input;
        private final int contentLength;

        public StreamResult( int status, InputStream input, int contentLength )
        {
            super( status );
            this.input = input;
            this.contentLength = contentLength;
            withHeader( CONTENT_LENGTH, String.valueOf( contentLength ) );
        }

        private StreamResult( HttpResponseStatus status, InputStream input, int contentLength )
        {
            this( status.code(), input, contentLength );
        }

        public final InputStream input()
        {
            return input;
        }

        public final int contentLength()
        {
            return contentLength;
        }
    }

    public static abstract class ChunkedResult
        extends AbstractResult<ChunkedResult>
    {

        public ChunkedResult( int status )
        {
            super( status );
        }

        private ChunkedResult( HttpResponseStatus status )
        {
            super( status.code() );
        }
    }
}
