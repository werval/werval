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
package org.qiweb.runtime.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codeartisans.java.toolbox.Strings;

/**
 * Stacktraces utilities.
 */
public final class Stacktraces
{

    private static final Pattern LINKS_PATTERN = Pattern.compile( "(?<left>.+\\()(?<file>.+\\..+):(?<line>[0-9]+)\\)" );

    public interface FileURLGenerator
    {

        String urlFor( String filename, int line );
    }

    public static String toHtml( Throwable throwable, FileURLGenerator urlGen )
    {
        // Parameters
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

        // HTML container
        StringBuilder html = new StringBuilder();
        html.append( "<div class=\"" ).append( containerClassName ).append( "\" style=\"white-space: pre;\">\n" );
        html.append( traceWriter.toString() );
        html.append( "</div>\n" );

        return html.toString();
    }

    private Stacktraces()
    {
    }
}
