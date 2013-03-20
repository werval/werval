package org.qiweb.runtime.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.runtime.util.Comparators;

/**
 * Instance of HTTP Headers.
 */
public class HeadersInstance
    implements MutableHeaders
{

    private Map<String, List<String>> headers;

    /**
     * Create new empty Headers instance.
     */
    public HeadersInstance()
    {
        this.headers = new TreeMap<>( Comparators.LOWER_CASE );
    }

    /**
     * Deep-copy constructor.
     */
    public HeadersInstance( Headers headers )
    {
        this();
        for( String name : headers.names() )
        {
            this.headers.put( name, new ArrayList<>( headers.valuesOf( name ) ) );
        }
    }

    @Override
    public Set<String> names()
    {
        return headers.keySet();
    }

    @Override
    public String valueOf( String name )
    {
        if( !headers.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        return headers.get( name ).get( 0 );
    }

    @Override
    public List<String> valuesOf( String name )
    {
        if( !headers.containsKey( name ) )
        {
            return Collections.emptyList();
        }
        return headers.get( name );
    }

    @Override
    public Map<String, String> toMap()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( Map.Entry<String, List<String>> entry : headers.entrySet() )
        {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            map.put( name, values.get( 0 ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<String>> toMapAll()
    {
        return Collections.unmodifiableMap( headers );
    }

    @Override
    public MutableHeaders without( String name )
    {
        headers.remove( name );
        return this;
    }

    @Override
    public MutableHeaders with( String name, String value )
    {
        if( headers.get( name ) == null )
        {
            headers.put( name, new ArrayList<String>() );
        }
        headers.get( name ).add( value );
        return this;
    }

    @Override
    public MutableHeaders withSingle( String name, String value )
    {
        if( headers.get( name ) == null )
        {
            headers.put( name, new ArrayList<String>() );
        }
        else
        {
            headers.get( name ).clear();
        }
        headers.get( name ).add( value );
        return this;
    }

    @Override
    public String toString()
    {
        return headers.toString();
    }
}
