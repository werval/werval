/*
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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.NotEnoughDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.werval.api.exceptions.WervalException;
import io.werval.api.http.FormUploads.Upload;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.Request;
import io.werval.spi.http.HttpBuildersSPI;
import io.werval.spi.http.HttpBuildersSPI.RequestBuilder;
import io.werval.runtime.http.FormUploadsInstance.UploadInstance;
import io.werval.util.ByteSource;
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

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static io.netty.handler.codec.http.HttpHeaders.Values.MULTIPART_FORM_DATA;
import static io.netty.handler.codec.http.HttpMethod.PATCH;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.runtime.http.RequestHeaderInstance.extractContentType;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * Factory methods used by the server.
 */
/* package */ final class NettyHttpFactories
{
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

    /* package */ static Request requestOf(
        Charset defaultCharset,
        HttpBuildersSPI builders,
        String remoteSocketAddress, String identity,
        FullHttpRequest request
    )
    {
        ensureNotEmpty( "Request Identity", identity );
        ensureNotNull( "Netty FullHttpRequest", request );

        RequestBuilder builder = builders.newRequestBuilder()
            .identifiedBy( identity )
            .remoteSocketAddress( remoteSocketAddress )
            .version( ProtocolVersion.valueOf( request.getProtocolVersion().text() ) )
            .method( request.getMethod().name() )
            .uri( request.getUri() )
            .headers( headersToMap( request.headers() ) );

        if( request.content().readableBytes() > 0
            && ( POST.equals( request.getMethod() )
                 || PUT.equals( request.getMethod() )
                 || PATCH.equals( request.getMethod() ) ) )
        {
            switch( extractContentType( request.headers().get( CONTENT_TYPE ) ) )
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
                                    Upload upload;
                                    if( fileUpload.isInMemory() )
                                    {
                                        upload = new UploadInstance(
                                            fileUpload.getContentType(),
                                            fileUpload.getCharset(),
                                            fileUpload.getFilename(),
                                            new ByteBufByteSource( fileUpload.getByteBuf() ).asBytes(),
                                            defaultCharset
                                        );
                                    }
                                    else
                                    {
                                        upload = new UploadInstance(
                                            fileUpload.getContentType(),
                                            fileUpload.getCharset(),
                                            fileUpload.getFilename(),
                                            fileUpload.getFile(),
                                            defaultCharset
                                        );
                                    }
                                    uploads.get( fileUpload.getName() ).add( upload );
                                    break;
                                default:
                                    break;
                            }
                        }
                        builder = builder.bodyForm( attributes, uploads );
                        break;
                    }
                    catch( ErrorDataDecoderException | IncompatibleDataDecoderException |
                           NotEnoughDataDecoderException | IOException ex )
                    {
                        throw new WervalException( "Form or multipart parsing error", ex );
                    }
                default:
                    ByteSource bodyBytes = new ByteBufByteSource( request.content() );
                    builder = builder.bodyBytes( bodyBytes );
                    break;
            }
        }
        return builder.build();
    }

    private static Map<String, List<String>> headersToMap( HttpHeaders nettyHeaders )
    {
        Map<String, List<String>> headers = new HashMap<>();
        for( String name : nettyHeaders.names() )
        {
            if( !headers.containsKey( name ) )
            {
                headers.put( name, new ArrayList<>() );
            }
            for( String value : nettyHeaders.getAll( name ) )
            {
                headers.get( name ).add( value );
            }
        }
        return headers;
    }

    private NettyHttpFactories()
    {
    }
}
