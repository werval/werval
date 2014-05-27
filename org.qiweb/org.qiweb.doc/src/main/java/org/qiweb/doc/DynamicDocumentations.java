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
package org.qiweb.doc;

import java.util.List;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.util.InputStreams;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Dynamic Documentations Controller.
 */
public class DynamicDocumentations
{
    public Outcome index()
    {
        StringBuilder menu = new StringBuilder();
        List<DynamicDocumentation> dyndocs = DynamicDocumentation.discover( application() );
        for( DynamicDocumentation dyndoc : dyndocs )
        {
            menu.append( "<li><a href=\"" ).append( dyndoc.id ).append( "\">" )
                .append( dyndoc.name ).append( "</a></li>\n" );
        }
        String html = new String(
            InputStreams.readAllBytes( getClass().getResourceAsStream( "dyndocs/index.html" ), 4096 ),
            UTF_8
        );
        String jquery = new String(
            InputStreams.readAllBytes( getClass().getResourceAsStream( "dyndocs/jquery-2.1.1.min.js" ), 4096 ),
            UTF_8
        );
        String historyjs = new String(
            InputStreams.readAllBytes( getClass().getResourceAsStream( "dyndocs/jquery.history.js" ), 4096 ),
            UTF_8
        );
        html = html.replace( "<!-- MENU -->", menu.toString() );
        html = html.replace( "// JQUERY", jquery );
        html = html.replace( "// HISTORY.JS", historyjs );
        return outcomes().ok( html ).asHtml().build();
    }
}
