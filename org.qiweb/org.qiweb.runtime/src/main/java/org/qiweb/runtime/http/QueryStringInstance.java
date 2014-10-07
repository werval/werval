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
package org.qiweb.runtime.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.http.QueryString;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.util.Strings;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.qiweb.runtime.util.Comparators.LOWER_CASE;
import static org.qiweb.util.IllegalArguments.ensureNotEmpty;

public class QueryStringInstance
    implements QueryString, Serializable
{
    public static final QueryString EMPTY = new QueryStringInstance();
    private final Map<String, List<String>> parameters;

    public QueryStringInstance()
    {
        this( emptyMap() );
    }

    public QueryStringInstance( Map<String, List<String>> parameters )
    {
        this.parameters = new TreeMap<>( LOWER_CASE );
        this.parameters.putAll( parameters );
    }

    @Override
    public boolean isEmpty()
    {
        return parameters.isEmpty();
    }

    @Override
    public boolean has( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        return parameters.containsKey( name );
    }

    @Override
    public Set<String> names()
    {
        return unmodifiableSet( parameters.keySet() );
    }

    @Override
    public String singleValue( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = parameters.get( name );
        if( values.size() != 1 )
        {
            throw new BadRequestException( "QueryString Parameter '" + name + "' has multiple values" );
        }
        return values.get( 0 );
    }

    @Override
    public String firstValue( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        return parameters.get( name ).get( 0 );
    }

    @Override
    public String lastValue( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = parameters.get( name );
        return values.get( values.size() - 1 );
    }

    @Override
    public List<String> values( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        if( !parameters.containsKey( name ) )
        {
            return emptyList();
        }
        return unmodifiableList( parameters.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> singleValues = new TreeMap<>( LOWER_CASE );
        parameters.keySet().stream().forEach( name -> singleValues.put( name, singleValue( name ) ) );
        return unmodifiableMap( singleValues );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> firstValues = new TreeMap<>( LOWER_CASE );
        parameters.keySet().stream().forEach( name -> firstValues.put( name, firstValue( name ) ) );
        return unmodifiableMap( firstValues );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> lastValues = new TreeMap<>( LOWER_CASE );
        parameters.keySet().stream().forEach( name -> lastValues.put( name, lastValue( name ) ) );
        return unmodifiableMap( lastValues );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return unmodifiableMap( parameters );
    }
}
