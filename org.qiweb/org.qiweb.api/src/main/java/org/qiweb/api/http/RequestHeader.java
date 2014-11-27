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
package org.qiweb.api.http;

import java.util.List;
import java.util.Map;
import org.qiweb.api.i18n.Lang;
import org.qiweb.api.mime.MediaRange;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.Route;

/**
 * Request header.
 * <p>
 * Method, Path, QueryString, Headers and Cookies.
 * <p>
 * No Entity.
 * <p>
 * Helper methods.
 * <p>
 * Parameters are available once bound to a Route, see
 * {@link #bind(org.qiweb.api.routes.ParameterBinders, org.qiweb.api.routes.Route)} and {@link #parameters()}.
 *
 * @navcomposed 1 - 1 ProtocolVersion
 * @navcomposed 1 - 1 Method
 * @navcomposed 1 - 1 QueryString
 * @navcomposed 1 - 1 Headers
 * @navcomposed 1 - 1 Cookies
 * @navcomposed 1 accepted * Lang
 * @navcomposed 1 preferred 1 Lang
 * @navcomposed 1 accepted * MediaRange
 */
public interface RequestHeader
{
    /**
     * @return The HTTP Request ID created by the framework
     */
    String identity();

    /**
     * @return The HTTP Request Protocol Version such as "HTTP/1.0" or "HTTP/1.1".
     */
    ProtocolVersion version();

    /**
     * @return The HTTP Request Method
     */
    Method method();

    /**
     * @return The HTTP Request URI, URL encoded
     */
    String uri();

    /**
     * @return The HTTP Request Path, URL decoded
     */
    String path();

    /**
     * @return The HTTP Request Query String
     */
    QueryString queryString();

    /**
     * @return The HTTP Request Headers
     */
    Headers headers();

    /**
     * @return The HTTP Request Cookies
     */
    Cookies cookies();

    /**
     * The HTTP Client Address.
     * <p>
     * Default value is the underlying socket client address.
     * <p>
     * If the {@literal qiweb.http.headers.x_forwarded_for.enabled} configuration property is {@literal yes}, then
     * the value is computed from the {@literal X-Forwarded-For} header value. This is the default behaviour.
     * <p>
     * If the {@literal qiweb.http.headers.x_forwarded_for.check_proxies} configuration property is {@literal yes},
     * then {@literal X-Forwarded-For} value is accepted only if all proxies in the chain are present in the
     * {@literal qiweb.http.headers.x_forwarded_for.trusted_proxies} configuration list. This is the default
     * behaviour trusting only {@literal 127.0.0.1}.
     * <p>
     * If the {@literal X-Forwarded-For} cannot be trusted, QiWeb respond with a {@literal 400 Bad Request}.
     *
     * @return The HTTP Client Address
     */
    String remoteAddress();

    /**
     * The HTTP Host with or without the port, or an empty String.
     * <p>
     * HTTP Host header, mandatory since HTTP 1.1.
     *
     * @return The HTTP Host with or without the port, or an empty String
     */
    String host();

    /**
     * The HTTP Port.
     * <p>
     * Computed from the request URI or HTTP Host header and standard defaults 80/443.
     *
     * @return The HTTP Port
     */
    int port();

    /**
     * The HTTP Domain.
     * <p>
     * Computed from the request URI.
     *
     * @return The HTTP Domain
     */
    String domain();

    /**
     * The HTTP Request content type, or and empty String.
     * <p>
     * Computed from the Content-Type header, charset removed.
     *
     * @return The HTTP Request content type, or and empty String
     */
    String contentType();

    /**
     * The HTTP Request Charset or an empty String.
     * <p>
     * Computed from the Content-Type header.
     *
     * @return The HTTP Request Charset or an empty String
     */
    String charset();

    /**
     * Return {@literal true} if and only if the connection can remain open.
     * <p>
     * Honnor {@link Headers.Names#CONNECTION} header and then protocol version defaults.
     *
     * @return {@literal true} if the connection should be kept-alive, {@literal false} otherwise
     */
    boolean isKeepAlive();

    /**
     * Bind Route parameters.
     * <p>
     * Successive calls to {@link #parameters()} will return bound parameters.
     *
     * @param parameterBinders Parameter binders
     * @param route            Route
     *
     * @return This very RequestHeader
     */
    RequestHeader bind( ParameterBinders parameterBinders, Route route );

    /**
     * Bound Route parameters.
     *
     * @return Request path and query-string bound parameters, or an empty Map if not bound
     */
    Map<String, Object> parameters();

    /**
     * Accepted Languages.
     *
     * @return Accepted languages, ordered by the q-values of the {@literal Accept-Language} header, preferred first
     */
    List<Lang> acceptedLangs();

    /**
     * Preferred Language.
     * <p>
     * Guess the preferred language according to the {@literal Accept-Language} request header or Application's
     * preferred language, or the language of the default {@literal Locale}.
     *
     * @return The preferred language
     */
    Lang preferredLang();

    /**
     * Accepted content mime types.
     *
     * @return Accepted content mime types, ordered by the q-values of the {@literal Accept} header
     */
    List<MediaRange> acceptedMimeTypes();

    /**
     * Test if this request accepts the given mime type.
     *
     * @param mimeType Mime type
     *
     * @return {@literal true} if {@literal mimeType} matches the Accept header, otherwise return {@literal false}
     */
    boolean acceptsMimeType( String mimeType );

    /**
     * Preferred Mime Type.
     * <p>
     * Guess the preferred mime types among given mime types according to the {@literal Accept} request header.
     * <p>
     * If the request accepts any mime type and no candidate were given, return {@literal *\/*}, otherwise return the
     * first candidate.
     * <p>
     * If no candidate is accepted, return the preferred mime type according the the {@literal Accept} request header.
     *
     * @param mimeTypes Candidate mime types
     *
     * @return The preferred mime type or the wildcard mimetype, never return null
     */
    String preferredMimeType( String... mimeTypes );
}
