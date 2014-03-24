/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.runtime.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.qiweb.api.Config;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.FormUploads;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.Request;
import org.qiweb.api.util.ByteSource;
import org.qiweb.api.util.Strings;
import org.qiweb.api.util.URLs;
import org.qiweb.spi.http.HttpBuilders;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotNull;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.X_HTTP_METHOD_OVERRIDE;
import static org.qiweb.api.http.ProtocolVersion.HTTP_1_1;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED;
import static org.qiweb.runtime.http.RequestHeaderInstance.extractCharset;

/**
 * HTTP API Objects Builders Instance.
 */
public class HttpBuildersInstance
    implements HttpBuilders
{
    private final Config config;
    private final Charset defaultCharset;

    public HttpBuildersInstance( Config config, Charset defaultCharset )
    {
        this.config = config;
        this.defaultCharset = defaultCharset;
    }

    @Override
    public RequestBuilder newRequestBuilder()
    {
        return new RequestBuilderInstance(
            config, defaultCharset, null, null, null, null, null, null, null, null, null, null
        );
    }

    private static class RequestBuilderInstance
        implements RequestBuilder
    {
        private final Config config;
        private final Charset defaultCharset;
        private final String identity;
        private final String remoteSocketAddress;
        private final ProtocolVersion version;
        private final String method;
        private final String uri;
        private final Headers headers;
        private final Cookies cookies;
        private final ByteSource bodyBytes;
        private final Map<String, List<String>> attributes;
        private final Map<String, List<FormUploads.Upload>> uploads;

        private RequestBuilderInstance(
            Config config, Charset defaultCharset,
            String identity, String remoteSocketAddress,
            ProtocolVersion version, String method, String uri,
            Headers headers, Cookies cookies,
            ByteSource bodyBytes,
            Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads
        )
        {
            this.config = config;
            this.defaultCharset = defaultCharset;
            this.identity = Strings.isEmpty( identity ) ? "NO_REQUEST_ID" : identity;
            this.remoteSocketAddress = remoteSocketAddress;
            this.version = version == null ? HTTP_1_1 : version;
            this.method = method;
            this.uri = uri;
            this.headers = headers == null ? HeadersInstance.EMPTY : headers;
            this.cookies = cookies == null ? CookiesInstance.EMPTY : cookies;
            this.bodyBytes = bodyBytes;
            this.attributes = attributes;
            this.uploads = uploads;
        }

        @Override
        public RequestBuilder identifiedBy( String identity )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder remoteSocketAddress( String remoteSocketAddress )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder version( ProtocolVersion version )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder method( String method )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder uri( String uri )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder headers( Headers headers )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder headers( Map<String, List<String>> headers )
        {
            return headers( new HeadersInstance( headers ) );
        }

        @Override
        public RequestBuilder cookies( Cookies cookies )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder bodyBytes( ByteSource bodyBytes )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder bodyForm( Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads )
        {
            return new RequestBuilderInstance(
                config, defaultCharset,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public Request build()
        {
            ensureNotEmpty( "identity", identity );
            ensureNotNull( "version", version );
            ensureNotEmpty( "method", method );
            ensureNotEmpty( "uri", uri );
            ensureNotNull( "headers", headers );
            ensureNotNull( "cookies", cookies );

            // Parse Path & QueryString from URI
            QueryString.Decoder decoder = new QueryString.Decoder( uri, defaultCharset );
            String path = URLs.decode( decoder.path(), defaultCharset );
            QueryString queryString = new QueryStringInstance( decoder.parameters() );

            // Request charset
            Charset requestCharset = headers.has( CONTENT_TYPE )
                                     ? Charset.forName( extractCharset( headers.singleValue( CONTENT_TYPE ) ) )
                                     : defaultCharset;

            // Build Request
            return new RequestInstance(
                // Request Header
                new RequestHeaderInstance(
                    // Identity
                    identity,
                    // Remote Address can be null
                    remoteSocketAddress,
                    // X-Forwarded-For configuration
                    config.bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED ),
                    config.bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK ),
                    config.stringList( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED ),
                    version,
                    // HTTP Method Override
                    headers.has( X_HTTP_METHOD_OVERRIDE ) ? headers.singleValue( X_HTTP_METHOD_OVERRIDE ) : method,
                    // Path & QueryString parsed from URI
                    uri, path, queryString,
                    // Headers & Cookies
                    headers, cookies
                ),
                // Request Body
                ( attributes != null || uploads != null )
                ? new RequestBodyInstance( requestCharset, attributes, uploads )
                : new RequestBodyInstance( requestCharset, bodyBytes )
            );
        }
    }
}
