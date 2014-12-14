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
package io.werval.runtime.http;

import io.werval.api.Config;
import io.werval.api.http.Cookies;
import io.werval.api.http.Cookies.Cookie;
import io.werval.api.http.FormUploads;
import io.werval.api.http.Headers;
import io.werval.api.http.Method;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.QueryString;
import io.werval.api.http.Request;
import io.werval.api.i18n.Langs;
import io.werval.spi.http.HttpBuildersSPI;
import io.werval.util.ByteSource;
import io.werval.util.Strings;
import io.werval.util.URLs;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.http.Headers.Names.COOKIE;
import static io.werval.api.http.Headers.Names.X_HTTP_METHOD_OVERRIDE;
import static io.werval.api.http.Method.CONNECT;
import static io.werval.api.http.Method.DELETE;
import static io.werval.api.http.Method.GET;
import static io.werval.api.http.Method.HEAD;
import static io.werval.api.http.Method.OPTIONS;
import static io.werval.api.http.Method.PATCH;
import static io.werval.api.http.Method.POST;
import static io.werval.api.http.Method.PUT;
import static io.werval.api.http.Method.TRACE;
import static io.werval.api.http.ProtocolVersion.HTTP_1_1;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_CHECK;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED;
import static io.werval.runtime.http.RequestHeaderInstance.extractCharset;
import static io.werval.util.IllegalArguments.ensureInRange;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * HTTP API Objects Builders Instance.
 */
public class HttpBuildersInstance
    implements HttpBuildersSPI
{
    private final Config config;
    private final Charset defaultCharset;
    private final Langs langs;

    /**
     * Create a new HttpBuilders instance.
     *
     * @param config         Configuration
     * @param defaultCharset Application default charset
     * @param langs          Applicaiton Langs
     */
    public HttpBuildersInstance( Config config, Charset defaultCharset, Langs langs )
    {
        this.config = config;
        this.defaultCharset = defaultCharset;
        this.langs = langs;
    }

    @Override
    public RequestBuilder newRequestBuilder()
    {
        return new RequestBuilderInstance(
            config, defaultCharset, langs, null, null, null, null, null, null, null, null, null, null
        );
    }

    private static final class RequestBuilderInstance
        implements RequestBuilder
    {
        private final Config config;
        private final Charset defaultCharset;
        private final Langs langs;
        private final String identity;
        private final String remoteSocketAddress;
        private final ProtocolVersion version;
        private final Method method;
        private final String uri;
        private final Headers headers;
        private final Cookies cookies;
        private final ByteSource bodyBytes;
        private final Map<String, List<String>> attributes;
        private final Map<String, List<FormUploads.Upload>> uploads;

        private RequestBuilderInstance(
            Config config, Charset defaultCharset, Langs langs,
            String identity, String remoteSocketAddress,
            ProtocolVersion version, Method method, String uri,
            Headers headers, Cookies cookies,
            ByteSource bodyBytes,
            Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads
        )
        {
            this.config = config;
            this.defaultCharset = defaultCharset;
            this.langs = langs;
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
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder remoteSocketAddress( String remoteSocketAddress )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder version( ProtocolVersion version )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder method( String method )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, Method.valueOf( method ), uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder method( Method method )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder uri( String uri )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder get( String uri )
        {
            return method( GET ).uri( uri );
        }

        @Override
        public RequestBuilder head( String uri )
        {
            return method( HEAD ).uri( uri );
        }

        @Override
        public RequestBuilder options( String uri )
        {
            return method( OPTIONS ).uri( uri );
        }

        @Override
        public RequestBuilder trace( String uri )
        {
            return method( TRACE ).uri( uri );
        }

        @Override
        public RequestBuilder connect( String uri )
        {
            return method( CONNECT ).uri( uri );
        }

        @Override
        public RequestBuilder put( String uri )
        {
            return method( PUT ).uri( uri );
        }

        @Override
        public RequestBuilder post( String uri )
        {
            return method( POST ).uri( uri );
        }

        @Override
        public RequestBuilder patch( String uri )
        {
            return method( PATCH ).uri( uri );
        }

        @Override
        public RequestBuilder delete( String uri )
        {
            return method( DELETE ).uri( uri );
        }

        @Override
        public RequestBuilder headers( Headers headers )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
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
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder bodyBytes( ByteSource bodyBytes )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
                identity, remoteSocketAddress, version, method, uri,
                headers, cookies,
                bodyBytes, attributes, uploads
            );
        }

        @Override
        public RequestBuilder bodyForm(
            Map<String, List<String>> attributes,
            Map<String, List<FormUploads.Upload>> uploads
        )
        {
            return new RequestBuilderInstance(
                config, defaultCharset, langs,
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
            ensureNotNull( "method", method );
            ensureNotEmpty( "uri", uri );
            ensureNotNull( "headers", headers );
            ensureNotNull( "cookies", cookies );

            // Parse Path & QueryString from URI
            QueryString.Decoder decoder = new QueryString.Decoder( uri, defaultCharset );
            String path = URLs.decode( decoder.path(), defaultCharset );
            QueryString queryString = new QueryStringInstance( decoder.parameters() );

            // Request charset
            Charset requestCharset = null;
            if( headers.has( CONTENT_TYPE ) )
            {
                String extractedCharset = extractCharset( headers.singleValue( CONTENT_TYPE ) );
                if( Strings.hasText( extractedCharset ) )
                {
                    requestCharset = Charset.forName( extractedCharset );
                }
            }
            if( requestCharset == null )
            {
                requestCharset = defaultCharset;
            }

            // Parse Cookies from Headers
            Map<String, Cookie> allCookies = new HashMap<>();
            if( headers.has( COOKIE ) )
            {
                List<String> cookieHeaders = headers.values( COOKIE );
                for( String cookieHeader : cookieHeaders )
                {
                    for( String splitCookieHeader : splitMultiCookies( cookieHeader ) )
                    {
                        for( HttpCookie jCookie : HttpCookie.parse( splitCookieHeader ) )
                        {
                            allCookies.put(
                                jCookie.getName(),
                                new CookiesInstance.CookieInstance(
                                    jCookie.getVersion(),
                                    jCookie.getName(),
                                    jCookie.getValue(),
                                    jCookie.getPath(),
                                    jCookie.getDomain(),
                                    jCookie.getMaxAge(),
                                    jCookie.getSecure(),
                                    jCookie.isHttpOnly(),
                                    jCookie.getComment(),
                                    jCookie.getCommentURL()
                                )
                            );
                        }
                    }
                }
            }

            // Override with cookies given through API
            for( Cookie cookie : cookies )
            {
                allCookies.put( cookie.name(), cookie );
            }

            // Build Request
            return new RequestInstance(
                // Request Header
                new RequestHeaderInstance(
                    // Langs
                    langs,
                    // Identity
                    identity,
                    // Remote Address can be null
                    remoteSocketAddress,
                    // X-Forwarded-For configuration
                    config.bool( WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED ),
                    config.bool( WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_CHECK ),
                    config.stringList( WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED ),
                    version,
                    // HTTP Method Override
                    headers.has( X_HTTP_METHOD_OVERRIDE )
                    ? Method.valueOf( headers.singleValue( X_HTTP_METHOD_OVERRIDE ) )
                    : method,
                    // Path & QueryString parsed from URI
                    uri, path, queryString,
                    // Headers
                    headers,
                    // Cookies
                    new CookiesInstance( allCookies )
                ),
                // Request Body
                ( attributes != null || uploads != null )
                ? new RequestBodyInstance( requestCharset, attributes, uploads )
                : new RequestBodyInstance( requestCharset, bodyBytes )
            );
        }
    }

    @Override
    public CookieBuilder newCookieBuilder()
    {
        return new CookieBuilderInstance( null, null, null, null, null, null, null, null, null, null );
    }

    private static final class CookieBuilderInstance
        implements CookieBuilder
    {
        private final int version;
        private final String name;
        private final String value;
        private final String path;
        private final String domain;
        private final long maxAge;
        private final boolean secure;
        private final boolean httpOnly;
        private final String comment;
        private final String commentUrl;

        private CookieBuilderInstance(
            Integer version,
            String name, String value,
            String path, String domain,
            Long maxAge,
            Boolean secure, Boolean httpOnly,
            String comment, String commentUrl )
        {
            this.version = version == null ? 0 : version;
            this.name = name;
            this.value = value == null ? Strings.EMPTY : value;
            this.path = path == null ? "/" : path;
            this.domain = domain;
            this.maxAge = maxAge == null ? Long.MIN_VALUE : maxAge;
            this.secure = secure == null ? false : secure;
            this.httpOnly = httpOnly == null ? true : httpOnly;
            this.comment = comment;
            this.commentUrl = commentUrl;
        }

        @Override
        public CookieBuilder version( int version )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder name( String name )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder value( String value )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder path( String path )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder domain( String domain )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder maxAge( long maxAge )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder secure( boolean secure )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder httpOnly( boolean httpOnly )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder comment( String comment )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public CookieBuilder commentUrl( String commentUrl )
        {
            return new CookieBuilderInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }

        @Override
        public Cookie build()
        {
            ensureNotEmpty( "name", name );
            ensureInRange( "version", version, 0, 1 );
            return new CookiesInstance.CookieInstance(
                version, name, value, path, domain, maxAge, secure, httpOnly, comment, commentUrl
            );
        }
    }

    private static List<String> splitMultiCookies( String header )
    {
        List<String> cookies = new java.util.ArrayList<>();
        int quoteCount = 0;
        int p, q;
        for( p = 0, q = 0; p < header.length(); )
        {
            int c = header.codePointAt( p );
            if( c == '"' )
            {
                quoteCount++;
            }
            if( c == ';' && ( quoteCount % 2 == 0 ) )
            {
                // it is ; and not surrounded by double-quotes
                cookies.add( header.substring( q, p ) );
                q = p + Character.charCount( c );
            }
            p += Character.charCount( c );
        }
        cookies.add( header.substring( q ) );
        return cookies;
    }
}
