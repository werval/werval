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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;
import static io.werval.util.IllegalArguments.ensureGreaterOrEqual;

/**
 * Utilities to work with Strings.
 */
public final class Strings
{
    /**
     * Empty string.
     */
    public static final String EMPTY = "";
    /**
     * Space.
     */
    public static final String SPACE = " ";
    /**
     * Tabulation.
     */
    public static final String TAB = "\t";
    /**
     * New line.
     */
    public static final String NEWLINE = "\n";
    /**
     * ….
     */
    public static final String ETC = "…";
    /**
     * Empty {@literal char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = EMPTY.toCharArray();

    /**
     * Check if a String is null or empty.
     *
     * @param value String
     *
     * @return TRUE if the String is null or empty, otherwise return FALSE
     */
    public static boolean isEmpty( String value )
    {
        return value == null || value.length() == 0;
    }

    /**
     * Check if a String is not null nor empty.
     *
     * @param value String
     *
     * @return TRUE if the String is not null nor empty, otherwise return FALSE
     */
    public static boolean hasText( String value )
    {
        return value != null && value.length() > 0;
    }

    /**
     * Null a String if it is empty.
     *
     * @param value String
     *
     * @return {@literal null} if the String is {@literal null} or empty, the original String otherwise
     */
    public static String hasTextOrNull( String value )
    {
        return value == null || value.length() == 0 ? null : value;
    }

    /**
     * Join strings.
     *
     * @param strings Strings to join
     *
     * @return Joined String
     */
    public static String join( String[] strings )
    {
        return join( Arrays.asList( strings ) );
    }

    /**
     * Join strings with a given delimiter.
     *
     * @param strings   Strings to join
     * @param delimiter Delimiter
     *
     * @return Joined String
     */
    public static String join( String[] strings, String delimiter )
    {
        return join( Arrays.asList( strings ), delimiter );
    }

    /**
     * Join strings.
     *
     * @param strings Strings to join
     *
     * @return Joined String
     */
    public static String join( Iterable<? extends CharSequence> strings )
    {
        return join( strings, EMPTY );
    }

    /**
     * Join strings with a given delimiter.
     *
     * @param strings   Strings to join
     * @param delimiter Delimiter
     *
     * @return Joined String
     */
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

    /**
     * Right pad a given string with spaces.
     *
     * If the given String length is greater than {@literal length}, the String is returned as is.
     *
     * @param length Total length of the padded String
     * @param string String to pad
     *
     * @return Right padded String
     *
     * @throws IllegalArgumentException if {@literal length} is negative
     */
    public static String rightPad( int length, String string )
    {
        return rightPad( length, string, ' ' );
    }

    /**
     * Right pad a given string.
     *
     * If the given String length is greater than {@literal length}, the String is returned as is.
     *
     * @param length Total length of the padded String
     * @param string String to pad
     * @param pad    Character to use for padding
     *
     * @return Right padded String
     *
     * @throws IllegalArgumentException if {@literal length} is negative
     */
    public static String rightPad( int length, String string, char pad )
    {
        ensureGreaterOrEqual( "length", length, 0 );
        if( isEmpty( string ) )
        {
            return repeat( pad, length );
        }
        if( string.length() > length )
        {
            return string;
        }
        return string + repeat( pad, length - string.length() );
    }

    /**
     * Left pad a given string with spaces.
     *
     * If the given String length is greater than {@literal length}, the String is returned as is.
     *
     * @param length Total length of the padded String
     * @param string String to pad
     *
     * @return Left padded String
     *
     * @throws IllegalArgumentException if {@literal length} is negative
     */
    public static String leftPad( int length, String string )
    {
        return leftPad( length, string, ' ' );
    }

    /**
     * Left pad a given string.
     *
     * If the given String length is greater than {@literal length}, the String is returned as is.
     *
     * @param length Total length of the padded String
     * @param string String to pad
     * @param pad    Character to use for padding
     *
     * @return Left padded String
     *
     * @throws IllegalArgumentException if {@literal length} is negative
     */
    public static String leftPad( int length, String string, char pad )
    {
        ensureGreaterOrEqual( "length", length, 0 );
        if( isEmpty( string ) )
        {
            return repeat( pad, length );
        }
        if( string.length() > length )
        {
            return string;
        }
        return repeat( pad, length - string.length() ) + string;
    }

    /**
     * Repeat a character.
     *
     * @param character Character to repeat
     * @param times     Times to repeat
     *
     * @return A String of {@literal times} length that contains only {@literal character}.
     */
    public static String repeat( char character, int times )
    {
        StringBuilder sb = new StringBuilder();
        for( int index = 0; index < times; index++ )
        {
            sb.append( character );
        }
        return sb.toString();
    }

    /**
     * Repeat a String.
     *
     * @param characters Characters to repeat
     * @param times      Times to repeat
     *
     * @return A String of {@literal characters.length()*times} length that contains repeated {@literal characters}.
     */
    public static String repeat( CharSequence characters, int times )
    {
        StringBuilder sb = new StringBuilder();
        for( int index = 0; index < times; index++ )
        {
            sb.append( characters );
        }
        return sb.toString();
    }

    /**
     * Indent a String with two spaces levels.
     *
     * @param input String to indent
     * @param level Indent levels count
     *
     * @return Indented String
     */
    public static String indentTwoSpaces( String input, int level )
    {
        try
        {
            return indentTwoSpaces( new StringReader( input ), level );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    /**
     * Indent a String with tabulation levels.
     *
     * @param input String to indent
     * @param level Indent levels count
     *
     * @return Indented String
     */
    public static String indentTab( String input, int level )
    {
        try
        {
            return indentTab( new StringReader( input ), level );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    /**
     * Indent a String.
     *
     * @param input String to indent
     * @param level Indent levels count
     * @param tab   Level String
     *
     * @return Indented String
     */
    public static String indent( String input, int level, String tab )
    {
        try
        {
            return indent( new StringReader( input ), level, tab, EMPTY );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    /**
     * Indent a String using a prefix.
     *
     * @param input  String to indent
     * @param level  Indent levels count
     * @param tab    Level String
     * @param prefix Prefix
     *
     * @return Indented String
     */
    public static String indent( String input, int level, String tab, String prefix )
    {
        try
        {
            return indent( new StringReader( input ), level, tab, prefix );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    /**
     * Read a String and indent it with two spaces levels.
     *
     * @param input Reader input
     * @param level Indent levels count
     *
     * @return Indented String
     *
     * @throws IOException If unable to read input
     */
    public static String indentTwoSpaces( Reader input, int level )
        throws IOException
    {
        return indent( input, level, "  " );
    }

    /**
     * Read a String and indent it with tabulation levels.
     *
     * @param input Reader input
     * @param level Indent levels count
     *
     * @return Indented String
     *
     * @throws IOException If unable to read input
     */
    public static String indentTab( Reader input, int level )
        throws IOException
    {
        return indent( input, level, TAB );
    }

    /**
     * Read a String and indent it.
     *
     * @param input Reader input
     * @param level Indent levels count
     * @param tab   Level String
     *
     * @return Indented String
     *
     * @throws IOException If unable to read input
     */
    public static String indent( Reader input, int level, String tab )
        throws IOException
    {
        return indent( input, level, tab, EMPTY );
    }

    /**
     * Read a String and indent it using a prefix.
     *
     * @param input  Reader input
     * @param level  Indent levels count
     * @param tab    Level String
     * @param prefix Prefix
     *
     * @return Indented String
     *
     * @throws IOException If unable to read input
     */
    public static String indent( Reader input, int level, String tab, String prefix )
        throws IOException
    {
        BufferedReader reader = new BufferedReader( input );
        StringBuilder output = new StringBuilder();
        try
        {

            String eachLine = reader.readLine();
            if( !isEmpty( eachLine ) )
            {
                appendIndent( output, level, tab ).append( prefix ).append( eachLine );
                while( ( eachLine = reader.readLine() ) != null )
                {
                    output.append( NEWLINE );
                    if( !isEmpty( eachLine ) )
                    {
                        appendIndent( output, level, tab ).append( prefix ).append( eachLine );
                    }
                }
            }
            return output.toString();

        }
        finally
        {
            Closeables.closeSilently( reader );
        }
    }

    private static StringBuilder appendIndent( StringBuilder output, int level, String tab )
    {
        for( int indent = 0; indent < level; indent++ )
        {
            output.append( tab );
        }
        return output;
    }

    public static String withTrail( String input, String trail )
    {
        return input.endsWith( trail ) ? input : input + trail;
    }

    public static String withHead( String input, String head )
    {
        return input.startsWith( head ) ? input : head + input;
    }

    public static String withoutTrail( String input, String trail )
    {
        return input.endsWith( trail ) ? input.substring( 0, input.length() - trail.length() ) : input;
    }

    public static String withoutHead( String input, String head )
    {
        return input.startsWith( head ) ? input.substring( head.length() ) : input;
    }

    public static int indexOfNth( String string, int count, String substring )
    {
        requireNonNull( string, "String" );
        ensureGreaterOrEqual( "N", count, 1 );
        requireNonNull( substring, "Sub String" );
        int index = -1;
        int nextFromIndex = 0;
        for( int loop = 0; loop < count; loop++ )
        {
            int loopIndex = string.indexOf( substring, nextFromIndex );
            if( loopIndex < 0 )
            {
                return -1;
            }
            index = loopIndex;
            nextFromIndex = loopIndex + substring.length();
        }
        return index;
    }

    public static int lastIndexOfNth( String string, int count, String substring )
    {
        requireNonNull( string, "String" );
        ensureGreaterOrEqual( "N", count, 1 );
        requireNonNull( substring, "Sub String" );
        String inner = string;
        int index = -1;
        for( int loop = 0; loop < count; loop++ )
        {
            index = inner.lastIndexOf( substring );
            if( index < 0 )
            {
                break;
            }
            inner = inner.substring( 0, index );
        }
        return index;
    }

    private Strings()
    {
    }
}
