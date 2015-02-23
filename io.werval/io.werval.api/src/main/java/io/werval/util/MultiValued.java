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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MultiValued.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public interface MultiValued<K, V>
{
    /**
     * Check if this MultiValued has any value.
     *
     * @return return {@literal true} if this MultiValued contains no value at all, {@literal false} otherwise
     */
    boolean isEmpty();

    /**
     * Check if this MultiValued has at least one value for a given key.
     *
     * @param key Key
     *
     * @return return {@literal true} if this MultiValued has at least one value for the given {@literal key},
     *         {@literal false} otherwise
     */
    boolean has( K key );

    /**
     * Get single value, ensuring it has only one value.
     *
     * @param key Key
     *
     * @return Single value for this key
     *
     * @throws IllegalArgumentException if there is no or multiple values for this key
     */
    V singleValue( K key )
        throws IllegalArgumentException;

    /**
     * Get first value, ensuring there's one.
     *
     * @param key Key
     *
     * @return First value for this key
     *
     * @throws IllegalArgumentException if there are no value for this key
     */
    V firstValue( K key )
        throws IllegalArgumentException;

    /**
     * Get last value, ensuring there's one.
     *
     * @param key Key
     *
     * @return Last value for this key
     *
     * @throws IllegalArgumentException if there are no value for this key
     */
    V lastValue( K key )
        throws IllegalArgumentException;

    /**
     * Get single value.
     *
     * @param key Key
     *
     * @return Single value for this key, optional
     */
    Optional<V> singleValueOptional( K key );

    /**
     * Get first value.
     *
     * @param key Key
     *
     * @return First value for this key, optional
     */
    Optional<V> firstValueOptional( K key );

    /**
     * Get last value.
     *
     * @param key Key
     *
     * @return Last value for this key, optional
     */
    Optional<V> lastValueOptional( K key );

    /**
     * Get all keys.
     *
     * @return All keys as immutable Set.
     */
    Set<K> keys();

    /**
     * Get all values.
     *
     * @param key Key
     *
     * @return All values for the given key as immutable List, or an immutable empty one.
     */
    List<V> values( K key );

    /**
     * Get all single values, ensuring each has only one value.
     *
     * @return Every single values for all keys as immutable Map, or an empty immutable one.
     *
     * @throws IllegalStateException if there is multiple values for a parameter
     */
    Map<K, V> singleValues()
        throws IllegalStateException;

    /**
     * Get all first values.
     *
     * @return Every first values for all keys as immutable Map, or an empty immutable one.
     */
    Map<K, V> firstValues();

    /**
     * Get all last values.
     *
     * @return Every last values for all keys as immutable Map, or an empty immutable one.
     */
    Map<K, V> lastValues();

    /**
     * Get all values.
     *
     * @return Every all values for all keys as immutable Map, or an empty immutable one.
     */
    Map<K, List<V>> allValues();
}
