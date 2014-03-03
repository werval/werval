/**
 * Copyright (c) 2013-2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.server.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultCookie;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Cookies.Cookie;
import org.qiweb.api.http.FormUploads.Upload;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.util.Strings;
import org.qiweb.api.util.URLs;
import org.qiweb.runtime.http.CookiesInstance;
import org.qiweb.runtime.http.CookiesInstance.CookieInstance;
import org.qiweb.runtime.http.FormUploadsInstance.UploadInstance;
import org.qiweb.runtime.http.HeadersInstance;
import org.qiweb.runtime.http.QueryStringInstance;
import org.qiweb.runtime.http.RequestBodyInstance;
import org.qiweb.runtime.http.RequestHeaderInstance;
import org.qiweb.runtime.util.ByteSource;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static io.netty.handler.codec.http.HttpHeaders.Values.MULTIPART_FORM_DATA;
import static io.netty.handler.codec.http.HttpMethod.PATCH;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.COOKIE;
import static org.qiweb.api.http.Headers.Names.X_HTTP_METHOD_OVERRIDE;
import static org.qiweb.runtime.http.RequestHeaderInstance.extractCharset;

/**
 * Factory methods used by the server.
 */
/* package */ final class NettyHttpFactories
{
    private static MutableHeaders headersOf( HttpRequest request, boolean allowMultiValuedHeaders )
    {
        MutableHeaders headers = new HeadersInstance( allowMultiValuedHeaders );
        for( String name : request.headers().names() )
        {
            for( String value : request.headers().getAll( name ) )
            {
                headers.with( name, value );
            }
        }
        return headers;
    }

    // TODO What to do about multiple Cookie headers?
    private static Cookies cookiesOf( HttpRequest nettyRequest )
    {
        Map<String, Cookie> cookies = new HashMap<>();
        String cookieHeaderValue = nettyRequest.headers().get( COOKIE );
        if( !Strings.isEmpty( cookieHeaderValue ) )
        {
            Set<io.netty.handler.codec.http.Cookie> nettyCookies = CookieDecoder.decode( cookieHeaderValue );
            for( io.netty.handler.codec.http.Cookie nettyCookie : nettyCookies )
            {
                cookies.put( nettyCookie.getName(), asQiWebCookie( nettyCookie ) );
            }
        }
        return new CookiesInstance( cookies );
    }

    /* package */ static String remoteAddressOf( Channel channel )
    {
        SocketAddress remoteAddress = channel.remoteAddress();
        if( remoteAddress == null || !( remoteAddress instanceof InetSocketAddress ) )
        {
            return null;
        }
        InetAddress inetRemoteAddress = ( (InetSocketAddress) remoteAddress ).getAddress();
        if( inetRemoteAddress == null )
        {
            return null;
        }
        return inetRemoteAddress.getHostAddress();
    }

    /* package */ static RequestHeader requestHeaderOf(
        String identity, HttpRequest request,
        String remoteSocketAddress,
        boolean xffEnabled, boolean xffCheckProxies, List<String> xffTrustedProxies,
        Charset defaultCharset,
        boolean allowMultiValuedQueryStringParameters, boolean allowMultiValuedHeaders )
    {
        ensureNotEmpty( "Request Identity", identity );
        ensureNotNull( "Netty HttpRequest", request );

        // Method
        String method = request.getMethod().name();
        if( request.headers().get( X_HTTP_METHOD_OVERRIDE ) != null )
        {
            method = request.headers().get( X_HTTP_METHOD_OVERRIDE );
        }

        // Path and QueryString
        String requestCharset = extractCharset( request.headers().get( CONTENT_TYPE ) );
        Charset charset = Strings.isEmpty( requestCharset ) ? defaultCharset : Charset.forName( requestCharset );
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder( request.getUri(), charset );
        String requestPath = URLs.decode( queryStringDecoder.path(), charset );
        QueryString queryString = new QueryStringInstance(
            allowMultiValuedQueryStringParameters,
            queryStringDecoder.parameters()
        );

        // Headers
        Headers headers = headersOf( request, allowMultiValuedHeaders );

        // Cookies
        Cookies cookies = cookiesOf( request );

        return new RequestHeaderInstance(
            identity, remoteSocketAddress,
            xffEnabled, xffCheckProxies, xffTrustedProxies,
            request.getProtocolVersion().text(),
            method,
            request.getUri(),
            requestPath,
            queryString,
            headers,
            cookies
        );
    }

    /* package */ static RequestBody bodyOf(
        RequestHeader requestHeader, FullHttpRequest request,
        Charset defaultCharset,
        boolean allowMultiValuedHeaders, boolean allowMultiValuedFormAttributes, boolean allowMultiValuedUploads )
    {
        RequestBody body;
        Charset requestCharset = requestHeader.charset().isEmpty()
                                 ? defaultCharset
                                 : Charset.forName( requestHeader.charset() );
        if( request.content().readableBytes() > 0
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
                                        attributes.put( attribute.getName(), new ArrayList<>() );
                                    }
                                    attributes.get( attribute.getName() ).add( attribute.getValue() );
                                    break;
                                case FileUpload:
                                    FileUpload fileUpload = (FileUpload) data;
                                    if( !uploads.containsKey( fileUpload.getName() ) )
                                    {
                                        uploads.put( fileUpload.getName(), new ArrayList<>() );
                                    }
                                    Upload upload = new UploadInstance(
                                        fileUpload.getContentType(),
                                        fileUpload.getCharset(),
                                        fileUpload.getFilename(),
                                        fileUpload.getFile(),
                                        defaultCharset
                                    );
                                    uploads.get( fileUpload.getName() ).add( upload );
                                    break;
                                default:
                                    break;
                            }
                        }
                        body = new RequestBodyInstance(
                            requestCharset,
                            allowMultiValuedFormAttributes, allowMultiValuedUploads,
                            attributes, uploads
                        );
                        break;
                    }
                    catch( ErrorDataDecoderException | IncompatibleDataDecoderException |
                           NotEnoughDataDecoderException | IOException ex )
                    {
                        throw new QiWebException( ex.getMessage(), ex );
                    }
                default:
                    ByteSource bodyBytes = new ByteBufByteSource( request.content() );
                    body = new RequestBodyInstance( requestCharset, allowMultiValuedFormAttributes, bodyBytes );
                    break;
            }
        }
        else
        {
            body = new RequestBodyInstance( requestCharset, allowMultiValuedFormAttributes, allowMultiValuedUploads );
        }
        return body;
    }

    /* package */ static io.netty.handler.codec.http.Cookie asNettyCookie( Cookie cookie )
    {
        io.netty.handler.codec.http.Cookie nettyCookie = new DefaultCookie( cookie.name(), cookie.value() );
        nettyCookie.setPath( cookie.path() );
        nettyCookie.setDomain( cookie.domain() );
        nettyCookie.setSecure( cookie.secure() );
        nettyCookie.setHttpOnly( cookie.httpOnly() );
        return nettyCookie;
    }

    /* package */ static Cookie asQiWebCookie( io.netty.handler.codec.http.Cookie nettyCookie )
    {
        return new CookieInstance( nettyCookie.getName(),
                                   nettyCookie.getPath(),
                                   nettyCookie.getDomain(),
                                   nettyCookie.isSecure(),
                                   nettyCookie.getValue(),
                                   nettyCookie.isHttpOnly() );
    }

    private NettyHttpFactories()
    {
    }
}
