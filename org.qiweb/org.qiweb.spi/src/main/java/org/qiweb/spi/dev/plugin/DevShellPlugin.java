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

import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.Application;
import org.qiweb.api.Mode;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteBuilder;

/**
 * DevShell Plugin.
 *
 * Provide routes that serve HTML documentation and Javadocs at {@literal /@doc} and configuration and version
 * information as JSON respectively at {@literal /@config} and {@literal /@version}.
 * <p>
 * Due to DevShell classloading mechanism, this should not use Javassist hence use Route parsing instead of the API.
 * Moreover, this class shall not be put in `org.qiweb.devshell`, nor `org.qiweb.spi.dev` because the loading of classes
 * of theses packages has to be done in the original DevShell classloader hierarchy.
 */
public class DevShellPlugin
    extends Plugin.Void
{
    private List<DynamicDocumentation> dynamicDocumentations;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        dynamicDocumentations = DynamicDocumentation.discover( application );
    }

    @Override
    public List<Route> firstRoutes( Mode mode, RouteBuilder routeBuilder )
    {
        List<Route> devRoutes = new ArrayList<>();

        // Setup base and core documentation routes
        devRoutes.addAll( routeBuilder.parse().routes(
            "GET /@config org.qiweb.api.controllers.Introspect.config",
            "GET /@version org.qiweb.api.controllers.Introspect.version",
            "GET /@doc org.qiweb.api.controllers.Default.seeOther( String url = '/@doc/index.html' )",
            "GET /@doc/api org.qiweb.api.controllers.Default.seeOther( String url = '/@doc/api/index.html' )"
        ) );

        // Setup dynamic documentation routes
        for( DynamicDocumentation dynamicDocumentation : dynamicDocumentations )
        {
            devRoutes.addAll( dynamicDocumentation.buildRoutes( routeBuilder ) );
        }

        // Setup /@doc/*path route last as fallback for everything under /@doc
        devRoutes.add( routeBuilder.parse().route(
            "GET /@doc/*path org.qiweb.api.controllers.Classpath.resource( String basepath = 'org/qiweb/doc/html', String path )"
        ) );

        // Done!
        return devRoutes;
    }

    @Override
    public void onPassivate( Application application )
    {
        dynamicDocumentations = null;
    }
}
