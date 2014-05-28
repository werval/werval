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
import java.util.LinkedHashMap;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.controllers.Classpath;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.mimeTypes;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;
import static org.qiweb.api.util.Charsets.UTF_8;
import static org.qiweb.api.util.InputStreams.BUF_SIZE_4K;
import static org.qiweb.api.util.InputStreams.readAllAsString;

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
                      + "<body><h1>Dynamic Documentation</h1>"
                      + "<p>Modules contains non-core functionnality. "
                      + "Modules are simple JARs and can contain controllers, utility classes and QiWeb Plugins.</p>"
                      + "<ul>";
        for( DynDoc dyndoc : dyndocs.values() )
        {
            String dyndocUrl = reverseRoutes().get( getClass(), c -> c.module( dyndoc.id ) ).httpUrl();
            html += "<li><a href=\"" + dyndocUrl + "\">" + dyndoc.name + "</a></li>";
        }
        html += "</ul></body></html>";
        return outcomes().ok( SiteMeshHelper.decorate( html ) ).asHtml().build();
    }

    public Outcome module( String id )
    {
        Map<String, DynDoc> dyndocs = discoverDynDocs( application() );
        DynDoc dyndoc = dyndocs.get( id );
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
        String decoratedHtml = SiteMeshHelper.decorate( html );
        return outcomes().ok( decoratedHtml ).asHtml().build();
    }

    /**
     * Dynamic Documentation.
     *
     * Dynamic documentations are declared in either a module's `reference.conf` or the `application.conf`.
     * <p>
     * Each dynamic documentation produce routes under the DevShell's `/@doc` umbrella that points to classpath
     * resources.
     */
    private static class DynDoc
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
        if( application.config().has( "qiweb.devshell.dyndocs" ) )
        {
            Config dyndocsConfig = application.config().object( "qiweb.devshell.dyndocs" );
            for( String id : dyndocsConfig.subKeys() )
            {
                Config dyndocConfig = dyndocsConfig.object( id );
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
