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
package io.werval.api.http;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HTTP Headers.
 *
 * @has 1 - 1 Names
 * @has 1 - 1 Values
 */
public interface Headers
{
    /**
     * @return TRUE if there's no header, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the header
     *
     * @return TRUE if there's a header with the given name
     */
    boolean has( String name );

    /**
     * @return All HTTP Header names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * Get single header value, ensuring it has only one value.
     *
     * @param name Name of the HTTP Header
     *
     * @return Value for this HTTP Header name or an empty String
     *
     * @throws IllegalStateException if there is multiple values for this header
     */
    String singleValue( String name );

    /**
     * Get first header value.
     *
     * @param name Name of the HTTP Header
     *
     * @return First value for this HTTP Header name or an empty String
     */
    String firstValue( String name );

    /**
     * Get last header value.
     *
     * @param name Name of the HTTP Header
     *
     * @return Last value for this HTTP Header name or an empty String
     */
    String lastValue( String name );

    /**
     * Get all header values.
     *
     * @param name Name of the HTTP Header
     *
     * @return All first values for this HTTP Header name as immutable List&lt;String&gt;, or an empty immutable one.
     */
    List<String> values( String name );

    /**
     * Get all headers single values, ensuring each has only one value.
     *
     * @return Every single value of each HTTP Header as immutable Map&lt;String,String&gt;, or an empty immutable one.
     *
     * @throws IllegalStateException if there is multiple values for a header
     */
    Map<String, String> singleValues();

    /**
     * Get all headers first values.
     *
     * @return Every first value of each HTTP Header as immutable Map&lt;String,String&gt;, or an empty immutable one.
     */
    Map<String, String> firstValues();

    /**
     * Get all headers last values.
     *
     * @return Every last value of each HTTP Header as immutable Map&lt;String,String&gt;, or an empty immutable one.
     */
    Map<String, String> lastValues();

    /**
     * Get all headers values.
     *
     * @return Every values of each HTTP Header as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *         or an empty immutable one.
     */
    Map<String, List<String>> allValues();

    /**
     * HTTP Header Names.
     */
    interface Names
    {
        /**
         * {@code "Accept"}.
         */
        String ACCEPT = "Accept";
        /**
         * {@code "Accept-Charset"}.
         */
        String ACCEPT_CHARSET = "Accept-Charset";
        /**
         * {@code "Accept-Encoding"}.
         */
        String ACCEPT_ENCODING = "Accept-Encoding";
        /**
         * {@code "Accept-Language"}.
         */
        String ACCEPT_LANGUAGE = "Accept-Language";
        /**
         * {@code "Accept-Ranges"}.
         */
        String ACCEPT_RANGES = "Accept-Ranges";
        /**
         * {@code "Accept-Patch"}.
         */
        String ACCEPT_PATCH = "Accept-Patch";
        /**
         * {@code "Access-Control-Allow-Credentials"}.
         */
        String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        /**
         * {@code "Access-Control-Allow-Headers"}.
         */
        String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        /**
         * {@code "Access-Control-Allow-Methods"}.
         */
        String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        /**
         * {@code "Access-Control-Allow-Origin"}.
         */
        String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        /**
         * {@code "Access-Control-Expose-Headers"}.
         */
        String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        /**
         * {@code "Access-Control-Max-Age"}.
         */
        String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        /**
         * {@code "Access-Control-Request-Headers"}.
         */
        String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        /**
         * {@code "Access-Control-Request-Method"}.
         */
        String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        /**
         * {@code "Age"}.
         */
        String AGE = "Age";
        /**
         * {@code "Allow"}.
         */
        String ALLOW = "Allow";
        /**
         * {@code "Authorization"}.
         */
        String AUTHORIZATION = "Authorization";
        /**
         * {@code "Cache-Control"}.
         */
        String CACHE_CONTROL = "Cache-Control";
        /**
         * {@code "Connection"}.
         */
        String CONNECTION = "Connection";
        /**
         * {@code "Content-Base"}.
         */
        String CONTENT_BASE = "Content-Base";
        /**
         * {@code "Content-Encoding"}.
         */
        String CONTENT_ENCODING = "Content-Encoding";
        /**
         * {@code "Content-Language"}.
         */
        String CONTENT_LANGUAGE = "Content-Language";
        /**
         * {@code "Content-Length"}.
         */
        String CONTENT_LENGTH = "Content-Length";
        /**
         * {@code "Content-Location"}.
         */
        String CONTENT_LOCATION = "Content-Location";
        /**
         * {@code "Content-Transfer-Encoding"}.
         */
        String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
        /**
         * {@code "Content-MD5"}.
         */
        String CONTENT_MD5 = "Content-MD5";
        /**
         * {@code "Content-Range"}.
         */
        String CONTENT_RANGE = "Content-Range";
        /**
         * {@code "Content-Type"}.
         */
        String CONTENT_TYPE = "Content-Type";
        /**
         * {@code "Cookie"}.
         */
        String COOKIE = "Cookie";
        /**
         * {@code "Date"}.
         */
        String DATE = "Date";
        /**
         * {@code "ETag"}.
         */
        String ETAG = "ETag";
        /**
         * {@code "Expect"}.
         */
        String EXPECT = "Expect";
        /**
         * {@code "Expires"}.
         */
        String EXPIRES = "Expires";
        /**
         * {@code "From"}.
         */
        String FROM = "From";
        /**
         * {@code "Host"}.
         */
        String HOST = "Host";
        /**
         * {@code "If-Match"}.
         */
        String IF_MATCH = "If-Match";
        /**
         * {@code "If-Modified-Since"}.
         */
        String IF_MODIFIED_SINCE = "If-Modified-Since";
        /**
         * {@code "If-None-Match"}.
         */
        String IF_NONE_MATCH = "If-None-Match";
        /**
         * {@code "If-Range"}.
         */
        String IF_RANGE = "If-Range";
        /**
         * {@code "If-Unmodified-Since"}.
         */
        String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
        /**
         * {@code "Last-Modified"}.
         */
        String LAST_MODIFIED = "Last-Modified";
        /**
         * {@code "Location"}.
         */
        String LOCATION = "Location";
        /**
         * {@code "Max-Forwards"}.
         */
        String MAX_FORWARDS = "Max-Forwards";
        /**
         * {@code "Origin"}.
         */
        String ORIGIN = "Origin";
        /**
         * {@code "Pragma"}.
         */
        String PRAGMA = "Pragma";
        /**
         * {@code "Proxy-Authenticate"}.
         */
        String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        /**
         * {@code "Proxy-Authorization"}.
         */
        String PROXY_AUTHORIZATION = "Proxy-Authorization";
        /**
         * {@code "Range"}.
         */
        String RANGE = "Range";
        /**
         * {@code "Referer"}.
         */
        String REFERER = "Referer";
        /**
         * {@code "Retry-After"}.
         */
        String RETRY_AFTER = "Retry-After";
        /**
         * {@code "Sec-WebSocket-Key1"}.
         */
        String SEC_WEBSOCKET_KEY1 = "Sec-WebSocket-Key1";
        /**
         * {@code "Sec-WebSocket-Key2"}.
         */
        String SEC_WEBSOCKET_KEY2 = "Sec-WebSocket-Key2";
        /**
         * {@code "Sec-WebSocket-Location"}.
         */
        String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
        /**
         * {@code "Sec-WebSocket-Origin"}.
         */
        String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
        /**
         * {@code "Sec-WebSocket-Protocol"}.
         */
        String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
        /**
         * {@code "Sec-WebSocket-Version"}.
         */
        String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
        /**
         * {@code "Sec-WebSocket-Key"}.
         */
        String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
        /**
         * {@code "Sec-WebSocket-Accept"}.
         */
        String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
        /**
         * {@code "Server"}.
         */
        String SERVER = "Server";
        /**
         * {@code "Set-Cookie"}.
         */
        String SET_COOKIE = "Set-Cookie";
        /**
         * {@code "Set-Cookie2"}.
         */
        String SET_COOKIE2 = "Set-Cookie2";
        /**
         * {@code "TE"}.
         */
        String TE = "TE";
        /**
         * {@code "Trailer"}.
         */
        String TRAILER = "Trailer";
        /**
         * {@code "Transfer-Encoding"}.
         */
        String TRANSFER_ENCODING = "Transfer-Encoding";
        /**
         * {@code "Upgrade"}.
         */
        String UPGRADE = "Upgrade";
        /**
         * {@code "User-Agent"}.
         */
        String USER_AGENT = "User-Agent";
        /**
         * {@code "Vary"}.
         */
        String VARY = "Vary";
        /**
         * {@code "Via"}.
         */
        String VIA = "Via";
        /**
         * {@code "Warning"}.
         */
        String WARNING = "Warning";
        /**
         * {@code "WebSocket-Location"}.
         */
        String WEBSOCKET_LOCATION = "WebSocket-Location";
        /**
         * {@code "WebSocket-Origin"}.
         */
        String WEBSOCKET_ORIGIN = "WebSocket-Origin";
        /**
         * {@code "WebSocket-Protocol"}.
         */
        String WEBSOCKET_PROTOCOL = "WebSocket-Protocol";
        /**
         * {@code "WWW-Authenticate"}.
         */
        String WWW_AUTHENTICATE = "WWW-Authenticate";
        /**
         * {@code "X-Forwarded-For"}.
         */
        String X_FORWARDED_FOR = "X-Forwarded-For";
        /**
         * {@code "X-HTTP-Method-Override"}.
         */
        String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
        /**
         * {@code "X-Werval-Content-Length"}.
         */
        String X_WERVAL_CONTENT_LENGTH = "X-Werval-Content-Length";
        /**
         * {@code "X-Werval-Client-IP"}.
         */
        String X_WERVAL_CLIENT_IP = "X-Werval-Client-IP";
        /**
         * {@code "X-Werval-Request-ID"}.
         */
        String X_WERVAL_REQUEST_ID = "X-Werval-Request-ID";
    }

    /**
     * HTTP Header Values.
     */
    interface Values
    {
        /**
         * {@code "base64"}.
         */
        String BASE64 = "base64";
        /**
         * {@code "binary"}.
         */
        String BINARY = "binary";
        /**
         * {@code "boundary"}.
         */
        String BOUNDARY = "boundary";
        /**
         * {@code "bytes"}.
         */
        String BYTES = "bytes";
        /**
         * {@code "charset"}.
         */
        String CHARSET = "charset";
        /**
         * {@code "chunked"}.
         */
        String CHUNKED = "chunked";
        /**
         * {@code "close"}.
         */
        String CLOSE = "close";
        /**
         * {@code "compress"}.
         */
        String COMPRESS = "compress";
        /**
         * {@code "100-continue"}.
         */
        String CONTINUE = "100-continue";
        /**
         * {@code "deflate"}.
         */
        String DEFLATE = "deflate";
        /**
         * {@code "gzip"}.
         */
        String GZIP = "gzip";
        /**
         * {@code "identity"}.
         */
        String IDENTITY = "identity";
        /**
         * {@code "keep-alive"}.
         */
        String KEEP_ALIVE = "keep-alive";
        /**
         * {@code "max-age"}.
         */
        String MAX_AGE = "max-age";
        /**
         * {@code "max-stale"}.
         */
        String MAX_STALE = "max-stale";
        /**
         * {@code "min-fresh"}.
         */
        String MIN_FRESH = "min-fresh";
        /**
         * {@code "must-revalidate"}.
         */
        String MUST_REVALIDATE = "must-revalidate";
        /**
         * {@code "no-cache"}.
         */
        String NO_CACHE = "no-cache";
        /**
         * {@code "no-store"}.
         */
        String NO_STORE = "no-store";
        /**
         * {@code "no-transform"}.
         */
        String NO_TRANSFORM = "no-transform";
        /**
         * {@code "none"}.
         */
        String NONE = "none";
        /**
         * {@code "only-if-cached"}.
         */
        String ONLY_IF_CACHED = "only-if-cached";
        /**
         * {@code "private"}.
         */
        String PRIVATE = "private";
        /**
         * {@code "proxy-revalidate"}.
         */
        String PROXY_REVALIDATE = "proxy-revalidate";
        /**
         * {@code "public"}.
         */
        String PUBLIC = "public";
        /**
         * {@code "quoted-printable"}.
         */
        String QUOTED_PRINTABLE = "quoted-printable";
        /**
         * {@code "s-maxage"}.
         */
        String S_MAXAGE = "s-maxage";
        /**
         * {@code "trailers"}.
         */
        String TRAILERS = "trailers";
        /**
         * {@code "Upgrade"}.
         */
        String UPGRADE = "Upgrade";
        /**
         * {@code "WebSocket"}.
         */
        String WEBSOCKET = "WebSocket";
    }
}
