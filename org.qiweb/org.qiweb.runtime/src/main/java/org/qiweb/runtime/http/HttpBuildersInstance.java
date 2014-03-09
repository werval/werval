/**
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
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.util.ByteSource;
import org.qiweb.spi.http.HttpBuilders;

import static java.util.Collections.emptyMap;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_FORMS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_QUERYSTRING_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_UPLOADS_MULTIVALUED;

/**
 * HTTP API Objects Builders Instance.
 */
public class HttpBuildersInstance
    implements HttpBuilders
{
    private final Config config;

    public HttpBuildersInstance( Config config )
    {
        this.config = config;
    }

    @Override
    public QueryStringBuilder newQueryStringBuilder()
    {
        return new QueryStringBuilderInstance( config, null );
    }

    private static class QueryStringBuilderInstance
        implements QueryStringBuilder
    {
        private final Config config;
        private final Map<String, List<String>> parameters;

        private QueryStringBuilderInstance( Config config, Map<String, List<String>> parameters )
        {
            this.config = config;
            this.parameters = parameters == null ? emptyMap() : parameters;
        }

        @Override
        public QueryStringBuilder parameters( Map<String, List<String>> parameters )
        {
            return new QueryStringBuilderInstance( config, parameters );
        }

        @Override
        public QueryString build()
        {
            return new QueryStringInstance(
                config.bool( QIWEB_HTTP_QUERYSTRING_MULTIVALUED ),
                parameters
            );
        }
    }

    @Override
    public HeadersBuilder newHeadersBuilder()
    {
        return new HeadersBuilderInstance( config, null );
    }

    private static class HeadersBuilderInstance
        implements HeadersBuilder
    {
        private final Config config;
        private final Map<String, List<String>> headers;

        private HeadersBuilderInstance( Config config, Map<String, List<String>> headers )
        {
            this.config = config;
            this.headers = headers == null ? emptyMap() : headers;
        }

        @Override
        public HeadersBuilder headers( Map<String, List<String>> headers )
        {
            return new HeadersBuilderInstance( config, headers );
        }

        @Override
        public Headers build()
        {
            return new HeadersInstance(
                config.bool( QIWEB_HTTP_HEADERS_MULTIVALUED ),
                headers
            );
        }
    }

    @Override
    public RequestHeaderBuilder newRequestHeaderBuilder()
    {
        return new RequestHeaderBuilderInstance( config, null, null, null, null, null, null, null, null, null );
    }

    private static class RequestHeaderBuilderInstance
        implements RequestHeaderBuilder
    {
        private final Config config;
        private final String identity;
        private final String remoteSocketAddress;
        private final ProtocolVersion version;
        private final String method;
        private final String uri;
        private final String path;
        private final QueryString queryString;
        private final Headers headers;
        private final Cookies cookies;

        private RequestHeaderBuilderInstance(
            Config config, String identity, String remoteSocketAddress,
            ProtocolVersion version, String method, String uri, String path,
            QueryString queryString, Headers headers, Cookies cookies
        )
        {
            this.config = config;
            this.identity = identity;
            this.remoteSocketAddress = remoteSocketAddress;
            this.version = version;
            this.method = method;
            this.uri = uri;
            this.path = path;
            this.queryString = queryString;
            this.headers = headers;
            this.cookies = cookies;
        }

        @Override
        public RequestHeaderBuilder identifiedBy( String identity )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder remoteSocketAddress( String remoteSocketAddress )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder version( ProtocolVersion version )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder method( String method )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder uri( String uri )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder path( String path )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder queryString( QueryString queryString )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder headers( Headers headers )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeaderBuilder cookies( Cookies cookies )
        {
            return new RequestHeaderBuilderInstance(
                config, identity, remoteSocketAddress, version, method, uri, path, queryString, headers, cookies
            );
        }

        @Override
        public RequestHeader build()
        {
            ensureNotEmpty( "identity", identity );
            ensureNotEmpty( "remoteSocketAddress", remoteSocketAddress );
            ensureNotNull( "version", version );
            ensureNotEmpty( "method", method );
            ensureNotEmpty( "uri", uri );
            ensureNotEmpty( "path", path );
            ensureNotNull( "queryString", queryString );
            ensureNotNull( "headers", headers );
            ensureNotNull( "cookies", cookies );
            return new RequestHeaderInstance(
                identity, remoteSocketAddress,
                config.bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED ),
                config.bool( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_CHECK ),
                config.stringList( QIWEB_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED ),
                version, method, uri, path, queryString, headers, cookies
            );
        }
    }

    @Override
    public RequestBodyBuilder newRequestBodyBuilder()
    {
        return new RequestBodyBuilderInstance( config, null, null, null, null );
    }

    private static class RequestBodyBuilderInstance
        implements RequestBodyBuilder
    {
        private final Config config;
        private final Charset charset;
        private final ByteSource bodyBytes;
        private final Map<String, List<String>> attributes;
        private final Map<String, List<FormUploads.Upload>> uploads;

        private RequestBodyBuilderInstance(
            Config config,
            Charset charset, ByteSource bodyBytes,
            Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads
        )
        {
            this.config = config;
            this.charset = charset;
            this.bodyBytes = bodyBytes;
            this.attributes = attributes;
            this.uploads = uploads;
        }

        @Override
        public RequestBodyBuilder charset( Charset charset )
        {
            return new RequestBodyBuilderInstance( config, charset, bodyBytes, attributes, uploads );
        }

        @Override
        public RequestBodyBuilder bytes( ByteSource bodyBytes )
        {
            return new RequestBodyBuilderInstance( config, charset, bodyBytes, attributes, uploads );
        }

        @Override
        public RequestBodyBuilder form( Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads )
        {
            return new RequestBodyBuilderInstance( config, charset, bodyBytes, attributes, uploads );
        }

        @Override
        public RequestBody build()
        {
            ensureNotNull( "charset", charset );
            if( attributes != null || uploads != null )
            {
                // Form
                return new RequestBodyInstance(
                    charset,
                    config.bool( QIWEB_HTTP_FORMS_MULTIVALUED ),
                    config.bool( QIWEB_HTTP_UPLOADS_MULTIVALUED ),
                    attributes,
                    uploads
                );
            }
            // Bytes
            return new RequestBodyInstance(
                charset,
                bodyBytes
            );
        }
    }
}
