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
package io.werval.api.outcomes;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Builder for Outcomes.
 *
 * @navassoc 1 create * Outcome
 */
public interface OutcomeBuilder
{
    /**
     * Use a HTTP header value.
     *
     * @param name  The header name
     * @param value The header value
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withHeader( String name, String value );

    /**
     * @param contentType Outcome Content-Type
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder as( String contentType );

    /**
     * @param contentType Outcome Content-Type
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asTextual( String contentType );

    /**
     * @param contentType Outcome Content-Type
     * @param charset     Charset
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asTextual( String contentType, Charset charset );

    /**
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asJson();

    /**
     * @param charset Charset
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asJson( Charset charset );

    /**
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asTextPlain();

    /**
     * @param charset Charset
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asTextPlain( Charset charset );

    /**
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asXml();

    /**
     * @param charset Charset
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asXml( Charset charset );

    /**
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asHtml();

    /**
     * @param charset Charset
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder asHtml( Charset charset );

    /**
     * Use a bytes body.
     *
     * Content-Length header will be set and identity Transfer-Encoding used.
     *
     * @param body Body bytes
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( byte[] body );

    /**
     * Use a characters body.
     *
     * Content-Length header will be set and identity Transfer-Encoding used.
     *
     * @param body Body as CharSequence
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( CharSequence body );

    /**
     * Use a characters body.
     *
     * Content-Length header will be set and identity Transfer-Encoding used.
     *
     * @param body    Body as CharSequence
     * @param charset Character encoding to use
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( CharSequence body, Charset charset );

    /**
     * Use an InputStream body if undetermined length.
     *
     * Content-Length header will NOT be set and chunked Transfer-Encoding used.
     * <p>
     * Use a chunk size as configured in <code>werval.http.chunksize</code>
     *
     * @param body Body InputStream
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body );

    /**
     * Use an InputStream body of undetermined length.
     * <p>
     * Content-Length header will NOT be set and chunked Transfer-Encoding used.
     *
     * @param body      Body InputStream, closed once consumed
     * @param chunkSize Desired HTTP chunk size
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body, int chunkSize );

    /**
     * Use an InputStream body of known length.
     *
     * Content-Length header will be set and identity Transfer-Encoding used.
     *
     * @param body   Body InputStream, closed once consumed
     * @param length Body length
     *
     * @return This very OutcomeBuilder instance
     */
    OutcomeBuilder withBody( InputStream body, long length );

    /**
     * @return A new Outcome instance
     */
    Outcome build();
}
