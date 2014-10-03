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
package org.qiweb.api.util;

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
 * Map Builder.
 */
public final class MapBuilder
{
    /**
     * Map Builder.
     *
     * @param <M> Map type
     * @param <K> Key type
     * @param <V> Value type
     */
    public interface Builder<M extends Map<K, V>, K, V>
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
        Builder<M, K, V> put( K key, V value );

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
     * @param <M> Map type
     * @param <K> Key type
     * @param <V> Value type
     */
    public interface MultiValueBuilder<M extends Map<K, List<V>>, K, V>
        extends Builder<M, K, List<V>>
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
        MultiValueBuilder<M, K, V> put( K key, List<V> values );

        /**
         * Add the specified values to the List associated with the specified key in this map.
         *
         * @param key        The key
         * @param value      A value
         * @param moreValues More values
         *
         * @return The builder for fluent usage
         */
        MultiValueBuilder<M, K, V> add( K key, V value, V... moreValues );
    }

    /**
     * Create a Map Builder starting with an empty HashMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty HashMap.
     */
    public static <K, V> Builder<HashMap<K, V>, K, V> newHashMap()
    {
        return fromMap( new HashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty LinkedHashMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty LinkedHashMap.
     */
    public static <K, V> Builder<LinkedHashMap<K, V>, K, V> newLinkedHashMap()
    {
        return fromMap( new LinkedHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentHashMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty ConcurrentHashMap.
     */
    public static <K, V> Builder<ConcurrentHashMap<K, V>, K, V> newConcurrentHashMap()
    {
        return fromMap( new ConcurrentHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentSkipListMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty ConcurrentSkipListMap.
     */
    public static <K, V> Builder<ConcurrentSkipListMap<K, V>, K, V> newConcurrentSkipListMap()
    {
        return fromMap( new ConcurrentSkipListMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty ConcurrentSkipListMap.
     *
     * @param <K>        Key type
     * @param <V>        Value type
     * @param comparator The comparator that will be used to order this map.
     *                   If null, the natural ordering of the keys will be used.
     *
     * @return The Map Builder started with an empty ConcurrentSkipListMap.
     */
    public static <K, V> Builder<ConcurrentSkipListMap<K, V>, K, V> newConcurrentSkipListMap(
        Comparator<? super K> comparator
    )
    {
        return fromMap( new ConcurrentSkipListMap<K, V>( comparator ) );
    }

    /**
     * Create a Map Builder starting with an empty IdentityHashMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty IdentityHashMap.
     */
    public static <K, V> Builder<IdentityHashMap<K, V>, K, V> newIdentityHashMap()
    {
        return fromMap( new IdentityHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty WeakHashMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty WeakHashMap.
     */
    public static <K, V> Builder<WeakHashMap<K, V>, K, V> newWeakHashMap()
    {
        return fromMap( new WeakHashMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty TreeMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The Map Builder started with an empty TreeMap.
     */
    public static <K, V> Builder<TreeMap<K, V>, K, V> newTreeMap()
    {
        return fromMap( new TreeMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with an empty TreeMap.
     *
     * @param <K>        Key type
     * @param <V>        Value type
     * @param comparator The comparator that will be used to order this map.
     *                   If null, the natural ordering of the keys will be used.
     *
     * @return The Map Builder started with an empty TreeMap.
     */
    public static <K, V> Builder<TreeMap<K, V>, K, V> newTreeMap( Comparator<? super K> comparator )
    {
        return fromMap( new TreeMap<K, V>( comparator ) );
    }

    /**
     * Create a MultiValueMap Builder starting with an empty LinkedMultiValueMap.
     *
     * @param <K> Key type
     * @param <V> Value type
     *
     * @return The MultiValueMap Builder started with an empty LinkedMultiValueMap
     */
    public static <K, V> MultiValueBuilder<LinkedMultiValueMap<K, V>, K, V> newLinkedMultiValueMap()
    {
        return fromMap( new LinkedMultiValueMap<K, V>() );
    }

    /**
     * Create a Map Builder starting with a given Map.
     *
     * @param <M> Map type
     * @param <K> Key type
     * @param <V> Value type
     *
     * @param map The Map the Builder will use
     *
     * @return The Map Builder started with the given Map
     */
    public static <M extends Map<K, V>, K, V> Builder<M, K, V> fromMap( M map )
    {
        return new SingleValueBuilderImpl<>( map );
    }

    /**
     * Create a MultiValueMap Builder starting with a given MultiValueMap.
     *
     * @param <M> MultiValueMap type
     * @param <K> Key type
     * @param <V> Value type
     * @param map The MultiValueMap the Builder will use
     *
     * @return The MultiValueMap Builder started with the given MultiValueMap
     */
    public static <M extends MultiValueMap<K, V>, K, V> MultiValueBuilder<M, K, V> fromMap( M map )
    {
        return new MultiValueBuilderImpl<>( map );
    }

    private static class SingleValueBuilderImpl<M extends Map<K, V>, K, V>
        implements Builder<M, K, V>
    {
        protected final M map;

        private SingleValueBuilderImpl( M map )
        {
            this.map = map;
        }

        @Override
        public Builder<M, K, V> put( K key, V value )
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

    private static final class MultiValueBuilderImpl<M extends MultiValueMap<K, V>, K, V>
        extends SingleValueBuilderImpl<M, K, List<V>>
        implements MultiValueBuilder<M, K, V>
    {
        private MultiValueBuilderImpl( M map )
        {
            super( map );
        }

        @Override
        public MultiValueBuilder<M, K, V> add( K key, V value, V... moreValues )
        {
            map.add( key, value, moreValues );
            return this;
        }

        @Override
        public MultiValueBuilder<M, K, V> put( K key, List<V> values )
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

    private MapBuilder()
    {
    }
}
