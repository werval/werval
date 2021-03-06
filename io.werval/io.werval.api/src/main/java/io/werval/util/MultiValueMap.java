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

/**
 * MultiValueMap.
 *
 * @param <K> Key Type
 * @param <V> Values Type
 */
public interface MultiValueMap<K, V>
    extends Map<K, List<V>>
{
    void add( K key, V value, V... moreValues );

    void setSingle( K key, V value );

    void setAll( Map<K, V> map );

    V getSingle( K key )
        throws IllegalStateException;

    V getFirst( K key );

    V getLast( K key );

    Map<K, V> toMapSingleValues()
        throws IllegalStateException;

    Map<K, V> toMapFirstValues();

    Map<K, V> toMapLastValues();
}
