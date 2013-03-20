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
     * @return The Set of HTTP Header names.
     */
    Set<String> names();

    /**
     * @param name Name of the HTTP Header
     * @return First value for this HTTP Header name
     */
    String valueOf( String name );

    /**
     * @param name Name of the HTTP Header
     * @return All values for this HTTP Header name
     */
    List<String> valuesOf( String name );

    /**
     * @return Every first value of each HTTP Header.
     */
    Map<String, String> toMap();

    /**
     * @return Every values of each HTTP Header.
     */
    Map<String, List<String>> toMapAll();
}
