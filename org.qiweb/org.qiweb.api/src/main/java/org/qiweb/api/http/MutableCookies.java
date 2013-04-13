package org.qiweb.api.http;

public interface MutableCookies
    extends Cookies
{

    MutableCookies set( String name, String value );

    MutableCookies invalidate( String name );
}
