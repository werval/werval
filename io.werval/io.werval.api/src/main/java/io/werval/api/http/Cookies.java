/*
 * Copyright (c) 2013-2015 the original author or authors
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

import java.util.Set;
import io.werval.api.http.Cookies.Cookie;
import java.util.Optional;

/**
 * HTTP Cookies.
 * <p>
 * Cookies are loaded from the {@literal Cookie} header of a {@link RequestHeader}.
 * <p>
 * See:
 * <ul>
 * <li><a href="http://tools.ietf.org/html/rfc2109">RFC2109 - HTTP State Management Mechanism - 1997</a>,</li>
 * <li><a href="http://tools.ietf.org/html/rfc2965">RFC2965 - HTTP State Management Mechanism - 2000</a>,</li>
 * <li><a href="http://tools.ietf.org/html/rfc6265">RFC6265 - HTTP State Management Mechanism - 2011</a>.</li>
 * </ul>
 *
 * @navcomposed 1 - * Cookie
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
     * @return The Cookie, optional
     */
    Optional<Cookie> get( String name );

    /**
     * @param name Name of the HTTP Cookie
     *
     * @return Value for this HTTP Cookie name, optional
     */
    Optional<String> value( String name );

    /**
     * HTTP Cookie.
     */
    interface Cookie
    {
        /**
         * Version of the cookie.
         *
         * @return Version of the cookie
         */
        int version();

        /**
         * Name of the cookie.
         *
         * @return Name of the cookie
         */
        String name();

        /**
         * Value of the cookie.
         *
         * @return The value of the cookie
         */
        String value();

        /**
         * Path of the cookie.
         *
         * @return Path of the cookie
         */
        String path();

        /**
         * Domain of the cookie.
         *
         * @return Domain of the cookie
         */
        String domain();

        /**
         * Maximum age of the cookie in seconds.
         * <p>
         * A positive value indicates that the cookie will expire after that many seconds have passed.
         * Note that the value is the maximum age when the cookie will expire, not the cookie's current age.
         * <p>
         * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser
         * exits.
         * <p>
         * A zero value causes the cookie to be deleted.
         *
         * @return a long specifying the maximum age of the cookie in seconds; if zero, the cookie should be discarded
         *         immediately; otherwise, the cookie's max age is unspecified
         */
        long maxAge();

        /**
         * Secure flag of the cookie.
         *
         * @return TRUE if the cookie is secure, FALSE otherwise
         */
        boolean secure();

        /**
         * HTTPOnly flag of the cookie.
         *
         * @return TRUE if the cookie is httpOnly, FALSE otherwise
         */
        boolean httpOnly();

        /**
         * Comment of the cookie.
         *
         * @return Comment of the cookie, optional
         */
        Optional<String> comment();

        /**
         * Comment URL of the cookie.
         *
         * @return Comment URL of the cookie, optional
         */
        Optional<String> commentUrl();
    }
}
