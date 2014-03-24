/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.runtime.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utilities to work with Iterables.
 */
public final class Iterables
{
    private static final Iterable EMPTY = new Iterable()
    {
        private final Iterator iterator = new Iterator()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public Object next()
            {
                throw new NoSuchElementException();
            }

            @Override
            public void remove()
            {
            }
        };

        @Override
        public Iterator iterator()
        {
            return iterator;
        }
    };

    /**
     * @param <T> Parameterized item type
     *
     * @return An empty iterable
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Iterable<T> empty()
    {
        return EMPTY;
    }

    /**
     * @param <T>      Parameterized item type
     * @param iterable Iterable
     *
     * @return First item or null
     */
    public static <T> T first( Iterable<T> iterable )
    {
        Iterator<T> it = iterable.iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * @param iterable Iterable
     *
     * @return Number of items in the iterable
     */
    public static long count( Iterable<?> iterable )
    {
        long count = 0;
        for( Object item : iterable )
        {
            count++;
        }
        return count;
    }

    /**
     * @param <T>      Parameterized item type
     * @param skip     How many items to skip
     * @param iterable Iterable
     *
     * @return An iterable starting after skipped items
     */
    public static <T> Iterable<T> skip( final int skip, final Iterable<T> iterable )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                Iterator<T> iterator = iterable.iterator();

                for( int i = 0; i < skip; i++ )
                {
                    if( iterator.hasNext() )
                    {
                        iterator.next();
                    }
                    else
                    {
                        return Iterables.<T>empty().iterator();
                    }
                }

                return iterator;
            }
        };
    }

    /**
     * @param <T>        Parameterized item type
     * @param <C>        Parameterized collection type
     * @param collection Collection to add to
     * @param iterable   Items to add to the given collection
     *
     * @return Given collection
     */
    public static <T, C extends Collection<T>> C addAll( C collection, Iterable<? extends T> iterable )
    {
        for( T item : iterable )
        {
            collection.add( item );
        }
        return collection;
    }

    /**
     * @param <T>   Parameterized item type
     * @param items Items to turn in an Iterable
     *
     * @return An Iterable of given items
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Iterable<T> iterable( T... items )
    {
        return Arrays.asList( items );
    }

    /**
     * @param <T>      Parameterized item type
     * @param iterable Iterable
     *
     * @return A new List with all items from the given iterable
     */
    public static <T> List<T> toList( Iterable<T> iterable )
    {
        return addAll( new ArrayList<T>(), iterable );
    }

    private Iterables()
    {
    }
}
