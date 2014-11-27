/*
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

import java.util.Map;
import org.qiweb.api.http.Cookies.Cookie;

/**
 * Session.
 *
 * @navassoc 1 create-signed 1 Cookie
 */
public interface Session
{
    /**
     * Check if the Session has changed.
     *
     * @return TRUE if the Session has changed, FALSE otherwise
     */
    boolean hasChanged();

    /**
     * Check if the Session has a value for a key.
     *
     * @param key Session key
     *
     * @return TRUE if the Session has a value for the given key
     */
    boolean has( String key );

    /**
     * Get a Session value.
     *
     * @param key Session key
     *
     * @return Session value for the given key, null if absent
     */
    String get( String key );

    /**
     * Set a Session value.
     *
     * @param key   Session key
     * @param value Session value
     */
    void set( String key, String value );

    /**
     * Remove a value from the Session.
     *
     * @param key Session key
     *
     * @return Returns the value to which this Session previously associated the key, or null if the Session contained
     *         no value for the key.
     */
    String remove( String key );

    /**
     * Clear all Session values.
     */
    void clear();

    /**
     * @return A Map copy of the Session values
     */
    Map<String, String> asMap();

    /**
     * @return Signed Session Cookie
     */
    Cookie signedCookie();
}
