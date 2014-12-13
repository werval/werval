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

/**
 * Mutable Cookies.
 */
public interface MutableCookies
    extends Cookies
{
    /**
     * Set a cookie value.
     *
     * @param name  Cookie name
     * @param value Cookie value
     *
     * @return This very MutableCookies instance
     */
    MutableCookies set( String name, String value );

    /**
     * Set a cookie value.
     *
     * @param cookie Cookie
     *
     * @return This very MutableCookies instance
     */
    MutableCookies set( Cookie cookie );

    /**
     * Invalidate a cookie.
     *
     * @param name Cookie name
     *
     * @return This very MutableCookies instance
     */
    MutableCookies invalidate( String name );
}
