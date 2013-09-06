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
import org.qiweb.api.http.QueryString;
import org.qiweb.runtime.util.Comparators;

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
            String key = entry.getKey();
            if( !this.parameters.containsKey( key ) )
            {
                this.parameters.put( key, new ArrayList<String>() );
            }
            List<String> values = entry.getValue();
            if( !allowMultiValuedParameters && ( !this.parameters.get( key ).isEmpty() || values.size() > 1 ) )
            {
                // TODO Make this lead to a 400 BadRequest, but how?
                throw new IllegalStateException( "Multi-valued query string parameters are not allowed" );
            }
            this.parameters.get( key ).addAll( entry.getValue() );
        }
    }

    @Override
    public Set<String> keys()
    {
        return Collections.unmodifiableSet( parameters.keySet() );
    }

    @Override
    public String singleValueOf( String name )
    {
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = parameters.get( name );
        if( values.size() != 1 )
        {
            throw new IllegalStateException( "QueryString parameter '" + name + "' has multiple values" );
        }
        return values.get( 0 );
    }

    @Override
    public String firstValueOf( String name )
    {
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        return parameters.get( name ).get( 0 );
    }

    @Override
    public String lastValueOf( String name )
    {
        if( !parameters.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = parameters.get( name );
        return values.get( values.size() - 1 );
    }

    @Override
    public List<String> valuesOf( String key )
    {
        if( !parameters.containsKey( key ) )
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList( parameters.get( key ) );
    }

    @Override
    public Map<String, String> asMap()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( Map.Entry<String, List<String>> entry : parameters.entrySet() )
        {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            map.put( key, values.get( 0 ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<String>> asMapAll()
    {
        return Collections.unmodifiableMap( parameters );
    }
}
