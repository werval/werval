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
package org.qiweb.spi.dev;

import io.werval.api.exceptions.WervalException;
import io.werval.util.Stacktraces;

/**
 * Thrown if something goes wrong while rebuilding the Application in development mode.
 * <p>
 * See {@link DevShellSPI#rebuild()}.
 */
public class DevShellRebuildException
    extends WervalException
{
    private final String buildLog;

    public DevShellRebuildException( Throwable cause )
    {
        this( null, cause, null );
    }

    public DevShellRebuildException( String message, Throwable cause )
    {
        this( message, cause, null );
    }

    public DevShellRebuildException( Throwable cause, String buildLog )
    {
        this( null, cause, buildLog );
    }

    public DevShellRebuildException( String message, Throwable cause, String buildLog )
    {
        super( message == null ? "Rebuild error" : message, cause );
        this.buildLog = buildLog;
    }

    public String htmlErrorPage()
    {
        StringBuilder html = new StringBuilder();
        html.append( "<!DOCTYPE html>\\n<html>\n<head><title>Rebuild error: " )
            .append( getMessage() )
            .append( "</title></head>\n" )
            .append( "<body>\n<h1>Rebuild error: " )
            .append( getMessage() )
            .append( "</h1>\n" );
        if( buildLog != null )
        {
            html.append( "<h3>Build log</h3>\n" )
                .append( "<div class=\"qiweb-buildlog\" style=\"white-space: pre; font-family: monospace\">\n" )
                .append( buildLog )
                .append( "</div>\n" );
        }
        else
        {
            html.append( "<h3>StackTrace</h3>\n" )
                .append( "<div class=\"qiweb-stacktrace\" style=\"white-space: pre; font-family: monospace\">\n" )
                .append( Stacktraces.toString( getCause() ) )
                .append( "</div>\n" );
        }
        html.append( "</body>\n</html>\n" );
        return html.toString();
    }
}
