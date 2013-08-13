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
        this.parameters = new TreeMap<>( Comparators.LOWER_CASE );
    }

    public QueryStringInstance( Map<String, List<String>> parameters )
    {
        this();
        for( Entry<String, List<String>> entry : parameters.entrySet() )
        {
            this.parameters.put( entry.getKey(), new ArrayList<>( entry.getValue() ) );
        }
    }

    @Override
    public Set<String> keys()
    {
        return Collections.unmodifiableSet( parameters.keySet() );
    }

    @Override
    public String valueOf( String key )
    {
        if( !parameters.containsKey( key ) )
        {
            return Strings.EMPTY;
        }
        return parameters.get( key ).get( 0 );
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
