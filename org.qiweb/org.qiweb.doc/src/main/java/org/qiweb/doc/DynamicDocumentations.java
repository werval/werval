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

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Map;
import org.qiweb.api.controllers.Classpath;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.util.InputStreams;
import org.sitemesh.builder.SiteMeshOfflineBuilder;
import org.sitemesh.offline.SiteMeshOffline;
import org.sitemesh.offline.directory.Directory;
import org.sitemesh.offline.directory.InMemoryDirectory;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.mimeTypes;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;
import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Dynamic Documentations Controller.
 */
public class DynamicDocumentations
{
    public Outcome index()
        throws IOException
    {
        Map<String, DynamicDocumentation> dyndocs = DynamicDocumentation.discover( application() );
        String html = "<!doctype html><html><head><title>Dynamic Documentation</title><body><h1>Dynamic Documentation</h1></body></html>";
        return outcomes().ok( decorate( html ) ).asHtml().build();
    }

    public Outcome module( String id )
    {
        Map<String, DynamicDocumentation> dyndocs = DynamicDocumentation.discover( application() );
        DynamicDocumentation dyndoc = dyndocs.get( id );
        if( dyndoc == null )
        {
            return outcomes().notFound().asHtml().build();
        }
        ReverseRoute redirect = reverseRoutes().get( getClass(), c -> c.resource( dyndoc.id, dyndoc.entryPoint ) );
        return outcomes().seeOther( redirect.httpUrl() ).build();
    }

    public Outcome resource( String id, String path )
        throws IOException
    {
        Map<String, DynamicDocumentation> dyndocs = DynamicDocumentation.discover( application() );
        DynamicDocumentation dyndoc = dyndocs.get( id );
        if( dyndoc == null )
        {
            return outcomes().notFound().asHtml().build();
        }
        String resourcePath = dyndoc.basePath + "/" + path;
        if( application().classLoader().getResource( resourcePath ) == null )
        {
            return outcomes().notFound().asHtml().build();
        }
        String mimetype = mimeTypes().ofPath( resourcePath );
        if( TEXT_HTML.equals( mimetype ) )
        {
            String html = new String(
                InputStreams.readAllBytes( application().classLoader().getResourceAsStream( resourcePath ), 4096 ),
                UTF_8
            );
            String decorated = decorate( html );
            return outcomes().ok( decorated ).asHtml().build();
        }
        return new Classpath().resource( resourcePath );
    }

    private String decorate( String html )
        throws IOException
    {
        Directory source = new InMemoryDirectory( UTF_8 );
        Directory destination = new InMemoryDirectory( UTF_8 );

        String decorator = "<!doctype html>\n"
                           + "<html>\n"
                           + "  <head>\n"
                           + "    <title><sitemesh:write property='title'/></title>\n"
                           + "    <sitemesh:write property='head'></sitemesh:write>\n"
                           + "  </head>\n"
                           + "  <body>\n"
                           + "    <div id=\"qiweb-doc-header\">QiWeb Documentation Global Header</div>\n"
                           + "    <sitemesh:write property='body'/>\n"
                           + "  </body>\n"
                           + "</html>";
        source.save( "decorator.html", CharBuffer.wrap( decorator.toCharArray() ) );

        SiteMeshOffline sitemesh = new SiteMeshOfflineBuilder()
            .setSourceDirectory( source )
            .setDestinationDirectory( destination )
            .addDecoratorPath( "/*", "decorator.html" )
            .create();

        CharBuffer result = sitemesh.processContent( "/anypath", CharBuffer.wrap( html.toCharArray() ) );

        return result.toString();
    }

}
