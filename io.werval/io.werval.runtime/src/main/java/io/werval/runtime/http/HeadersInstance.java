/*
 * Copyright (c) 2013-2015 the original author or authors
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.werval.api.http.Headers;
import io.werval.api.http.MutableHeaders;
import io.werval.util.MultiValueMapMultiValued;
import io.werval.util.Strings;
import io.werval.util.TreeMultiValueMap;

import static io.werval.runtime.util.Comparators.LOWER_CASE;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static java.util.Collections.emptyMap;

/**
 * Instance of HTTP Headers.
 */
public final class HeadersInstance
    extends MultiValueMapMultiValued<String, String>
    implements MutableHeaders, Serializable
{
    /**
     * Create new empty Headers instance.
     */
    public HeadersInstance()
    {
        this( emptyMap() );
    }

    /**
     * Deep-copy constructor.
     *
     * @param headers Headers to copy
     */
    private HeadersInstance( Headers headers )
    {
        this( emptyMap() );
        withAll( headers );
    }

    /**
     * Deep-copy Map constructor.
     *
     * @param values Headers to copy
     */
    private HeadersInstance( Map<String, List<String>> values )
    {
        this( values, DEFAULT_CONSTRAINT_EX_BUILDER );
    }

    /**
     * Deep-copy Map constructur with custom constraint exception builder.
     *
     * @param values              Headers to copy
     * @param constraintExBuilder MultiValued-constraint-exception builder
     */
    public HeadersInstance(
        Map<String, List<String>> values,
        Function<String, ? extends RuntimeException> constraintExBuilder
    )
    {
        super( new TreeMultiValueMap<>( LOWER_CASE ), constraintExBuilder );
        if( values != null )
        {
            values.entrySet().stream().forEach(
                val -> this.mvmap.put( val.getKey(), new ArrayList<>( val.getValue() ) )
            );
        }
    }

    @Override
    public MutableHeaders without( String name )
    {
        ensureNotEmpty( "Header Name", name );
        mvmap.remove( name );
        return this;
    }

    @Override
    public MutableHeaders with( String name, String value )
    {
        ensureNotEmpty( "Header Name", name );
        if( mvmap.get( name ) == null )
        {
            mvmap.put( name, new ArrayList<>() );
        }
        mvmap.get( name ).add( value == null ? Strings.EMPTY : value );
        return this;
    }

    @Override
    public MutableHeaders withSingle( String name, String value )
    {
        ensureNotEmpty( "Header Name", name );
        if( mvmap.get( name ) == null )
        {
            mvmap.put( name, new ArrayList<>() );
        }
        else
        {
            mvmap.get( name ).clear();
        }
        mvmap.get( name ).add( value == null ? Strings.EMPTY : value );
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
}
