package org.qiweb.runtime.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.NotEnoughDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestBody.Upload;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Session;
import org.qiweb.runtime.controllers.ContextInstance;
import org.qiweb.runtime.http.RequestBodyInstance.UploadInstance;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static io.netty.handler.codec.http.HttpHeaders.Values.MULTIPART_FORM_DATA;
import static io.netty.handler.codec.http.HttpMethod.PATCH;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
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

    public static RequestHeader requestHeaderOf( String identity, HttpRequest request )
    {
        NullArgumentException.ensureNotEmpty( "Request Identity", identity );
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

        // Headers
        Headers headers = headersOf( request );

        return new RequestHeaderInstance( identity,
                                          request.getProtocolVersion().text(),
                                          method,
                                          request.getUri(),
                                          requestPath,
                                          queryString,
                                          headers );
    }

    public static RequestBody bodyOf( RequestHeader requestHeader, FullHttpRequest request )
    {
        RequestBody body;
        if( request.data().readableBytes() > 0
            && ( POST.equals( request.getMethod() )
                 || PUT.equals( request.getMethod() )
                 || PATCH.equals( request.getMethod() ) ) )
        {
            switch( requestHeader.contentType() )
            {
                case APPLICATION_X_WWW_FORM_URLENCODED:
                case MULTIPART_FORM_DATA:
                    try
                    {
                        Map<String, List<String>> attributes = new LinkedHashMap<>();
                        Map<String, List<Upload>> uploads = new LinkedHashMap<>();
                        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder( request );
                        for( InterfaceHttpData data : postDecoder.getBodyHttpDatas() )
                        {
                            switch( data.getHttpDataType() )
                            {
                                case Attribute:
                                    Attribute attribute = (Attribute) data;
                                    if( !attributes.containsKey( attribute.getName() ) )
                                    {
                                        attributes.put( attribute.getName(), new ArrayList<String>() );
                                    }
                                    attributes.get( attribute.getName() ).add( attribute.getValue() );
                                    break;
                                case FileUpload:
                                    FileUpload fileUpload = (FileUpload) data;
                                    if( !uploads.containsKey( fileUpload.getName() ) )
                                    {
                                        uploads.put( fileUpload.getName(), new ArrayList<Upload>() );
                                    }
                                    Upload upload = new UploadInstance( fileUpload.getContentType(),
                                                                        fileUpload.getCharset(),
                                                                        fileUpload.getFilename(),
                                                                        fileUpload.getFile() );
                                    uploads.get( fileUpload.getName() ).add( upload );
                                    break;
                            }
                        }
                        body = new RequestBodyInstance( attributes, uploads );
                        break;
                    }
                    catch( ErrorDataDecoderException | IncompatibleDataDecoderException |
                           NotEnoughDataDecoderException | IOException ex )
                    {
                        throw new RuntimeException( ex.getMessage(), ex );
                    }
                default:
                    body = new RequestBodyInstance( request.data() );
                    break;
            }
        }
        else
        {
            body = new RequestBodyInstance(); // Empty
        }
        return body;
    }

    public static Request requestOf( RequestHeader header, RequestBody body )
    {
        return new RequestInstance( header, body );
    }

    public static Context contextOf( Request request, Flash flash, Session session )
    {
        return new ContextInstance( request, flash, session );
    }

    private HttpFactories()
    {
    }
}
