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
import org.qiweb.api.controllers.Classpath;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseRoute;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.mimeTypes;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;
import static org.qiweb.api.util.Charsets.UTF_8;
import static org.qiweb.api.util.InputStreams.BUF_SIZE_4K;
import static org.qiweb.api.util.InputStreams.readAllAsString;

/**
 * Core Documentations Controller.
 */
public class CoreDocumentations
{
    public Outcome index()
    {
        ReverseRoute redirect = reverseRoutes().get( getClass(), c -> c.catchAll( "index.html" ) );
        return outcomes().seeOther( redirect.httpUrl() ).build();
    }

    public Outcome api()
    {
        ReverseRoute redirect = reverseRoutes().get( getClass(), c -> c.catchAll( "api/index.html" ) );
        return outcomes().seeOther( redirect.httpUrl() ).build();
    }

    public Outcome catchAll( String path )
        throws IOException
    {
        String resourcePath = "org/qiweb/doc/html/" + path;
        if( path.startsWith( "api/" ) )
        {
            // Do not decorate javadocs
            return new Classpath().resource( resourcePath );
        }
        if( application().classLoader().getResource( resourcePath ) == null )
        {
            // Not found
            return outcomes().notFound().asHtml().build();
        }
        if( !TEXT_HTML.equals( mimeTypes().ofPath( resourcePath ) ) )
        {
            // Do not decorate non-HTML files
            return new Classpath().resource( resourcePath );
        }
        String html = readAllAsString(
            application().classLoader().getResourceAsStream( resourcePath ),
            BUF_SIZE_4K,
            UTF_8
        );
        String decorated = SiteMeshHelper.decorate(
            reverseRoutes().get( getClass(), c -> c.catchAll( path ) ).uri(),
            html,
            reverseRoutes()
        );
        return outcomes().ok( decorated ).asHtml().build();
    }
}
