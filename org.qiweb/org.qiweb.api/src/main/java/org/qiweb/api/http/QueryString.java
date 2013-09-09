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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * URI Query String.
 */
public interface QueryString
{

    /**
     * @return TRUE if there's no query string parameter, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the query string parameter
     * @return TRUE if there's a query string parameter with the given name
     */
    boolean has( String name );

    /**
     * @return  All query string parameter names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * Get single query parameter value, ensuring it has only one value.
     *
     * @param name Name of the query string parameter
     * @return  Value for this query string parameter name or an empty String
     * @throws IllegalStateException if there is multiple values for this query string parameter
     */
    String singleValue( String name );

    /**
     * Get first query string parameter value.
     *
     * @param name Name of the query string parameter
     * @return  First value for this query string parameter name or an empty String
     */
    String firstValue( String name );

    /**
     * Get last query string parameter value.
     *
     * @param name Name of the query string parameter
     * @return  Last value for this query string parameter name or an empty String
     */
    String lastValue( String name );

    /**
     * Get all query string parameter values.
     *
     * @param name Name of the query string parameter
     * @return  All String values from the query string for the given name as immutable List&lt;String&gt;,
     *          or an immutable empty one.
     */
    List<String> values( String name );

    /**
     * Get all query string parameters single values, ensuring each has only one value.
     *
     * @return  Every single value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     * @throws IllegalStateException if there is multiple values for a parameter
     */
    Map<String, String> singleValues();

    /**
     * Get all query string parameters first values.
     *
     * @return  Every first value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     */
    Map<String, String> firstValues();

    /**
     * Get all query string parameters last values.
     *
     * @return  Every last value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     */
    Map<String, String> lastValues();

    /**
     * Get all query string parameters values.
     *
     * @return  Every values of each query string parameter as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *          or an empty immutable one.
     */
    Map<String, List<String>> allValues();
}
