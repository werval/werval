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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.http.FormAttributes;
import org.qiweb.runtime.exceptions.BadRequestException;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;
import static org.qiweb.api.util.Strings.EMPTY;
import static org.qiweb.runtime.util.Comparators.LOWER_CASE;

public class FormAttributesInstance
    implements FormAttributes
{
    private final Map<String, List<String>> attributes = new TreeMap<>( LOWER_CASE );

    public FormAttributesInstance( Map<String, List<String>> attributes )
    {
        if( attributes != null )
        {
            attributes.entrySet().stream().forEach(
                attribute -> this.attributes.put( attribute.getKey(), new ArrayList<>( attribute.getValue() ) )
            );
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
        return unmodifiableSet( attributes.keySet() );
    }

    @Override
    public String singleValue( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return EMPTY;
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
            return EMPTY;
        }
        return attributes.get( name ).get( 0 );
    }

    @Override
    public String lastValue( String name )
    {
        ensureNotEmpty( "Form Attribute Name", name );
        if( !attributes.containsKey( name ) )
        {
            return EMPTY;
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
            return emptyList();
        }
        return unmodifiableList( attributes.get( name ) );
    }

    @Override
    public Map<String, String> singleValues()
    {
        Map<String, String> singleValues = new TreeMap<>( LOWER_CASE );
        attributes.keySet().stream().forEach( name -> singleValues.put( name, singleValue( name ) ) );
        return unmodifiableMap( singleValues );
    }

    @Override
    public Map<String, String> firstValues()
    {
        Map<String, String> firstValues = new TreeMap<>( LOWER_CASE );
        attributes.keySet().stream().forEach( name -> firstValues.put( name, firstValue( name ) ) );
        return unmodifiableMap( firstValues );
    }

    @Override
    public Map<String, String> lastValues()
    {
        Map<String, String> lastValues = new TreeMap<>( LOWER_CASE );
        attributes.keySet().stream().forEach( name -> lastValues.put( name, lastValue( name ) ) );
        return unmodifiableMap( lastValues );
    }

    @Override
    public Map<String, List<String>> allValues()
    {
        return unmodifiableMap( attributes );
    }
}
