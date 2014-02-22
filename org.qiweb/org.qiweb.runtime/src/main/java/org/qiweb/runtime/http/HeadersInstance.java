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
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

/**
 * Instance of HTTP Headers.
 */
public final class HeadersInstance
    implements MutableHeaders
{
    private final boolean allowMultiValuedHeaders;
    private final Map<String, List<String>> headers;

    /**
     * Create new empty Headers instance.
     * @param allowMultiValuedHeaders Allow multi-valued headers
     */
    public HeadersInstance( boolean allowMultiValuedHeaders )
    {
        this.allowMultiValuedHeaders = allowMultiValuedHeaders;
        this.headers = new TreeMap<>( Comparators.LOWER_CASE );
    }

    /**
     * Deep-copy constructor.
     * @param allowMultiValuedHeaders Allow multi-valued headers
     * @param headers Headers to copy
     */
    public HeadersInstance( boolean allowMultiValuedHeaders, Headers headers )
    {
        this( allowMultiValuedHeaders );
        withAll( headers );
    }

    @Override
    public boolean isEmpty()
    {
        return headers.isEmpty();
    }

    @Override
    public boolean has( String name )
    {
        ensureNotEmpty( "Header Name", name );
        return headers.containsKey( name );
    }

    @Override
    public Set<String> names()
    {
        return Collections.unmodifiableSet( headers.keySet() );
    }

    @Override
    public String singleValue( String name )
    {
        ensureNotEmpty( "Header Name", name );
        if( !headers.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = headers.get( name );
        if( values.size() != 1 )
        {
            throw new BadRequestException( "Header " + name + " has multiple values" );
        }
        return values.get( 0 );
    }

    @Override
    public String firstValue( String name )
    {
        ensureNotEmpty( "Header Name", name );
        if( !headers.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        return headers.get( name ).get( 0 );
    }

    @Override
    public String lastValue( String name )
    {
        ensureNotEmpty( "Header Name", name );
        if( !headers.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = headers.get( name );
        return values.get( values.size() - 1 );
    }

    @Override
    public List<String> values( String name )
    {
        ensureNotEmpty( "Header Name", name );
        if( !headers.containsKey( name ) )
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList( headers.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : headers.keySet() )
        {
            map.put( name, singleValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : headers.keySet() )
        {
            map.put( name, firstValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : headers.keySet() )
        {
            map.put( name, lastValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return Collections.unmodifiableMap( headers );
    }

    @Override
    public MutableHeaders without( String name )
    {
        ensureNotEmpty( "Header Name", name );
        headers.remove( name );
        return this;
    }

    @Override
    public MutableHeaders with( String name, String value )
    {
        ensureNotEmpty( "Header Name", name );
        if( headers.get( name ) == null )
        {
            headers.put( name, new ArrayList<>() );
        }
        else if( !allowMultiValuedHeaders && headers.get( name ).size() > 0 )
        {
            throw new BadRequestException( "Multi-valued headers are not allowed" );
        }
        headers.get( name ).add( value == null ? Strings.EMPTY : value );
        return this;
    }

    @Override
    public MutableHeaders withSingle( String name, String value )
    {
        ensureNotEmpty( "Header Name", name );
        if( headers.get( name ) == null )
        {
            headers.put( name, new ArrayList<>() );
        }
        else
        {
            headers.get( name ).clear();
        }
        headers.get( name ).add( value == null ? Strings.EMPTY : value );
        return this;
    }

    @Override
    public MutableHeaders withAll( String name, String... values )
    {
        ensureNotEmpty( "Header Name", name );
        for( String value : values )
        {
            with( name, value );
        }
        return this;
    }

    @Override
    public MutableHeaders withAll( Headers headers )
    {
        for( Entry<String, List<String>> header : headers.allValues().entrySet() )
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
