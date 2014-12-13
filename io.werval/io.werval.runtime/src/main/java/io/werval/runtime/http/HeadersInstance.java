/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.runtime.http;

import io.werval.api.http.Headers;
import io.werval.api.http.MutableHeaders;
import io.werval.runtime.exceptions.BadRequestException;
import io.werval.util.Strings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.werval.runtime.util.Comparators.LOWER_CASE;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Instance of HTTP Headers.
 */
public final class HeadersInstance
    implements MutableHeaders, Serializable
{
    public static final Headers EMPTY = new HeadersInstance();
    private final Map<String, List<String>> headers = new TreeMap<>( LOWER_CASE );

    /**
     * Create new empty Headers instance.
     */
    public HeadersInstance()
    {
    }

    /**
     * Deep-copy Map constructor.
     *
     * @param headers Headers to copy
     */
    public HeadersInstance( Map<String, List<String>> headers )
    {
        if( headers != null )
        {
            headers.entrySet().stream().forEach(
                header -> this.headers.put( header.getKey(), new ArrayList<>( header.getValue() ) )
            );
        }
    }

    /**
     * Deep-copy constructor.
     *
     * @param headers Headers to copy
     */
    public HeadersInstance( Headers headers )
    {
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
        return unmodifiableSet( headers.keySet() );
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
            return emptyList();
        }
        return unmodifiableList( headers.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> singleValues = new TreeMap<>( LOWER_CASE );
        headers.keySet().stream().forEach( name -> singleValues.put( name, singleValue( name ) ) );
        return unmodifiableMap( singleValues );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> firstValues = new TreeMap<>( LOWER_CASE );
        headers.keySet().stream().forEach( name -> firstValues.put( name, firstValue( name ) ) );
        return unmodifiableMap( firstValues );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> lastValues = new TreeMap<>( LOWER_CASE );
        headers.keySet().stream().forEach( name -> lastValues.put( name, lastValue( name ) ) );
        return unmodifiableMap( lastValues );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return unmodifiableMap( headers );
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
        if( headers != null )
        {
            headers.allValues().entrySet().stream().forEach(
                (header) ->
                {
                    withAll(
                        header.getKey(),
                        header.getValue().toArray( new String[ header.getValue().size() ] )
                    );
                }
            );
        }
        return this;
    }

    @Override
    public String toString()
    {
        return headers.toString();
    }
}
