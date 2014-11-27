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

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Maps Utilities.
 *
 * @navassoc 1 create * MapBuilder
 */
public final class Maps
{
    /**
     * Map Builder.
     *
     * @param <M> Map parameterized type
     * @param <K> Key parameterized type
     * @param <V> Value parameterized type
     *
     * @navassoc 1 create * Map
     */
    public interface MapBuilder<M extends Map<K, V>, K, V>
    {
        /**
         * Associates the specified value with the specified key in this map.
         *
         * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
         *
         * @param key   The key
         * @param value The value
         *
         * @return The builder for fluent usage
         */
        MapBuilder<M, K, V> put( K key, V value );

        /**
         * Build the Map.
         *
         * @return The built Map·
         */
        M toMap();
    }

    /**
     * MultiValueMap Builder.
     *
     * @param <M> Map parameterized type
     * @param <K> Key parameterized type
     * @param <V> Value parameterized type
     *
     * @navassoc 1 create * MultiValueMap
     */
    public interface MultiValueMapBuilder<M extends Map<K, List<V>>, K, V>
        extends MapBuilder<M, K, List<V>>
    {
        /**
         * Associates the specified values with the specified key in this map.
         *
         * If the map previously contained a mapping for the key, the old values are replaced by the specified values.
         *
         * @param key    The key
         * @param values The List of values
         *
         * @return The builder for fluent usage
         */
        @Override
        MultiValueMapBuilder<M, K, V> put( K key, List<V> values );

        /**
         * Add the specified values to the List associated with the specified key in this map.
         *
         * @param key        The key
         * @param value      A value
         * @param moreValues More values
         *
         * @return The builder for fluent usage
         */
        MultiValueMapBuilder<M, K, V> add( K key, V value, V... moreValues );
    }

    /**
     * Create a Map Builder starting with an empty HashMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty HashMap.
     */
    public static <K, V>
        MapBuilder<HashMap<K, V>, K, V> newHashMap( Class<? super K> kClass, Class<? super V> vClass )
    {
        return fromMap( new HashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty LinkedHashMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty LinkedHashMap.
     */
    public static <K, V>
        MapBuilder<LinkedHashMap<K, V>, K, V> newLinkedHashMap( Class<? super K> kClass, Class<? super V> vClass )
    {
        return fromMap( new LinkedHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentHashMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty ConcurrentHashMap.
     */
    public static <K, V>
        MapBuilder<ConcurrentHashMap<K, V>, K, V> newConcurrentHashMap(
            Class<? super K> kClass,
            Class<? super V> vClass
        )
    {
        return fromMap( new ConcurrentHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentSkipListMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty ConcurrentSkipListMap.
     */
    public static <K, V>
        MapBuilder<ConcurrentSkipListMap<K, V>, K, V> newConcurrentSkipListMap(
            Class<? super K> kClass,
            Class<? super V> vClass
        )
    {
        return fromMap( new ConcurrentSkipListMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentSkipListMap.
     *
     * @param <K>        Key parameterized type
     * @param <V>        Value parameterized type
     * @param kClass     Key Class
     * @param vClass     Value Class
     * @param comparator The comparator that will be used to order this map.
     *                   If null, the natural ordering of the keys will be used.
     *
     * @return The Map Builder started with an empty ConcurrentSkipListMap.
     */
    public static <K, V>
        MapBuilder<ConcurrentSkipListMap<K, V>, K, V> newConcurrentSkipListMap(
            Class<? super K> kClass,
            Class<? super V> vClass,
            Comparator<? super K> comparator
        )
    {
        return fromMap( new ConcurrentSkipListMap<K, V>( comparator ) );
    }

    /**
     * Create a Map Builder starting with an empty IdentityHashMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty IdentityHashMap.
     */
    public static <K, V>
        MapBuilder<IdentityHashMap<K, V>, K, V> newIdentityHashMap( Class<? super K> kClass, Class<? super V> vClass )
    {
        return fromMap( new IdentityHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty WeakHashMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty WeakHashMap.
     */
    public static <K, V>
        MapBuilder<WeakHashMap<K, V>, K, V> newWeakHashMap( Class<? super K> kClass, Class<? super V> vClass )
    {
        return fromMap( new WeakHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty TreeMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The Map Builder started with an empty TreeMap.
     */
    public static <K, V>
        MapBuilder<TreeMap<K, V>, K, V> newTreeMap( Class<? super K> kClass, Class<? super V> vClass )
    {
        return fromMap( new TreeMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty TreeMap.
     *
     * @param <K>        Key parameterized type
     * @param <V>        Value parameterized type
     * @param kClass     Key Class
     * @param vClass     Value Class
     * @param comparator The comparator that will be used to order this map.
     *                   If null, the natural ordering of the keys will be used.
     *
     * @return The Map Builder started with an empty TreeMap.
     */
    public static <K, V>
        MapBuilder<TreeMap<K, V>, K, V> newTreeMap(
            Class<? super K> kClass,
            Class<? super V> vClass,
            Comparator<? super K> comparator
        )
    {
        return fromMap( new TreeMap<K, V>( comparator ) );
    }

    /**
     * Create a MultiValueMap Builder starting with an empty LinkedMultiValueMap.
     *
     * @param <K>    Key parameterized type
     * @param <V>    Value parameterized type
     * @param kClass Key Class
     * @param vClass Value Class
     *
     * @return The MultiValueMap Builder started with an empty LinkedMultiValueMap
     */
    public static <K, V>
        MultiValueMapBuilder<LinkedMultiValueMap<K, V>, K, V> newLinkedMultiValueMap(
            Class<? super K> kClass,
            Class<? super V> vClass
        )
    {
        return fromMap( new LinkedMultiValueMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with a given Map.
     *
     * @param <M> Map parameterized type
     * @param <K> Key parameterized type
     * @param <V> Value parameterized type
     *
     * @param map The Map the Builder will use
     *
     * @return The Map Builder started with the given Map
     */
    public static <M extends Map<K, V>, K, V>
        MapBuilder<M, K, V> fromMap( M map )
    {
        return new MapBuilderImpl<>( map );
    }

    /**
     * Create a MultiValueMap Builder starting with a given MultiValueMap.
     *
     * @param <M> Map parameterized type
     * @param <K> Key parameterized type
     * @param <V> Value parameterized type
     * @param map The MultiValueMap the Builder will use
     *
     * @return The MultiValueMap Builder started with the given MultiValueMap
     */
    public static <M extends MultiValueMap<K, V>, K, V>
        MultiValueMapBuilder<M, K, V> fromMap( M map )
    {
        return new MultiValueMapBuilderImpl<>( map );
    }

    private static class MapBuilderImpl<M extends Map<K, V>, K, V>
        implements MapBuilder<M, K, V>
    {
        protected final M map;

        private MapBuilderImpl( M map )
        {
            this.map = map;
        }

        @Override
        public MapBuilder<M, K, V> put( K key, V value )
        {
            map.put( key, value );
            return this;
        }

        @Override
        public M toMap()
        {
            return map;
        }
    }

    private static final class MultiValueMapBuilderImpl<M extends MultiValueMap<K, V>, K, V>
        extends MapBuilderImpl<M, K, List<V>>
        implements MultiValueMapBuilder<M, K, V>
    {
        private MultiValueMapBuilderImpl( M map )
        {
            super( map );
        }

        @Override
        public MultiValueMapBuilder<M, K, V> add( K key, V value, V... moreValues )
        {
            map.add( key, value, moreValues );
            return this;
        }

        @Override
        public MultiValueMapBuilder<M, K, V> put( K key, List<V> values )
        {
            map.put( key, values );
            return this;
        }

        @Override
        public M toMap()
        {
            return map;
        }
    }

    /**
     * Returns an unmodifiable view of the specified MultiValueMap.
     *
     * @param <K>   Parameterized type of the MultiValueMap keys
     * @param <V>   Parameterized type of the MultiValueMap values
     * @param mvmap The MultiValueMap for which an unmodifiable view is to be returned.
     *
     * @return An unmodifiable view of the specified MultiValueMap.
     */
    public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap( MultiValueMap<? extends K, ? extends V> mvmap )
    {
        return new UnmodifiableMultiValueMap<>( mvmap );
    }

    private Maps()
    {
    }
}
