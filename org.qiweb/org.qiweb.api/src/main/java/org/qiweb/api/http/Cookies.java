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

import java.util.Set;
import org.qiweb.api.http.Cookies.Cookie;

/**
 * HTTP Cookies.
 *
 * @composed 1 - * Cookie
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
     * @return TRUE if there's an cookie with the given name
     */
    boolean has( String name );

    /**
     * @return The Set of HTTP Cookie names.
     */
    Set<String> names();

    /**
     * @param name Name of the HTTP Cookie
     * @return The Cookie
     */
    Cookie get( String name );

    /**
     * @param name Name of the HTTP Cookie
     * @return Value for this HTTP Cookie name or an empty String
     */
    String value( String name );

    /**
     * HTTP Cookie.
     */
    interface Cookie
    {

        /**
         * @return Name of the cookie
         */
        String name();

        /**
         * @return Path of the cookie
         */
        String path();

        /**
         * @return Domain of the cookie
         */
        String domain();

        /**
         * @return TRUE if the cookie is secure, FALSE otherwise
         */
        boolean secure();

        /**
         * @return The value of the cookie
         */
        String value();

        /**
         * @return TRUE if the cookie is httpOnly, FALSE otherwise
         */
        boolean httpOnly();
    }
}
