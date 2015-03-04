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

import static java.util.Collections.EMPTY_LIST;

/**
 * MultiValueMap MultiValued.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class MultiValueMapMultiValued<K, V>
    implements MultiValued<K, V>, Serializable
{
    protected final MultiValueMap<K, V> mvmap;

    public MultiValueMapMultiValued()
    {
        this.mvmap = new TreeMultiValueMap<>();
    }

    public MultiValueMapMultiValued( MultiValueMap<K, V> mvmap )
    {
        this.mvmap = mvmap;
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
        throws IllegalArgumentException
    {
        return singleValueOptional( key )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format( "No or multiple %s for %s", valueTypeDisplayName(), key )
                )
            );
    }

    @Override
    public V firstValue( K key )
        throws IllegalArgumentException
    {
        return firstValueOptional( key )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format( "No %s for %s", valueTypeDisplayName(), key )
                )
            );
    }

    @Override
    public V lastValue( K key )
        throws IllegalArgumentException
    {
        return lastValueOptional( key )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format( "No %s for %s", valueTypeDisplayName(), key )
                )
            );
    }

    @Override
    public Optional<V> singleValueOptional( K key )
    {
        return Optional.ofNullable( mvmap.getSingle( key ) );
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
        return Collections.unmodifiableList( mvmap.getOrDefault( key, EMPTY_LIST ) );
    }

    @Override
    public Map<K, V> singleValues()
    {
        return Collections.unmodifiableMap( mvmap.toMapSingleValues() );
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
