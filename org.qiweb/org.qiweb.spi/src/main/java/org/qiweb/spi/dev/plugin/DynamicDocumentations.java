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
package org.qiweb.spi.dev.plugin;

import java.util.List;
import org.qiweb.api.outcomes.Outcome;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;

/**
 * Dynamic Documentations Controller.
 */
public class DynamicDocumentations
{
    public Outcome index()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(
            "<!doctype html>\n<html lang=\"en\">\n<head><title>Dynamic Documentations</title></head>\n<body>\n"
        );
        builder.append( "<ul>\n" );
        List<DynamicDocumentation> dyndocs = DynamicDocumentation.discover( application() );
        for( DynamicDocumentation dyndoc : dyndocs )
        {
            builder.append( "<li>\n" );
            builder.append( "<a href=\"/@doc/" ).append( dyndoc.id ).append( "\" " )
                .append( "title=\"" ).append( dyndoc.name ).append( "\">" );
            builder.append( dyndoc.name );
            builder.append( "</a>\n" );
            builder.append( "</li>\n" );
        }
        builder.append( "</ul>\n" );
        builder.append( "</body>\n</html>\n" );
        return outcomes().ok( builder.toString() ).asHtml().build();
    }
}
