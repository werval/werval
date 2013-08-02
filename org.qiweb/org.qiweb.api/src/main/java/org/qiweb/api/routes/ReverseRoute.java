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
