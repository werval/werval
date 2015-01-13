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
package io.werval.doc;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.outcomes.Outcome;
import io.werval.controllers.Classpath;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.mimeTypes;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.reverseRoutes;
import static io.werval.api.mime.MimeTypes.TEXT_HTML;
import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.BUF_SIZE_4K;
import static io.werval.util.InputStreams.readAllAsString;

/**
 * Dynamic Documentations Controller.
 */
public class DynamicDocumentations
{
    private static final Logger LOG = LoggerFactory.getLogger( DynamicDocumentations.class );

    public Outcome index()
        throws IOException
    {
        Map<String, DynDoc> dyndocs = discoverDynDocs( application() );
        String html = "<!doctype html><html><head><title>Dynamic Documentation</title></head>"
                      + "<body><div id=\"header\"><h1>Dynamic Documentation</h1></div>"
                      + "<div id=\"content\">"
                      + "<p>Modules contains non-core functionnality. "
                      + "Modules are simple JARs and can contain controllers, utility classes and Plugins.</p>"
                      + "<ul>";
        for( DynDoc dyndoc : dyndocs.values() )
        {
            String dyndocUrl = reverseRoutes().get(
                getClass(),
                c -> c.resource( dyndoc.id, dyndoc.entryPoint )
            ).httpUrl();
            html += "<li><a href=\"" + dyndocUrl + "\">" + dyndoc.name + "</a></li>";
        }
        html += "</ul></div><div id=\"footer\"></div></body></html>";
        String decorated = SiteMeshHelper.decorate(
            reverseRoutes().get( getClass(), c -> c.index() ).uri(),
            html
        );
        return outcomes().ok( decorated ).asHtml().build();
    }

    public Outcome resource( String id, String path )
        throws IOException
    {
        Map<String, DynDoc> dyndocs = discoverDynDocs( application() );
        DynDoc dyndoc = dyndocs.get( id );
        if( dyndoc == null )
        {
            // Module not found
            return outcomes().notFound().asHtml().build();
        }
        String resourcePath = dyndoc.basePath + "/" + path;
        if( application().classLoader().getResource( resourcePath ) == null )
        {
            // Resource not found
            return outcomes().notFound().asHtml().build();
        }
        String mimetype = mimeTypes().ofPath( resourcePath );
        if( !TEXT_HTML.equals( mimetype ) )
        {
            // Do not decorate non-HTML files
            return new Classpath().resource( resourcePath );
        }
        String html = readAllAsString(
            application().classLoader().getResourceAsStream( resourcePath ),
            BUF_SIZE_4K,
            UTF_8
        );
        String decoratedHtml = SiteMeshHelper.decorate(
            reverseRoutes().get( getClass(), c -> c.resource( id, path ) ).uri(),
            html
        );
        return outcomes().ok( decoratedHtml ).asHtml().build();
    }

    private static final class DynDoc
    {
        private final String id;
        private final String basePath;
        private final String entryPoint;
        private final String name;

        private DynDoc( String id, String basePath, String entryPoint, String name )
        {
            this.id = id;
            this.basePath = basePath;
            this.entryPoint = entryPoint;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return "DynDoc{" + "id=" + id
                   + ", basePath=" + basePath
                   + ", entryPoint=" + entryPoint
                   + ", name=" + name + '}';
        }
    }

    private static Map<String, DynDoc> discoverDynDocs( Application application )
    {
        Map<String, DynDoc> map = new LinkedHashMap<>();
        if( application.config().has( "werval.devshell.dyndocs" ) )
        {
            Config dyndocsConfig = application.config().atPath( "werval.devshell.dyndocs" );
            for( String id : dyndocsConfig.subKeys() )
            {
                Config dyndocConfig = dyndocsConfig.atKey( id );
                if( !dyndocConfig.has( "base_path" ) )
                {
                    LOG.warn(
                        "Dynamic Documentation for '{}' will not be registered as no 'base_path' is defined.",
                        id
                    );
                    break;
                }
                String basePath = dyndocConfig.string( "base_path" );
                String entryPoint = dyndocConfig.has( "entry_point" )
                                    ? dyndocConfig.string( "entry_point" )
                                    : "index.html";
                String entryPointResource = basePath + "/" + entryPoint;
                if( application.classLoader().getResource( entryPointResource ) == null )
                {
                    LOG.warn(
                        "Dynamic Documentation for '{}' will not be served as '{}' can not be found.",
                        id, entryPointResource
                    );
                    break;
                }
                String name = dyndocConfig.has( "name" ) ? dyndocConfig.string( "name" ) : id;
                map.put( id, new DynDoc( id, basePath, entryPoint, name ) );
            }
        }
        return map;
    }
}
