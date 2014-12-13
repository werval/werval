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

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Couple of Objects.
 *
 * @param <L> Left Object Type
 * @param <R> Right Object Type
 */
public final class Couple<L, R>
{
    /**
     * Create a new empty Couple.
     *
     * @param <L> Left Object Type
     * @param <R> Right Object Type
     *
     * @return A new empty Couple
     */
    public static <L, R> Couple<L, R> empty()
    {
        return new Couple<>( null, null );
    }

    /**
     * Create a new Couple with only the left object set.
     *
     * @param <L>  Left Object Type
     * @param <R>  Right Object Type
     * @param left Left Object
     *
     * @return A new Couple with only the left object set
     */
    public static <L, R> Couple<L, R> leftOnly( L left )
    {
        return new Couple<>( left, null );
    }

    /**
     * Create a new Couple with only the right object set.
     *
     * @param <L>   Left Object Type
     * @param <R>   Right Object Type
     * @param right Right Object
     *
     * @return A new Couple with only the right object set
     */
    public static <L, R> Couple<L, R> rightOnly( R right )
    {
        return new Couple<>( null, right );
    }

    /**
     * Create a new Couple with both the left and right objects set.
     *
     * @param <L>   Left Object Type
     * @param <R>   Right Object Type
     * @param left  Left Object
     * @param right Right Object
     *
     * @return A new Couple with both the left and right objects set
     */
    public static <L, R> Couple<L, R> of( L left, R right )
    {
        return new Couple<>( left, right );
    }

    private final L left;
    private final R right;

    private Couple( L left, R right )
    {
        this.left = left;
        this.right = right;
    }

    /**
     * @return Left Object or null if not set
     */
    public L left()
    {
        return left;
    }

    /**
     * @return Right Object or null if not set
     */
    public R right()
    {
        return right;
    }

    /**
     * @return {@literal true} if Left Object is not null, otherwise return {@literal false}
     */
    public boolean hasLeft()
    {
        return left != null;
    }

    /**
     * @return {@literal true} if Right Object is not null, otherwise return {@literal false}
     */
    public boolean hasRight()
    {
        return right != null;
    }

    /**
     * Create a new Couple with this Right Object and the given Left Object.
     *
     * @param <T>  Left Object Type
     * @param left Left Object
     *
     * @return A new Couple with this Right Object and the given Left Object
     */
    public <T> Couple<T, R> withLeft( T left )
    {
        return new Couple<>( left, right );
    }

    /**
     * Create a new Couple with this Left Object and the given Right Object.
     *
     * @param <T>   Right Object Type
     * @param right Right Object
     *
     * @return A new Couple with this Left Object and the given Right Object
     */
    public <T> Couple<L, T> withRight( T right )
    {
        return new Couple<>( left, right );
    }

    /**
     * Apply the given function to this Couple.
     *
     * @param <T>      Function Return Type
     * @param function Function to apply
     *
     * @return The result of the function applied to this Couple.
     */
    public <T> T apply( Function<Couple<L, R>, T> function )
    {
        return function.apply( this );
    }

    /**
     * Apply the given function to this Couple.
     *
     * @param <T>      Function Return Type
     * @param function Function to apply
     *
     * @return The result of the function applied to this Couple.
     */
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
