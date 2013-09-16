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
package org.qiweb.api.exceptions;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Null or empty argument exception.
 */
public final class NullArgumentException
    extends IllegalArgumentException
{

    private static final long serialVersionUID = 1L;
    private static final String WAS_NULL = " was null.";
    private static final String WAS_EMPTY = " was empty.";
    private static final String WAS_ZERO = " was zero.";

    private NullArgumentException( String message )
    {
        super( message );
    }

    public static void ensureNotNull( String name, Object value )
    {
        if( value != null )
        {
            return;
        }
        throw new NullArgumentException( name + WAS_NULL );
    }

    public static void ensureNotEmpty( String name, String value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, CharSequence value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, boolean trim, String value )
    {
        ensureNotNull( name, value );
        if( value.length() == 0 || ( trim && value.trim().length() == 0 ) )
        {

            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, Object[] value )
    {
        ensureNotNull( name, value );
        if( value.length == 0 )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, Collection<?> value )
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, Properties value )
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    public static void ensureNotEmpty( String name, Map<?, ?> value )
        throws NullArgumentException
    {
        ensureNotNull( name, value );
        if( value.isEmpty() )
        {
            throw new NullArgumentException( name + WAS_EMPTY );
        }
    }

    /**
     * Ensures that the string array instance is not null and that it has entries that are not null or empty
     * either without trimming the string.
     */
    public static void ensureNotEmptyContent( String name, String[] value )
    {
        ensureNotEmptyContent( name, false, value );
    }

    /**
     * Ensures that the string array instance is not null and that it has entries that are not null or empty.
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

    public static void ensureNotZero( String name, Integer value )
    {
        ensureNotNull( name, value );
        if( value == 0 )
        {
            throw new NullArgumentException( name + WAS_ZERO );
        }
    }
}
