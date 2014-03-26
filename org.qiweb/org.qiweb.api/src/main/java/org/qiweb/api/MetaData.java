/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.api;

import java.util.LinkedHashMap;

/**
 * Meta-Data.
 */
public final class MetaData
    extends LinkedHashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @param <T>   Parameterized meta-data type
     * @param type  Requested meta-data type
     * @param key   Key
     * @return      The value to which the specified key is mapped,
     *              or {@code null} if this map contains no mapping for the key.
     * @throws ClassCastException if the MetaData at the given key is not assignable to the given type.
     */
    public <T> T get( Class<T> type, String key )
    {
        Object value = get( key );
        if( value == null )
        {
            return null;
        }
        return type.cast( value );
    }

    /**
     * Returns a shallow copy of this <tt>MetaData</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this MetaData
     */
    @Override
    @SuppressWarnings( "CloneDoesntCallSuperClone" )
    public Object clone()
    {
        MetaData result = new MetaData();
        result.putAll( this );
        return result;
    }
}
