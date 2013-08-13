/**
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.runtime.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    private final Map<String, List<String>> headers;

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
        return Collections.unmodifiableSet( headers.keySet() );
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
        return Collections.unmodifiableList( headers.get( name ) );
    }

    @Override
    public Map<String, String> asMap()
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
    public Map<String, List<String>> asMapAll()
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
    public MutableHeaders withAll( String name, String... values )
    {
        for( String value : values )
        {
            with( name, value );
        }
        return this;
    }

    @Override
    public MutableHeaders withAll( Headers headers )
    {
        for( Entry<String, List<String>> header : headers.asMapAll().entrySet() )
        {
            withAll( header.getKey(), header.getValue().toArray( new String[ header.getValue().size() ] ) );
        }
        return this;
    }

    @Override
    public String toString()
    {
        return headers.toString();
    }
}
