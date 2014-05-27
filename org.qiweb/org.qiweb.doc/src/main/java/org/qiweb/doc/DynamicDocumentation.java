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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic Documentation.
 *
 * Dynamic documentations are declared in either a module's `reference.conf` or the `application.conf`.
 * <p>
 * Each dynamic documentation produce routes under the DevShell's `/@doc` umbrella that points to classpath resources.
 */
/* package */ final class DynamicDocumentation
{
    private static final Logger LOG = LoggerFactory.getLogger( DocumentationPlugin.class );

    /* package */ static Map<String, DynamicDocumentation> discover( Application application )
    {
        Map<String, DynamicDocumentation> map = new LinkedHashMap<>();
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
                map.put( id, new DynamicDocumentation( id, basePath, entryPoint, name ) );
            }
        }
        return map;
    }
    /* package */ final String id;
    /* package */ final String basePath;
    /* package */ final String entryPoint;
    /* package */ final String name;

    /* package */ DynamicDocumentation( String id, String basePath, String entryPoint, String name )
    {
        this.id = id;
        this.basePath = basePath;
        this.entryPoint = entryPoint;
        this.name = name;
    }

    List<Route> buildRoutes( RouteBuilder routeBuilder )
    {
        // Redirect /@doc/modules/{id} to /@doc/modules/{id}/{entry_point}
        StringBuilder redirect = new StringBuilder();
        redirect.append( "GET /@doc/modules/" ).append( id )
            .append( " org.qiweb.api.controllers.Default.seeOther( String url = '" )
            .append( "/@doc/modules/" ).append( id ).append( "/" ).append( entryPoint )
            .append( "' )" );
        StringBuilder index = new StringBuilder();
        // Serve all resources from {base_path} on /@doc/modules/{id}/*
        index.append( "GET /@doc/modules/" ).append( id ).append( "/*path" )
            .append( " org.qiweb.api.controllers.Classpath.resource( String basepath = '" )
            .append( basePath )
            .append( "', String path )" );
        return Arrays.asList(
            routeBuilder.parse().route( redirect.toString() ),
            routeBuilder.parse().route( index.toString() )
        );
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
