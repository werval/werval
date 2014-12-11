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
package io.werval.api.http;

/**
 * Mutable HTTP Headers.
 */
public interface MutableHeaders
    extends Headers
{
    /**
     * Remove all values of a HTTP Header.
     *
     * @param name The HTTP Header name
     *
     * @return This very MutableHeaders
     */
    MutableHeaders without( String name );

    /**
     * Add a HTTP Header value.
     *
     * @param name  The HTTP Header name
     * @param value The HTTP Header value
     *
     * @return This very MutableHeaders
     */
    MutableHeaders with( String name, String value );

    /**
     * Set a HTTP Header single value, removing previous value(s).
     *
     * @param name  The HTTP Header name
     * @param value The HTTP Header value
     *
     * @return This very MutableHeaders
     */
    MutableHeaders withSingle( String name, String value );

    /**
     * Add all HTTP Headers values.
     *
     * @param name   The HTTP Header name
     * @param values The HTTP Header values
     *
     * @return This very MutableHeaders
     */
    MutableHeaders withAll( String name, String... values );

    /**
     * Add all HTTP Headers values.
     *
     * @param headers Headers to add
     *
     * @return This very MutableHeaders
     */
    MutableHeaders withAll( Headers headers );
}
