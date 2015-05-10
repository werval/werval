/*
 * Copyright (c) 2014-2015 the original author or authors
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
package io.werval.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * MultiValueMap MultiValued.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class MultiValueMapMultiValued<K, V>
    implements MultiValued<K, V>, Serializable
{
    protected static final Function<String, ? extends RuntimeException> DEFAULT_CONSTRAINT_EX_BUILDER;

    static
    {
        DEFAULT_CONSTRAINT_EX_BUILDER = message -> new MultiValuedConstraintException( message );
    }

    private final Function<String, ? extends RuntimeException> constraintExBuilder;
    protected final MultiValueMap<K, V> mvmap;

    public MultiValueMapMultiValued()
    {
        this( new TreeMultiValueMap<>(), DEFAULT_CONSTRAINT_EX_BUILDER );
    }

    public MultiValueMapMultiValued( MultiValueMap<K, V> mvmap )
    {
        this( mvmap, DEFAULT_CONSTRAINT_EX_BUILDER );
    }

    public MultiValueMapMultiValued(
        MultiValueMap<K, V> mvmap,
        Function<String, ? extends RuntimeException> constraintExBuilder
    )
    {
        this.mvmap = mvmap;
        this.constraintExBuilder = constraintExBuilder;
    }

    @Override
    public boolean isEmpty()
    {
        return mvmap.isEmpty() || mvmap.values().stream().allMatch( v -> v == null || v.isEmpty() );
    }

    @Override
    public boolean has( K key )
    {
        return mvmap.containsKey( key ) && mvmap.get( key ) != null && !mvmap.get( key ).isEmpty();
    }

    @Override
    public V singleValue( K key )
        throws MultiValuedConstraintException
    {
        return singleValueOptional( key )
            .orElseThrow(
                () -> constraintExBuilder.apply(
                    format( "No or multiple %s(s) for '%s'", valueTypeDisplayName(), key )
                )
            );
    }

    @Override
    public V firstValue( K key )
        throws MultiValuedConstraintException
    {
        return firstValueOptional( key )
            .orElseThrow(
                () -> constraintExBuilder.apply(
                    format( "No %s for '%s', so no first '%s'", valueTypeDisplayName(), key, valueTypeDisplayName() )
                )
            );
    }

    @Override
    public V lastValue( K key )
        throws MultiValuedConstraintException
    {
        return lastValueOptional( key )
            .orElseThrow(
                () -> constraintExBuilder.apply(
                    format( "No %s for '%s', so no last '%s'", valueTypeDisplayName(), key, valueTypeDisplayName() )
                )
            );
    }

    @Override
    public Optional<V> singleValueOptional( K key )
    {
        try
        {
            return Optional.ofNullable( mvmap.getSingle( key ) );
        }
        catch( IllegalStateException ex )
        {
            return Optional.empty();
        }
    }

    @Override
    public Optional<V> firstValueOptional( K key )
    {
        return Optional.ofNullable( mvmap.getFirst( key ) );
    }

    @Override
    public Optional<V> lastValueOptional( K key )
    {
        return Optional.ofNullable( mvmap.getLast( key ) );
    }

    @Override
    public Set<K> keys()
    {
        return Collections.unmodifiableSet( mvmap.keySet() );
    }

    @Override
    public List<V> values( K key )
    {
        return Collections.unmodifiableList( mvmap.getOrDefault( key, emptyList() ) );
    }

    @Override
    public Map<K, V> singleValues()
    {
        try
        {
            return Collections.unmodifiableMap( mvmap.toMapSingleValues() );
        }
        catch( IllegalStateException ex )
        {
            throw constraintExBuilder.apply( ex.getMessage() );
        }
    }

    @Override
    public Map<K, V> firstValues()
    {
        return Collections.unmodifiableMap( mvmap.toMapFirstValues() );
    }

    @Override
    public Map<K, V> lastValues()
    {
        return Collections.unmodifiableMap( mvmap.toMapLastValues() );
    }

    @Override
    public Map<K, List<V>> allValues()
    {
        return Maps.unmodifiableMultiValueMap( mvmap );
    }

    /**
     * Key type display name.
     * <p>
     * Defaults to {@literal key}, override to provide better error messages.
     *
     * @return the key type display name.
     */
    protected String keyTypeDisplayName()
    {
        return "key";
    }

    /**
     * Value type display name.
     * <p>
     * Defaults to {@literal value}, override to provide better error messages.
     *
     * @return the value type display name.
     */
    protected String valueTypeDisplayName()
    {
        return "value";
    }
}
