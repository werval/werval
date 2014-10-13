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
package org.qiweb.api.http;

import org.qiweb.api.http.Cookies.Cookie;

/**
 * HTTP API Objects Builders.
 * <p>
 * Use this to create instances of HTTP API Objects found in the {@link org.qiweb.api.http} package.
 * <p>
 * Typically used in controllers and filters.
 * <p>
 * All builders are immutable and reusable.
 */
public interface HttpBuilders
{
    /**
     * Create a new Cookie Builder.
     * <p>
     * Name is mandatory.
     *
     * @return A new Cookie Builder
     */
    CookieBuilder newCookieBuilder();

    /**
     * Cookie Builder.
     * <p>
     * Name is mandatory.
     */
    interface CookieBuilder
    {

        /**
         * Cookie version.
         * <p>
         * Can be {@literal 0} or {@literal 1}, default to {@literal 0}.
         *
         * @param version cookie version
         *
         * @return A new builder with the version set
         */
        CookieBuilder version( int version );

        /**
         * Cookie name.
         *
         * @param name cookie name
         *
         * @return A new builder with the name set
         */
        CookieBuilder name( String name );

        /**
         * Cookie value.
         * <p>
         * Default to an empty string.
         *
         * @param value cookie value
         *
         * @return A new builder with the value set
         */
        CookieBuilder value( String value );

        /**
         * Cookie path.
         * <p>
         * Default to {@literal /}.
         *
         * @param path cookie path
         *
         * @return A new builder with the path set
         */
        CookieBuilder path( String path );

        /**
         * Cookie domain.
         *
         * @param domain cookie domain
         *
         * @return A new builder with the domain set
         */
        CookieBuilder domain( String domain );

        /**
         * Cookie maximum age.
         * <p>
         * Default to {@link Long#MIN_VALUE}, see {@link Cookie#maxAge()}.
         *
         * @param maxAge cookie maximum age
         *
         * @return A new builder with the maximum age set
         */
        CookieBuilder maxAge( long maxAge );

        /**
         * Cookie secure flag.
         * <p>
         * Default to {@literal false}.
         *
         * @param secure cookie secure flag
         *
         * @return A new builder with the secure flag set
         */
        CookieBuilder secure( boolean secure );

        /**
         * Cookie httpOnly flag.
         * <p>
         * Default to {@literal true}.
         *
         * @param httpOnly cookie httpOnly flag
         *
         * @return A new builder with the httpOnly flag set
         */
        CookieBuilder httpOnly( boolean httpOnly );

        /**
         * Cookie comment.
         *
         * @param comment cookie comment
         *
         * @return A new builder with the comment set
         */
        CookieBuilder comment( String comment );

        /**
         * Cookie comment URL.
         *
         * @param commentUrl cookie comment URL
         *
         * @return A new builder with the comment URL set
         */
        CookieBuilder commentUrl( String commentUrl );

        /**
         * Build a new Cookie.
         *
         * @return A new Cookie
         *
         * @throws IllegalArgumentException if the builder state is not appropriate
         */
        Cookie build();
    }
}
