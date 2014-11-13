/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LinkedMultiValueMap.
 *
 * @param <K> Key type
 * @param <V> Values type
 */
public class LinkedMultiValueMap<K, V>
    implements MultiValueMap<K, V>, Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<K, List<V>> internalMap;

    public LinkedMultiValueMap()
    {
        this.internalMap = new LinkedHashMap<>();
    }

    public LinkedMultiValueMap( int initialCapacity )
    {
        this.internalMap = new LinkedHashMap<>( initialCapacity );
    }

    public LinkedMultiValueMap( Map<K, List<V>> map )
    {
        this.internalMap = new LinkedHashMap<>( map );
    }

    @Override
    public void add( K key, V value, V... moreValues )
    {
        internalMap.computeIfAbsent( key, k -> new LinkedList<>() ).add( value );
        internalMap.get( key ).addAll( Arrays.asList( moreValues ) );
    }

    @Override
    public void setSingle( K key, V value )
    {
        List<V> values = new LinkedList<>();
        values.add( value );
        internalMap.put( key, values );
    }

    @Override
    public void setAll( Map<K, V> values )
    {
        values.entrySet().forEach( entry -> setSingle( entry.getKey(), entry.getValue() ) );
    }

    @Override
    public V getSingle( K key )
    {
        if( !internalMap.containsKey( key ) || internalMap.get( key ) == null || internalMap.get( key ).isEmpty() )
        {
            throw new IllegalStateException( "No values for key '" + key + "'" );
        }
        List<V> values = internalMap.get( key );
        if( values.size() != 1 )
        {
            throw new IllegalStateException( "Multiple values for key '" + key + "': " + values );
        }
        return values.get( 0 );
    }

    @Override
    public V getFirst( K key )
    {
        if( !internalMap.containsKey( key ) || internalMap.get( key ) == null || internalMap.get( key ).isEmpty() )
        {
            return null;
        }
        return internalMap.get( key ).get( 0 );
    }

    @Override
    public V getLast( K key )
    {
        if( !internalMap.containsKey( key ) || internalMap.get( key ) == null || internalMap.get( key ).isEmpty() )
        {
            return null;
        }
        List<V> values = internalMap.get( key );
        return values.get( values.size() - 1 );
    }

    @Override
    public Map<K, V> toMapSingleValues()
    {
        Map<K, V> map = new LinkedHashMap<>( internalMap.size() );
        internalMap.keySet().forEach( key -> map.put( key, getSingle( key ) ) );
        return map;
    }

    @Override
    public Map<K, V> toMapFirstValues()
    {
        Map<K, V> map = new LinkedHashMap<>( internalMap.size() );
        internalMap.keySet().forEach( key -> map.put( key, getFirst( key ) ) );
        return map;
    }

    @Override
    public Map<K, V> toMapLastValues()
    {
        Map<K, V> map = new LinkedHashMap<>( internalMap.size() );
        internalMap.keySet().forEach( key -> map.put( key, getLast( key ) ) );
        return map;
    }

    @Override
    public int size()
    {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty()
    {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey( Object key )
    {
        return internalMap.containsKey( key );
    }

    @Override
    public boolean containsValue( Object value )
    {
        return internalMap.containsValue( value );
    }

    @Override
    public List<V> get( Object key )
    {
        return internalMap.get( key );
    }

    @Override
    public List<V> put( K key, List<V> value )
    {
        return internalMap.put( key, value );
    }

    @Override
    public List<V> remove( Object key )
    {
        return internalMap.remove( key );
    }

    @Override
    public void putAll( Map<? extends K, ? extends List<V>> m )
    {
        internalMap.putAll( m );
    }

    @Override
    public void clear()
    {
        internalMap.clear();
    }

    @Override
    public Set<K> keySet()
    {
        return internalMap.keySet();
    }

    @Override
    public Collection<List<V>> values()
    {
        return internalMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet()
    {
        return internalMap.entrySet();
    }
}
