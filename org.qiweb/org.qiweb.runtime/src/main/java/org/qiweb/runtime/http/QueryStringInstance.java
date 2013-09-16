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
import org.qiweb.api.http.QueryString;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

public class QueryStringInstance
    implements QueryString
{

    public static final QueryString EMPTY = new QueryStringInstance();
    private final Map<String, List<String>> parameters;

    public QueryStringInstance()
    {
        this( Collections.<String, List<String>>emptyMap() );
    }

    public QueryStringInstance( boolean allowMultiValuedParameters )
    {
        this( allowMultiValuedParameters, Collections.<String, List<String>>emptyMap() );
    }

    public QueryStringInstance( Map<String, List<String>> parameters )
    {
        this( false, parameters );
    }

    public QueryStringInstance( boolean allowMultiValuedParameters, Map<String, List<String>> parameters )
    {
        this.parameters = new TreeMap<>( Comparators.LOWER_CASE );
        for( Entry<String, List<String>> entry : parameters.entrySet() )
        {
            String name = entry.getKey();
            if( !this.parameters.containsKey( name ) )
            {
                this.parameters.put( name, new ArrayList<String>() );
            }
            List<String> values = entry.getValue();
            if( !allowMultiValuedParameters && ( !this.parameters.get( name ).isEmpty() || values.size() > 1 ) )
            {
                throw new BadRequestException( "Multi-valued query string parameters are not allowed" );
            }
            this.parameters.get( name ).addAll( entry.getValue() );
        }
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
        return Collections.unmodifiableSet( parameters.keySet() );
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
            return Collections.emptyList();
        }
        return Collections.unmodifiableList( parameters.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : parameters.keySet() )
        {
            map.put( name, singleValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : parameters.keySet() )
        {
            map.put( name, firstValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : parameters.keySet() )
        {
            map.put( name, lastValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return Collections.unmodifiableMap( parameters );
    }
}
