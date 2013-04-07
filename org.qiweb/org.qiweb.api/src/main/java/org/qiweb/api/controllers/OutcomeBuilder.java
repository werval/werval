package org.qiweb.api.controllers;

import java.io.InputStream;

/**
 * Builder for Outcomes.
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
