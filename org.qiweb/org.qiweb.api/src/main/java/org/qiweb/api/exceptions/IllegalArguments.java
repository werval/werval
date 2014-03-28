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
package org.qiweb.api.exceptions;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Null or empty argument exception utilities.
 */
public final class IllegalArguments
{
    private static final String WAS_NULL = " was null.";
    private static final String WAS_EMPTY = " was empty.";
    private static final String WAS_ZERO = " was zero.";

    /**
     * Ensure not null.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null
     */
    public static void ensureNotNull( String name, Object value )
    {
        if( value != null )
        {
            return;
        }
        throw new IllegalArgumentException( name + WAS_NULL );
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, String value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, CharSequence value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param trim  Trim value before check if true, don't otherwise
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, boolean trim, String value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 || ( trim && value.trim().length() == 0 ) )
        {

            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, Object[] value )
    {
        ensureNotNull( name, value );
        if( value.length == 0 )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, Collection<?> value )
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, Properties value )
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensure not empty.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmpty( String name, Map<?, ?> value )
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new IllegalArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensures that the string array instance is not null and that it has entries that are not null or empty
     * either without trimming the string.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmptyContent( String name, String[] value )
    {
        ensureNotEmptyContent( name, false, value );
    }

    /**
     * Ensures that the string array instance is not null and that it has entries that are not null or empty.
     *
     * @param name  Name
     * @param trim  Trim flag
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void ensureNotEmptyContent( String name, boolean trim, String[] value )
    {
        ensureNotEmpty( name, value );
        for( int i = 0; i < value.length; i++ )
        {
            ensureNotEmpty( value[i] + "[" + i + "]", value[i] );
            if( trim )
            {
                ensureNotEmpty( value[i] + "[" + i + "]", value[i].trim() );
            }
        }
    }

    /**
     * Ensure not zero.
     *
     * @param name  Name
     * @param value Value
     *
     * @throws IllegalArgumentException if value is null or zero
     */
    public static void ensureNotZero( String name, Integer value )
    {
        ensureNotNull( name, value );
        if( value == 0 )
        {
            throw new IllegalArgumentException( name + WAS_ZERO );
        }
    }

    public static void ensureInRange( String name, Integer value, Integer from, Integer to )
    {
        ensureNotNull( name, value );
        if( value < from || value > to )
        {
            throw new IllegalArgumentException( name + " was not in range [" + from + "," + to + "]." );
        }
    }

    private IllegalArguments()
    {
    }
}
