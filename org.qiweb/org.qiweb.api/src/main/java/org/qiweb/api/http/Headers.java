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
 * HTTP Headers.
 */
public interface Headers
{

    /**
     * @return  All HTTP Header names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * @param name Name of the HTTP Header
     * @return  First value for this HTTP Header name or an empty String
     */
    String valueOf( String name );

    /**
     * @param name Name of the HTTP Header
     * @return  All first values for this HTTP Header name as immutable List&lt;String&gt;, or an empty immutable one.
     */
    List<String> valuesOf( String name );

    /**
     * @return  Every first value of each HTTP Header as immutable Map&lt;String,String&gt;, or an empty immutable one.
     */
    Map<String, String> asMap();

    /**
     * @return  Every values of each HTTP Header as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *          or an empty immutable one.
     */
    Map<String, List<String>> asMapAll();
}
