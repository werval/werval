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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * Stacktraces utilities.
 */
public final class Stacktraces
{
    private static final class RecursivePredicate
        implements Predicate<Throwable>
    {
        private final Predicate<Throwable> predicate;

        private RecursivePredicate( Predicate<Throwable> predicate )
        {
            this.predicate = predicate;
        }

        @Override
        public boolean test( Throwable throwable )
        {
            if( predicate.test( throwable ) )
            {
                return true;
            }
            if( throwable.getCause() != null && test( throwable.getCause() ) )
            {
                return true;
            }
            for( Throwable suppressed : throwable.getSuppressed() )
            {
                if( test( suppressed ) )
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static final Pattern LINKS_PATTERN = Pattern.compile( "(?<left>.+\\()(?<file>.+\\..+):(?<line>[0-9]+)\\)" );
    private static final Pattern LINKS_PACKAGE_PATTERN = Pattern.compile( ".*at (?<packageName>.+)\\(" );

    public static Predicate<Throwable> containsEqual( Class<? extends Throwable> throwableClass )
    {
        ensureNotNull( "Throwable class", throwableClass );
        return new RecursivePredicate( (ex) -> Classes.equal( throwableClass ).test( ex.getClass() ) );
    }

    public static Predicate<Throwable> containsAssignable( Class<? extends Throwable> throwableClass )
    {
        ensureNotNull( "Throwable class", throwableClass );
        return new RecursivePredicate( (ex) -> Classes.assignable( throwableClass ).test( ex.getClass() ) );
    }

    public static Predicate<Throwable> containsMessage( String message )
    {
        ensureNotNull( "Message", message );
        return new RecursivePredicate( (ex) -> message.equals( ex.getMessage() ) );
    }

    public static Predicate<Throwable> containsInMessage( String string )
    {
        ensureNotNull( "String", string );
        return new RecursivePredicate( (ex) -> ex.getMessage() == null ? false : ex.getMessage().contains( string ) );
    }

    /**
     * URL Generator to use with {@link #toHtml(java.lang.Throwable, io.werval.util.Stacktraces.FileURLGenerator)}.
     */
    public interface FileURLGenerator
    {
        /**
         * Generate URL for the given filename and line number.
         *
         * @param packageName Package name
         * @param filename    File name
         * @param line        Line number
         *
         * @return URL for the given filename and line number, or null if not found
         */
        String urlFor( String packageName, String filename, int line );
    }

    /**
     * Null object that implements {@link FileURLGenerator}.
     */
    public static final class NullFileURLGenerator
        implements FileURLGenerator
    {
        @Override
        public String urlFor( String packageName, String filename, int line )
        {
            return null;
        }
    }

    public static String toString( Throwable throwable )
    {
        StringWriter traceWriter = new StringWriter();
        throwable.printStackTrace( new PrintWriter( traceWriter ) );
        return traceWriter.toString();
    }

    public static CharSequence toHtml( Throwable throwable, FileURLGenerator urlGen )
    {
        // Parameters
        ensureNotNull( "Throwable", throwable );
        ensureNotNull( "FileURLGenerator", urlGen );

        // Get trace
        String originalTrace = toString( throwable );

        // Add links
        StringWriter traceWriter = new StringWriter();
        Scanner scanner = new Scanner( originalTrace );
        while( scanner.hasNextLine() )
        {
            String line = scanner.nextLine();
            Matcher matcher = LINKS_PATTERN.matcher( line );
            if( matcher.matches() )
            {
                Matcher packageNameMatcher = LINKS_PACKAGE_PATTERN.matcher( matcher.group( "left" ) );
                packageNameMatcher.matches();
                String packageName = packageNameMatcher.group( "packageName" );
                packageName = packageName.substring( 0, Strings.lastIndexOfNth( packageName, 2, "." ) );
                String filename = matcher.group( "file" );
                String lineNumber = matcher.group( "line" );
                String url = urlGen.urlFor( packageName, filename, Integer.parseInt( lineNumber ) );
                if( Strings.isEmpty( url ) )
                {
                    traceWriter.append( line ).append( "\n" );
                }
                else
                {
                    traceWriter.append( matcher.group( "left" ) ).
                        append( "<a href=\"" ).append( url ).append( "\">" ).
                        append( filename ).append( ":" ).append( lineNumber ).append( "</a>)\n" );
                }
            }
            else
            {
                traceWriter.append( line ).append( "\n" );
            }
        }

        // Put in HTML container
        return new StringBuilder()
            .append( "<div class=\"werval-stacktrace\" style=\"white-space: pre; font-family: monospace\">\n" )
            .append( traceWriter.toString() )
            .append( "</div>\n" );
    }

    private Stacktraces()
    {
    }
}
