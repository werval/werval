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

import java.util.Set;
import org.qiweb.api.http.Cookies.Cookie;

/**
 * HTTP Cookies.
 *
 * Cookies are loaded from the {@literal Cookie} header of a {@link RequestHeader}.
 * <p>
 * See:
 * <ul>
 * <li><a href="http://tools.ietf.org/html/rfc2109">RFC2109 - HTTP State Management Mechanism - 1997</a>,</li>
 * <li><a href="http://tools.ietf.org/html/rfc2965">RFC2965 - HTTP State Management Mechanism - 2000</a>,</li>
 * <li><a href="http://tools.ietf.org/html/rfc6265">RFC6265 - HTTP State Management Mechanism - 2011</a>.</li>
 * </ul>
 */
public interface Cookies
    extends Iterable<Cookie>
{
    /**
     * @return TRUE if there's no cookie, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the cookie
     *
     * @return TRUE if there's an cookie with the given name
     */
    boolean has( String name );

    /**
     * @return The Set of HTTP Cookie names.
     */
    Set<String> names();

    /**
     * @param name Name of the HTTP Cookie
     *
     * @return The Cookie
     */
    Cookie get( String name );

    /**
     * @param name Name of the HTTP Cookie
     *
     * @return Value for this HTTP Cookie name or an empty String
     */
    String value( String name );

    /**
     * HTTP Cookie.
     */
    interface Cookie
    {
        /**
         * @return Version of the cookie
         */
        int version();

        /**
         * @return Name of the cookie
         */
        String name();

        /**
         * @return The value of the cookie
         */
        String value();

        /**
         * @return Path of the cookie
         */
        String path();

        /**
         * @return Domain of the cookie
         */
        String domain();

        /**
         * @return Maximum age of the cookie
         */
        long maxAge();

        /**
         * @return TRUE if the cookie is secure, FALSE otherwise
         */
        boolean secure();

        /**
         * @return TRUE if the cookie is httpOnly, FALSE otherwise
         */
        boolean httpOnly();

        /**
         * @return Comment of the cookie
         */
        String comment();

        /**
         * @return Comment URL of the cookie
         */
        String commentUrl();
    }
}
