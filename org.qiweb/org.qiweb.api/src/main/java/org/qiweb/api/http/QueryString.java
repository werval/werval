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
