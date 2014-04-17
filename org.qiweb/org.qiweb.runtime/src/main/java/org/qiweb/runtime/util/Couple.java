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

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Couple.
 *
 * @param <L> Left Type
 * @param <R> Right Type
 */
public final class Couple<L, R>
{
    public static <L, R> Couple<L, R> of( L left, R right )
    {
        return new Couple<>( left, right );
    }

    private final L left;
    private final R right;

    public Couple( L left, R right )
    {
        this.left = left;
        this.right = right;
    }

    public L left()
    {
        return left;
    }

    public R right()
    {
        return right;
    }

    public boolean hasLeft()
    {
        return left != null;
    }

    public boolean hasRight()
    {
        return right != null;
    }

    public <T> Couple<T, R> withLeft( T left )
    {
        return new Couple<>( left, right );
    }

    public <T> Couple<L, T> withRight( T right )
    {
        return new Couple<>( left, right );
    }

    public <T> T apply( Function<Couple<L, R>, T> function )
    {
        return function.apply( this );
    }

    public <T> T apply( BiFunction<L, R, T> function )
    {
        return function.apply( left, right );
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode( this.left );
        hash = 17 * hash + Objects.hashCode( this.right );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final Couple<?, ?> other = (Couple<?, ?>) obj;
        if( !Objects.equals( this.left, other.left ) )
        {
            return false;
        }
        return Objects.equals( this.right, other.right );
    }

    @Override
    public String toString()
    {
        return "Couple{" + "left=" + left + ", right=" + right + '}';
    }
}
