/**
 * Copyright (c) 2013 the original author or authors
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

/**
 * Method, Path, QueryString, Headers and Cookies.
 * 
 * <p>No Entity.</p>
 * <p>Helper methods.</p>
 *
 * @composed 1 - 1 QueryString
 * @composed 1 - 1 Headers
 * @composed 1 - 1 Cookies
 */
public interface RequestHeader
{

    /**
     * @return The HTTP Request ID created by the framework
     */
    String identity();

    /**
     * @return The HTTP Request Protocol Version
     */
    String version();

    /**
     * @return The HTTP Request Method
     */
    String method();

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
     * <p>Computed from the X-Forwarded-For header value if this header is present and the local address is loopback.</p>
     * @return The HTTP Client Address or an empty String
     */
    String remoteAddress();

    /**
     * The HTTP Host with or without the port, or an empty String.
     * <p>HTTP Host header, mandatory since HTTP 1.1.</p>
     * @return The HTTP Host with or without the port, or an empty String
     */
    String host();

    /**
     * The HTTP Port.
     * <p>Computed from the request URI or HTTP Host header and standard defaults 80/443.</p>
     * @return The HTTP Port
     */
    int port();

    /**
     * The HTTP Domain.
     * <p>Computed from the request URI.</p>
     * @return The HTTP Domain
     */
    String domain();

    /**
     * The HTTP Request content type, or and empty String.
     * <p>Computed from the Content-Type header, charset removed.</p>
     * @return The HTTP Request content type, or and empty String
     */
    String contentType();

    /**
     * The HTTP Request Charset or an empty String.
     * <p>Computed from the Content-Type header.</p>
     * @return The HTTP Request Charset or an empty String
     */
    String charset();
}
