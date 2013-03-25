package org.qiweb.api.http;

import java.util.Set;
import org.qiweb.api.http.Cookies.Cookie;

/**
 * HTTP Cookies.
 */
public interface Cookies
    extends Iterable<Cookie>
{

    /**
     * @return The Set of HTTP Cookie names.
     */
    Set<String> names();

    /**
     * @param name Name of the HTTP Cookie
     * @return The Cookie
     */
    Cookie get( String name );

    /**
     * @param name Name of the HTTP Cookie
     * @return Value for this HTTP Cookie name or an empty String
     */
    String valueOf( String name );

    /**
     * HTTP Cookie.
     */
    public interface Cookie
    {

        String name();

        String path();

        String domain();

        boolean secure();

        String value();

        boolean httpOnly();
    }
}
