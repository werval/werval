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
package org.qiweb.api.outcomes;

import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Status;

import static org.qiweb.api.http.Headers.Names.ACCEPT;
import static org.qiweb.api.http.Headers.Names.VARY;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;
import static org.qiweb.api.mime.MimeTypes.TEXT_PLAIN;
import static org.qiweb.util.Strings.NEWLINE;
import static org.qiweb.util.Strings.SPACE;
import static org.qiweb.util.Strings.hasText;

/**
 * Default error outcomes utilities.
 *
 * @navassoc 1 create * OutcomeBuilder
 */
public final class DefaultErrorOutcomes
{
    /**
     * Build a default error outcome.
     * <p>
     * Respect content-type negociation to output a {@literal HTML}, {@literal JSON} or {@literal text/plain} outcome.
     *
     * @param request Request header or null, {@literal text/plain} will be used if null
     * @param status Outcome status or null, {@literal 500 Internal Server Error} will be used if null
     * @param title Error title or null/empty to use the status reason phrase
     * @param detail Error detail or null/empty if none
     * @param outcomes Outcomes factory, not null
     *
     * @return OutcomeBuilder
     */
    public static OutcomeBuilder errorOutcome(
        RequestHeader request, Status status, String title, String detail, Outcomes outcomes
    )
    {
        Status theStatus = status != null ? status : Status.INTERNAL_SERVER_ERROR;
        String theTitle = hasText( title ) ? title : theStatus.code() + SPACE + theStatus.reasonPhrase();
        String preferredMimeType = request != null ? request.preferredMimeType() : TEXT_PLAIN;

        OutcomeBuilder builder = outcomes.status( theStatus.code() );
        switch( preferredMimeType )
        {
            case APPLICATION_JSON:
                StringBuilder json = new StringBuilder();
                json.append( "{ \"code\": " ).append( theStatus.code() )
                    .append( ", \"title\": \"" ).append( theTitle ).append( "\"" ); // TODO JSON Escape!
                if( hasText( detail ) )
                {
                    json.append( ", \"detail\": \"" ).append( detail ).append( "\"" ); // TODO JSON Escape!
                }
                json.append( " }" );
                builder.withBody( json.toString() ).asJson();
                break;
            case TEXT_HTML:
                StringBuilder html = new StringBuilder();
                html.append( "<!DOCTYPE html>\n<html>\n<head><title>" )
                    .append( theTitle )
                    .append( "</title></head>\n" );
                html.append( "<body>\n<h1>" ).append( theTitle ).append( "</h1>\n" );
                if( hasText( detail ) )
                {
                    html.append( detail ).append( NEWLINE );
                }
                html.append( "</body>\n</html>\n" );
                builder.withBody( html.toString() ).asHtml();
                break;
            default:
                StringBuilder text = new StringBuilder();
                text.append( theTitle ).append( NEWLINE );
                if( hasText( detail ) )
                {
                    text.append( NEWLINE ).append( detail ).append( NEWLINE );
                }
                builder.withBody( text.toString() ).asTextPlain();
                break;
        }
        if( request != null )
        {
            builder.withHeader( VARY, ACCEPT );
        }
        return builder;
    }

    private DefaultErrorOutcomes()
    {
    }
}
