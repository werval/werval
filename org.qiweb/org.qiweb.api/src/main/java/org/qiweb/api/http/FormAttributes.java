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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Form Attributes.
 */
public interface FormAttributes
{
    /**
     * @return TRUE if there's no attribute, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the attribute
     *
     * @return TRUE if there's an attribute with the given name
     */
    boolean has( String name );

    /**
     * @return All form attributes names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * Get single form attribute value, ensuring it has only one value.
     *
     * @param name Name of the form attribute
     *
     * @return Value for this form attribute name or an empty String
     *
     * @throws IllegalStateException if there is multiple values for this form attribute
     */
    String singleValue( String name );

    /**
     * Get first form attribute value.
     *
     * @param name Name of the form attribute
     *
     * @return First value for this form attribute name or an empty String
     */
    String firstValue( String name );

    /**
     * Get last form attribute value.
     *
     * @param name Name of the form attribute
     *
     * @return Last value for this form attribute name or an empty String
     */
    String lastValue( String name );

    /**
     * Get all form attribute values.
     *
     * @param name Name of the form attribute
     *
     * @return All String values of the form for the given name as immutable List&lt;String&gt;,
     *         or an immutable empty one.
     */
    List<String> values( String name );

    /**
     * Get all form attributes single values, ensuring each has only one value.
     *
     * @return Every single value of each form attribute as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     *
     * @throws IllegalStateException if there is multiple values for a parameter
     */
    Map<String, String> singleValues();

    /**
     * Get all form attributes first values.
     *
     * @return Every first value of each form attribute as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     */
    Map<String, String> firstValues();

    /**
     * Get all form attributes last values.
     *
     * @return Every last value of each form attribute as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     */
    Map<String, String> lastValues();

    /**
     * Get all form attributes values.
     *
     * @return Every values of each form attribute as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *         or an empty immutable one.
     */
    Map<String, List<String>> allValues();
}
