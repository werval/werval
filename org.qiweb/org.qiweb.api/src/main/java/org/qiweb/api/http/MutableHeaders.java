package org.qiweb.api.http;

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
     * @return This very MutableHeaders
     */
    MutableHeaders without( String name );

    /**
     * Add a HTTP Header value.
     * 
     * @param name The HTTP Header name
     * @param value The HTTP Header value
     * @return This very MutableHeaders
     */
    MutableHeaders with( String name, String value );

    /**
     * Set a HTTP Header single value, removing previous value(s).
     * 
     * @param name The HTTP Header name
     * @param value The HTTP Header value
     * @return This very MutableHeaders
     */
    MutableHeaders withSingle( String name, String value );
}
