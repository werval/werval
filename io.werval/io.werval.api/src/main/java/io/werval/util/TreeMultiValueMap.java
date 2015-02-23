/*
 * Copyright (c) 2014-2014 the original author or authors
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TreeMap MultiValueMap.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class TreeMultiValueMap<K, V>
    extends MultiValueMapAdapter<K, V>
{
    private static final long serialVersionUID = 1L;

    public TreeMultiValueMap()
    {
        super( new TreeMap<>() );
    }

    public TreeMultiValueMap( Comparator<K> comparator )
    {
        super( new TreeMap<>( comparator ) );
    }

    public TreeMultiValueMap( SortedMap<K, List<V>> map )
    {
        super( new TreeMap<>( map ) );
    }

    @Override
    protected Map<K, V> newFlattenedMap()
    {
        return new TreeMap<>( ( (TreeMap) internalMap ).comparator() );
    }
}
