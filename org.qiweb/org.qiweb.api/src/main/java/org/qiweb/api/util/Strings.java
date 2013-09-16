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
package org.qiweb.api.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Utilities to work with Strings.
 */
public final class Strings
{

    public static final String EMPTY = "";

    public static boolean isEmpty( String value )
    {
        return value == null || value.length() == 0;
    }

    public static String join( String[] strings )
    {
        return join( Arrays.asList( strings ) );
    }

    public static String join( String[] strings, String delimiter )
    {
        return join( Arrays.asList( strings ), delimiter );
    }

    public static String join( Iterable<? extends CharSequence> strings )
    {
        return join( strings, "" );
    }

    public static String join( Iterable<? extends CharSequence> strings, String delimiter )
    {
        int capacity = 0;
        int delimLength = delimiter.length();
        Iterator<? extends CharSequence> iter = strings.iterator();
        if( iter.hasNext() )
        {
            capacity += iter.next().length() + delimLength;
        }
        StringBuilder buffer = new StringBuilder( capacity );
        iter = strings.iterator();
        if( iter.hasNext() )
        {
            buffer.append( iter.next() );
            while( iter.hasNext() )
            {
                buffer.append( delimiter );
                buffer.append( iter.next() );
            }
        }
        return buffer.toString();
    }

    private Strings()
    {
    }
}
