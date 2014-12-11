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
package io.werval.runtime.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Lazy Value.
 *
 * @param <T> Value type
 */
public final class Lazy<T>
    implements Supplier<T>
{
    public static <T> Lazy<T> of( Supplier<T> delegate )
    {
        return new Lazy<>( delegate );
    }

    public static <T> Lazy<Optional<T>> ofOptional( Supplier<T> delegate )
    {
        return new Lazy<>( () -> Optional.ofNullable( delegate.get() ) );
    }

    public static <T> Lazy<T> of( T value )
    {
        return new Lazy<>( () -> value );
    }

    public static <T> Lazy<Optional<T>> ofOptional( T value )
    {
        return new Lazy<>( () -> Optional.ofNullable( value ) );
    }

    public static <T> Lazy<T> of( Class<? extends T> type )
    {
        return new Lazy<>(
            () ->
            {
                try
                {
                    return (T) type.newInstance();
                }
                catch( InstantiationException | IllegalAccessException ex )
                {
                    throw new RuntimeException( "Unable to instanciate lazy object", ex );
                }
            }
        );
    }

    private final Supplier<T> delegate;
    private transient volatile boolean initialized;
    // "value" does not need to be volatile; visibility piggy-backs on volatile read of "initialized"
    private transient T value = null;

    private Lazy( Supplier<T> delegate )
    {
        this.delegate = delegate;
    }

    @Override
    public T get()
    {
        // 2-field variant of Double Checked Locking
        if( !initialized )
        {
            synchronized( this )
            {
                if( !initialized )
                {
                    value = delegate.get();
                    initialized = true;
                    return value;
                }
            }
        }
        return value;
    }

    @Override
    public String toString()
    {
        return "Lazy{" + "delegate=" + delegate + ", value=" + value + '}';
    }
}
