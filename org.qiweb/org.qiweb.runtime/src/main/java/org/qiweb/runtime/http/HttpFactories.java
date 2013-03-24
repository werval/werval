package org.qiweb.runtime.http;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestHeader;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.util.CharsetUtil.UTF_8;

public class HttpFactories
{

    public static MutableHeaders headersOf( HttpRequest request )
    {
        MutableHeaders headers = new HeadersInstance();
        for( String name : request.headers().names() )
        {
            for( String value : request.headers().getAll( name ) )
            {
                headers.with( name, value );
            }
        }
        return headers;
    }

    public static RequestHeader requestHeaderOf( String identity, Headers headers, HttpRequest request )
    {
        NullArgumentException.ensureNotEmpty( "Request Identity", identity );
        NullArgumentException.ensureNotNull( "Request Headers", headers );
        NullArgumentException.ensureNotNull( "Netty HttpRequest", request );

        // Method
        String method = request.getMethod().name();
        if( request.headers().get( "X-HTTP-Method-Override" ) != null )
        {
            method = request.headers().get( "X-HTTP-Method-Override" );
        }
        // Path and QueryString
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder( request.getUri(), UTF_8 );
        String requestPath = queryStringDecoder.path();
        QueryString queryString = new QueryStringInstance( queryStringDecoder.parameters() );

        // Does the request come with a body? - RFC2616 Sections 4.3 and 4.4
        boolean hasBody = ( request.headers().get( CONTENT_LENGTH ) != null
                            || request.headers().get( TRANSFER_ENCODING ) != null
                            || request.headers().getAll( CONTENT_TYPE ).contains( "multipart/byteranges" ) );

        return new RequestHeaderInstance( identity,
                                          request.getProtocolVersion().text(),
                                          method,
                                          request.getUri(),
                                          requestPath,
                                          queryString,
                                          headers,
                                          hasBody );
    }

    private HttpFactories()
    {
    }
}
