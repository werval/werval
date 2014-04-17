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
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.Route;

/**
 * Request header.
 *
 * Method, Path, QueryString, Headers and Cookies.
 * <p>
 * No Entity.
 * <p>
 * Helper methods.
 * <p>
 * Parameters are available once bound to a Route, see
 * {@link #bind(org.qiweb.api.routes.ParameterBinders, org.qiweb.api.routes.Route)} and {@link #parameters()}.
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
     *
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
     *
     * HTTP Host header, mandatory since HTTP 1.1.
     *
     * @return The HTTP Host with or without the port, or an empty String
     */
    String host();

    /**
     * The HTTP Port.
     *
     * Computed from the request URI or HTTP Host header and standard defaults 80/443.
     *
     * @return The HTTP Port
     */
    int port();

    /**
     * The HTTP Domain.
     *
     * Computed from the request URI.
     *
     * @return The HTTP Domain
     */
    String domain();

    /**
     * The HTTP Request content type, or and empty String.
     *
     * Computed from the Content-Type header, charset removed.
     *
     * @return The HTTP Request content type, or and empty String
     */
    String contentType();

    /**
     * The HTTP Request Charset or an empty String.
     *
     * Computed from the Content-Type header.
     *
     * @return The HTTP Request Charset or an empty String
     */
    String charset();

    /**
     * Return {@literal true} if and only if the connection can remain open.
     *
     * Honnor {@link Headers.Names#CONNECTION} header and then protocol version defaults.
     *
     * @return {@literal true} if the connection should be kept-alive, {@literal false} otherwise
     */
    boolean isKeepAlive();

    /**
     * Bind Route parameters.
     *
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
     *
     * Guess the preferred language according to the {@literal Accept-Language} request header or Application's
     * preferred language, or the language of the default {@literal Locale}.
     *
     * @return The preferred language
     */
    Lang preferredLang();
}
