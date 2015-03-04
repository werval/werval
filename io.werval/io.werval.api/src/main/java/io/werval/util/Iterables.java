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
package io.werval.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Utilities to work with Iterables.
 */
public final class Iterables
{
    private static final class EmptyIterable
        implements Iterable
    {
        @Override
        public Iterator iterator()
        {
            return new EmptyIterator();
        }
    }

    private static final class EmptyIterator
        implements Iterator
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
    }

    private static final class SkipIterable<T>
        implements Iterable<T>
    {
        private final int skip;
        private final Iterable iterable;

        SkipIterable( int skip, Iterable iterable )
        {
            this.skip = skip;
            this.iterable = iterable;
        }

        @Override
        public Iterator iterator()
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
    }

    private static final class MapIterable<I, O>
        implements Iterable<O>
    {
        private final Iterable<I> input;
        private final Function<I, O> function;

        private MapIterable( Iterable<I> input, Function<I, O> function )
        {
            this.input = input;
            this.function = function;
        }

        @Override
        public Iterator<O> iterator()
        {
            return new MapIterator<>( input.iterator(), function );
        }
    }

    private static final class MapIterator<I, O>
        implements Iterator<O>
    {
        private final Iterator<I> input;
        private final Function<I, O> function;

        private MapIterator( Iterator<I> input, Function<I, O> function )
        {
            this.input = input;
            this.function = function;
        }

        @Override
        public boolean hasNext()
        {
            return input.hasNext();
        }

        @Override
        public O next()
        {
            return function.apply( input.next() );
        }
    }

    private static final Iterable EMPTY = new EmptyIterable();

    /**
     * Empty Iterable.
     *
     * @param <T> Parameterized item type
     *
     * @return An empty Iterable
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Iterable<T> empty()
    {
        return EMPTY;
    }

    /**
     * First item or null.
     *
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
     * Count the number of items.
     *
     * @param iterable Iterable
     *
     * @return Number of items in the Iterable
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
     * Skip some items.
     *
     * @param <T>      Parameterized item type
     * @param skip     How many items to skip
     * @param iterable Iterable
     *
     * @return An Iterable starting after skipped items
     */
    public static <T> Iterable<T> skip( final int skip, final Iterable<T> iterable )
    {
        return new SkipIterable<>( skip, iterable );
    }

    public static <I, O> Iterable<O> map( Iterable<I> input, Function<I, O> function )
    {
        return new MapIterable<>( input, function );
    }

    /**
     * Add all items of an Iterable into a Collection.
     *
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
     * Create an Iterable of given items.
     *
     * @param <T>   Parameterized item type
     * @param items Items to turn in an Iterable
     *
     * @return An Iterable of given items
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Iterable<T> asIterable( T... items )
    {
        return Arrays.asList( items );
    }

    /**
     * Create a new List with all items of an Iterable.
     *
     * @param <T>      Parameterized item type
     * @param iterable Iterable
     *
     * @return A new List with all items from the given Iterable
     */
    public static <T> List<T> toList( Iterable<T> iterable )
    {
        return addAll( new ArrayList<T>(), iterable );
    }

    public static <T, I extends Iterable<T>> I notEmptyOrNull( I iterable )
    {
        return iterable != null && count( iterable ) > 0 ? iterable : null;
    }

    private Iterables()
    {
    }
}
