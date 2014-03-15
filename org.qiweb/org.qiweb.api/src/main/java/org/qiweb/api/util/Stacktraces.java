/**
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
package org.qiweb.api.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotNull;

/**
 * Stacktraces utilities.
 */
public final class Stacktraces
{
    private static class RecursivePredicate
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
     * URL Generator to use with {@link #toHtml(java.lang.Throwable, org.qiweb.api.util.Stacktraces.FileURLGenerator)}.
     */
    public interface FileURLGenerator
    {
        /**
         * Generate URL for the given filename and line number.
         * 
         * @param filename File name
         * @param line Line number
         * @return URL for the given filename and line number, or null if not found
         */
        String urlFor( String filename, int line );
    }

    /**
     * Null object that implements {@link FileURLGenerator}.
     */
    public static final class NullFileURLGenerator
        implements FileURLGenerator
    {
        @Override
        public String urlFor( String filename, int line )
        {
            return null;
        }
    }

    public static CharSequence toHtml( Throwable throwable, FileURLGenerator urlGen )
    {
        // Parameters
        ensureNotNull( "Throwable", throwable );
        ensureNotNull( "FileURLGenerator", urlGen );
        String containerClassName = "qiweb-stacktrace";

        // Get trace
        StringWriter traceWriter = new StringWriter();
        throwable.printStackTrace( new PrintWriter( traceWriter ) );
        String trace = traceWriter.toString();

        // Add links
        traceWriter = new StringWriter();
        Scanner scanner = new Scanner( trace );
        while( scanner.hasNextLine() )
        {
            String line = scanner.nextLine();
            Matcher matcher = LINKS_PATTERN.matcher( line );
            if( matcher.matches() )
            {
                String filename = matcher.group( "file" );
                String lineNumber = matcher.group( "line" );
                String url = urlGen.urlFor( filename, Integer.valueOf( lineNumber ) );
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
        StringBuilder html = new StringBuilder();
        html.append( "<div class=\"" ).append( containerClassName ).append( "\" style=\"white-space: pre;\">\n" );
        html.append( traceWriter.toString() );
        html.append( "</div>\n" );

        return html;
    }

    private Stacktraces()
    {
    }
}
