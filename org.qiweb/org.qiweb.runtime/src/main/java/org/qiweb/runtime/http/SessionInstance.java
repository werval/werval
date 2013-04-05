package org.qiweb.runtime.http;

import java.util.Map;
import java.util.TreeMap;
import org.qiweb.api.http.Session;
import org.qiweb.runtime.util.Comparators;

public class SessionInstance
    implements Session
{

    private final Map<String, String> session;

    public SessionInstance()
    {
        this.session = new TreeMap<>( Comparators.LOWER_CASE );
    }

    public SessionInstance( Map<String, String> session )
    {
        this();
        this.session.putAll( session );
    }

    @Override
    public boolean has( String key )
    {
        return session.containsKey( key );
    }

    @Override
    public String get( String key )
    {
        return session.get( key );
    }

    @Override
    public void set( String key, String value )
    {
        session.put( key, value );
    }

    @Override
    public void remove( String key )
    {
        session.remove( key );
    }

    @Override
    public void clear()
    {
        session.clear();
    }
}
