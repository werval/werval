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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * UnmodifiableMultiValueMap.
 */
final class UnmodifiableMultiValueMap<K, V>
    implements MultiValueMap<K, V>, Serializable
{
    private static final long serialVersionUID = 1L;
    private final MultiValueMap<? extends K, ? extends V> delegate;
    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, List<V>>> entrySet;
    private transient Collection<List<V>> values;

    UnmodifiableMultiValueMap( MultiValueMap<? extends K, ? extends V> delegate )
    {
        Objects.requireNonNull( delegate, "Original MultiValueMap" );
        this.delegate = delegate;
    }

    @Override
    public void add( K key, V value, V... moreValues )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSingle( K key, V value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAll( Map<K, V> map )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getSingle( K key )
    {
        return ( (MultiValueMap<K, V>) delegate ).getSingle( key );
    }

    @Override
    public V getFirst( K key )
    {
        return ( (MultiValueMap<K, V>) delegate ).getFirst( key );
    }

    @Override
    public V getLast( K key )
    {
        return ( (MultiValueMap<K, V>) delegate ).getLast( key );
    }

    @Override
    public Map<K, V> toMapSingleValues()
    {
        return ( (MultiValueMap<K, V>) delegate ).toMapSingleValues();
    }

    @Override
    public Map<K, V> toMapFirstValues()
    {
        return ( (MultiValueMap<K, V>) delegate ).toMapFirstValues();
    }

    @Override
    public Map<K, V> toMapLastValues()
    {
        return ( (MultiValueMap<K, V>) delegate ).toMapLastValues();
    }

    @Override
    public int size()
    {
        return delegate.size();
    }

    @Override
    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey( Object key )
    {
        return delegate.containsKey( key );
    }

    @Override
    public boolean containsValue( Object value )
    {
        return delegate.containsValue( value );
    }

    @Override
    public List<V> get( Object key )
    {
        return ( (Map<K, List<V>>) delegate ).get( key );
    }

    @Override
    public List<V> put( K key, List<V> value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> remove( Object key )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll( Map<? extends K, ? extends List<V>> m )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        delegate.clear();
    }

    @Override
    public Set<K> keySet()
    {
        if( keySet == null )
        {
            keySet = Collections.unmodifiableSet( delegate.keySet() );
        }
        return keySet;
    }

    @Override
    public Collection<List<V>> values()
    {
        if( values == null )
        {
            values = Collections.unmodifiableCollection( ( (Map<K, List<V>>) delegate ).values() );
        }
        return values;
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet()
    {
        if( entrySet == null )
        {
            entrySet = new UnmodifiableEntrySet<>( ( (Map<K, List<V>>) delegate ).entrySet() );
        }
        return entrySet;
    }

    @Override
    public List<V> getOrDefault( Object key, List<V> defaultValue )
    {
        // Safe cast as we don't change the value
        return ( (Map<K, List<V>>) delegate ).getOrDefault( key, defaultValue );
    }

    @Override
    public void forEach( BiConsumer<? super K, ? super List<V>> action )
    {
        // Safe cast as we don't change the value
        ( (Map<K, List<V>>) delegate ).forEach( action );
    }

    @Override
    public void replaceAll( BiFunction<? super K, ? super List<V>, ? extends List<V>> function )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> putIfAbsent( K key, List<V> value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove( Object key, Object value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace( K key, List<V> oldValue, List<V> newValue )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> replace( K key, List<V> value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> computeIfAbsent( K key, Function<? super K, ? extends List<V>> mappingFunction )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> computeIfPresent( K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> compute( K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> merge( K key, List<V> value, BiFunction<? super List<V>, ? super List<V>, ? extends List<V>> remappingFunction )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    @Override @SuppressWarnings( value = "EqualsWhichDoesntCheckParameterClass" )
    public boolean equals( Object obj )
    {
        return delegate.equals( obj );
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

    /**
     * We need this class in addition to UnmodifiableSet as
     * Map.Entries themselves permit modification of the backing Map
     * via their setValue operation. This class is subtle: there are
     * many possible attacks that must be thwarted.
     *
     * @serial include
     */
    @SuppressWarnings( value = "EqualsAndHashcode" )
    static class UnmodifiableEntrySet<K, V>
        extends UnmodifiableSet<Map.Entry<K, V>>
    {
        private static final long serialVersionUID = 7854390611657943733L;

        UnmodifiableEntrySet( Set<? extends Map.Entry<? extends K, ? extends V>> s )
        {
            // Need to cast to raw in order to work around a limitation in the type system
            super( (Set) s );
        }

        static <K, V> Consumer<Map.Entry<K, V>> entryConsumer( Consumer<? super Entry<K, V>> action )
        {
            return (java.util.Map.Entry<K, V> e) -> action.accept( new UnmodifiableEntry<>( e ) );
        }

        @Override
        public void forEach( Consumer<? super Entry<K, V>> action )
        {
            Objects.requireNonNull( action );
            c.forEach( entryConsumer( action ) );
        }

        static final class UnmodifiableEntrySetSpliterator<K, V>
            implements Spliterator<Entry<K, V>>
        {
            final Spliterator<Map.Entry<K, V>> s;

            UnmodifiableEntrySetSpliterator( Spliterator<Entry<K, V>> s )
            {
                this.s = s;
            }

            @Override
            public boolean tryAdvance( Consumer<? super Entry<K, V>> action )
            {
                Objects.requireNonNull( action );
                return s.tryAdvance( entryConsumer( action ) );
            }

            @Override
            public void forEachRemaining( Consumer<? super Entry<K, V>> action )
            {
                Objects.requireNonNull( action );
                s.forEachRemaining( entryConsumer( action ) );
            }

            @Override
            public Spliterator<Entry<K, V>> trySplit()
            {
                Spliterator<Entry<K, V>> split = s.trySplit();
                return split == null ? null : new UnmodifiableEntrySetSpliterator<>( split );
            }

            @Override
            public long estimateSize()
            {
                return s.estimateSize();
            }

            @Override
            public long getExactSizeIfKnown()
            {
                return s.getExactSizeIfKnown();
            }

            @Override
            public int characteristics()
            {
                return s.characteristics();
            }

            @Override
            public boolean hasCharacteristics( int characteristics )
            {
                return s.hasCharacteristics( characteristics );
            }

            @Override
            public Comparator<? super Entry<K, V>> getComparator()
            {
                return s.getComparator();
            }
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator()
        {
            return new UnmodifiableEntrySetSpliterator<>( (Spliterator<Map.Entry<K, V>>) c.spliterator() );
        }

        @Override
        public Stream<Entry<K, V>> stream()
        {
            return StreamSupport.stream( spliterator(), false );
        }

        @Override
        public Stream<Entry<K, V>> parallelStream()
        {
            return StreamSupport.stream( spliterator(), true );
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new Iterator<Map.Entry<K, V>>()
            {
                private final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = c.iterator();

                @Override
                public boolean hasNext()
                {
                    return i.hasNext();
                }

                @Override
                public Map.Entry<K, V> next()
                {
                    return new UnmodifiableEntry<>( i.next() );
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Object[] toArray()
        {
            Object[] a = c.toArray();
            for( int i = 0; i < a.length; i++ )
            {
                a[i] = new UnmodifiableEntry<>( (Map.Entry<? extends K, ? extends V>) a[i] );
            }
            return a;
        }

        @Override
        public <T> T[] toArray( T[] a )
        {
            // We don't pass a to c.toArray, to avoid window of
            // vulnerability wherein an unscrupulous multithreaded client
            // could get his hands on raw (unwrapped) Entries from c.
            @SuppressWarnings( value = "SuspiciousToArrayCall" )
            Object[] arr = c.toArray( a.length == 0 ? a : Arrays.copyOf( a, 0 ) );
            for( int i = 0; i < arr.length; i++ )
            {
                arr[i] = new UnmodifiableEntry<>( (Map.Entry<? extends K, ? extends V>) arr[i] );
            }
            if( arr.length > a.length )
            {
                return (T[]) arr;
            }
            System.arraycopy( arr, 0, a, 0, arr.length );
            if( a.length > arr.length )
            {
                a[arr.length] = null;
            }
            return a;
        }

        /**
         * This method is overridden to protect the backing set against
         * an object with a nefarious equals function that senses
         * that the equality-candidate is Map.Entry and calls its
         * setValue method.
         */
        @Override
        public boolean contains( Object o )
        {
            if( !( o instanceof Map.Entry ) )
            {
                return false;
            }
            return c.contains( new UnmodifiableEntry<>( (Map.Entry<?, ?>) o ) );
        }

        /**
         * The next two methods are overridden to protect against
         * an unscrupulous List whose contains(Object o) method senses
         * when o is a Map.Entry, and calls o.setValue.
         */
        @Override @SuppressWarnings( value = "element-type-mismatch" )
        public boolean containsAll( Collection<?> coll )
        {
            // Invokes safe contains() above
            for( Object e : coll )
            {
                if( !contains( e ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals( Object o )
        {
            if( o == this )
            {
                return true;
            }
            if( !( o instanceof Set ) )
            {
                return false;
            }
            Set<?> s = (Set<?>) o;
            if( s.size() != c.size() )
            {
                return false;
            }
            return containsAll( s ); // Invokes safe containsAll() above
        }

        /**
         * This "wrapper class" serves two purposes: it prevents
         * the client from modifying the backing Map, by short-circuiting
         * the setValue method, and it protects the backing Map against
         * an ill-behaved Map.Entry that attempts to modify another
         * Map Entry when asked to perform an equality check.
         */
        private static class UnmodifiableEntry<K, V>
            implements Map.Entry<K, V>
        {
            private final Map.Entry<? extends K, ? extends V> entry;

            private UnmodifiableEntry( Map.Entry<? extends K, ? extends V> entry )
            {
                this.entry = Objects.requireNonNull( entry );
            }

            @Override
            public K getKey()
            {
                return entry.getKey();
            }

            @Override
            public V getValue()
            {
                return entry.getValue();
            }

            @Override
            public V setValue( V value )
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode()
            {
                return entry.hashCode();
            }

            @Override
            public boolean equals( Object o )
            {
                if( this == o )
                {
                    return true;
                }
                if( !( o instanceof Map.Entry ) )
                {
                    return false;
                }
                Map.Entry<?, ?> t = (Map.Entry<?, ?>) o;
                return Objects.equals( entry.getKey(), t.getKey() ) && Objects.equals( entry.getValue(), t.getValue() );
            }

            @Override
            public String toString()
            {
                return entry.toString();
            }
        }
    }

    static class UnmodifiableSet<E>
        extends UnmodifiableCollection<E>
        implements Set<E>, Serializable
    {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet( Set<? extends E> s )
        {
            super( s );
        }

        @Override @SuppressWarnings( value = "EqualsWhichDoesntCheckParameterClass" )
        public boolean equals( Object o )
        {
            return o == this || c.equals( o );
        }

        @Override
        public int hashCode()
        {
            return c.hashCode();
        }
    }

    static class UnmodifiableCollection<E>
        implements Collection<E>, Serializable
    {
        private static final long serialVersionUID = 1820017752578914078L;
        final Collection<? extends E> c;

        UnmodifiableCollection( Collection<? extends E> c )
        {
            if( c == null )
            {
                throw new NullPointerException();
            }
            this.c = c;
        }

        @Override
        public int size()
        {
            return c.size();
        }

        @Override
        public boolean isEmpty()
        {
            return c.isEmpty();
        }

        @Override
        public boolean contains( Object o )
        {
            return c.contains( o );
        }

        @Override
        public Object[] toArray()
        {
            return c.toArray();
        }

        @Override @SuppressWarnings( value = "SuspiciousToArrayCall" )
        public <T> T[] toArray( T[] a )
        {
            return c.toArray( a );
        }

        @Override
        public String toString()
        {
            return c.toString();
        }

        @Override
        public Iterator<E> iterator()
        {
            return new Iterator<E>()
            {
                private final Iterator<? extends E> i = c.iterator();

                @Override
                public boolean hasNext()
                {
                    return i.hasNext();
                }

                @Override
                public E next()
                {
                    return i.next();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void forEachRemaining( Consumer<? super E> action )
                {
                    // Use backing collection version
                    i.forEachRemaining( action );
                }
            };
        }

        @Override
        public boolean add( E e )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove( Object o )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll( Collection<?> coll )
        {
            return c.containsAll( coll );
        }

        @Override
        public boolean addAll( Collection<? extends E> coll )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll( Collection<?> coll )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll( Collection<?> coll )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        // Override default methods in Collection
        @Override
        public void forEach( Consumer<? super E> action )
        {
            c.forEach( action );
        }

        @Override
        public boolean removeIf( Predicate<? super E> filter )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Spliterator<E> spliterator()
        {
            return (Spliterator<E>) c.spliterator();
        }

        @Override
        public Stream<E> stream()
        {
            return (Stream<E>) c.stream();
        }

        @Override
        public Stream<E> parallelStream()
        {
            return (Stream<E>) c.parallelStream();
        }
    }
}
