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
