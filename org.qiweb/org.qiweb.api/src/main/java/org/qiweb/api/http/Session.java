package org.qiweb.api.http;

import java.util.Map;
import org.qiweb.api.http.Cookies.Cookie;

public interface Session
{

    boolean hasChanged();

    boolean has( String key );

    String get( String key );

    void set( String key, String value );

    String remove( String key );

    void clear();

    Map<String, String> asMap();

    Cookie signedCookie();
}
