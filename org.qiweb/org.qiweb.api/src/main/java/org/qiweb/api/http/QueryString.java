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
     * @return  All query string keys as immutable Set&lt;String&gt;.
     */
    Set<String> keys();

    /**
     * @return  First String value from the query string for the given key or an empty String.
     */
    String valueOf( String key );

    /**
     * @return  All String values from the query string for the given key as immutable List&lt;String&gt;,
     *          or an immutable empty one.
     */
    List<String> valuesOf( String key );

    /**
     * @return  First String values from the query string for all keys as immutable Map&lt;String,String&gt;,
     *          or an empty immutable one.
     */
    Map<String, String> asMap();

    /**
     * @return  All String values from the query string for all keys as immutable Map&lt;String,String&gt;,
     *          or an empty immutable one.
     */
    Map<String, List<String>> asMapAll();
}
