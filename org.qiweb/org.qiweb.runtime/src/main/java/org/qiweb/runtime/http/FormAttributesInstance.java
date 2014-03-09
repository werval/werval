/**
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.http.FormAttributes;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

public class FormAttributesInstance
    implements FormAttributes
{
    private final Map<String, List<String>> attributes;

    public FormAttributesInstance( boolean allowMultiValuedAttributes, Map<String, List<String>> attributes )
    {
        this.attributes = new TreeMap<>( Comparators.LOWER_CASE );
        if( attributes != null )
        {
            for( Map.Entry<String, List<String>> entry : attributes.entrySet() )
            {
                String name = entry.getKey();
                if( !this.attributes.containsKey( name ) )
                {
                    this.attributes.put( name, new ArrayList<>() );
                }
                List<String> values = entry.getValue();
                if( !allowMultiValuedAttributes && ( !this.attributes.get( name ).isEmpty() || values.size() > 1 ) )
                {
                    throw new BadRequestException( "Multi-valued attributes are not allowed" );
                }
                this.attributes.get( name ).addAll( entry.getValue() );
            }
        }
    }

    @Override
    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    @Override
    public boolean has( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        return attributes.containsKey( name );
    }

    @Override
    public Set<String> names()
    {
        return Collections.unmodifiableSet( attributes.keySet() );
    }

    @Override
    public String singleValue( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = attributes.get( name );
        if( values.size() != 1 )
        {
            throw new BadRequestException( "Form Attribute " + name + " has multiple values" );
        }
        return values.get( 0 );
    }

    @Override
    public String firstValue( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        return attributes.get( name ).get( 0 );
    }

    @Override
    public String lastValue( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = attributes.get( name );
        return values.get( values.size() - 1 );
    }

    @Override
    public List<String> values( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList( attributes.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : attributes.keySet() )
        {
            map.put( name, singleValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : attributes.keySet() )
        {
            map.put( name, firstValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : attributes.keySet() )
        {
            map.put( name, lastValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return Collections.unmodifiableMap( attributes );
    }
}
