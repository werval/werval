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
package org.qiweb.api.routes;

import java.util.Map;

/**
 * Mutable Reverse Route.
 */
public interface ReverseRoute
{

    /**
     * @return HTTP Method
     */
    String method();

    /**
     * @return HTTP URI
     */
    String uri();

    /**
     * Append a parameter to this ReverseRoute Query String.
     * 
     * @param key QueryString key
     * @param value QueryString value
     * @return This very ReverseRoute as fluent API
     * @throws IllegalArgumentException when key is null or empty, or when value is null
     */
    ReverseRoute appendQueryString( String key, String value );

    /**
     * Append a bunch of parameters to this ReverseRoute Query String.
     * <p>
     *     If the given <code>parameters</code> Map is a Map&lt;String,List&lt;?&gt;&gt; then each value of each list is
     *     appended to the Query String.
     * </p>
     * <p>
     *     This is the result of each value's <code>toString()</code> method that is appended to the Query String.
     * </p>
     *
     * @param parameters Parameters as a Map
     * @return This very ReverseRoute as fluent API
     * @throws  IllegalArgumentException when parameters is null, or when one key is null or empty,
     *          or when one value is null
     */
    ReverseRoute appendQueryString( Map<String, ?> parameters );

    /**
     * @param fragmentIdentifier Fragment identifier
     * @return This very ReverseRoute as fluent API
     */
    ReverseRoute withFragmentIdentifier( String fragmentIdentifier );

    /**
     * @return Absolute HTTP URL
     */
    String httpUrl();

    /**
     * @param secure is the URL secure?
     * @return Absolute HTTP(s) URL
     */
    String httpUrl( boolean secure );

    /**
     * @return Absolute WebSocket URL
     */
    String webSocketUrl();

    /**
     * @param secure is the URL secure?
     * @return Absolute WebSocket(SSL) URL
     */
    String webSocketUrl( boolean secure );
}
