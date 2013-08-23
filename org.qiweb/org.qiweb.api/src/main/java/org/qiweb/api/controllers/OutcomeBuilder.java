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
package org.qiweb.api.controllers;

import java.io.InputStream;

/**
 * Builder for Outcomes.
 *
 * @navassoc - <build> - Outcome
 */
public interface OutcomeBuilder
{

    /**
     * Use a HTTP header value.
     * @param name The header name
     * @param value The header value
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withHeader( String name, String value );

    /**
     * @param contentType Outcome Content-Type
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder as( String contentType );

    /**
     * Use a String body.
     * <p>Content-Length header will be set and identity Transfer-Encoding used.</p>
     * @param body Body as String
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( String body );

    /**
     * Use an InputStream body if undetermined length.
     * <p>Content-Length header will NOT be set and chunked Transfer-Encoding used.</p>
     * <p>Use a chunk size as configured in <code>qiweb.http.chunksize</code></p>
     * @param body Body InputStream
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body );

    /**
     /**
     * Use an InputStream body if undetermined length.
     * <p>Content-Length header will NOT be set and chunked Transfer-Encoding used.</p>
     * @param body Body InputStream
     * @param chunkSize Desired HTTP chunk size
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body, int chunkSize );

    /**
     * Use an InputStream body of known length.
     * <p>Content-Length header will be set and identity Transfer-Encoding used.</p>
     * @param body Body InputStream
     * @param length Body length
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body, long length );

    /**
     * @return A new Outcome instance
     */
    Outcome build();
}
