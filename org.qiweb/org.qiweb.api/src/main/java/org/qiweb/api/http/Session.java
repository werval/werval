package org.qiweb.api.http;

public interface Session
{

    boolean has( String key );

    String get( String key );

    void set( String key, String value );

    void remove( String key );

    void clear();
}
