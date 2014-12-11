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

import io.werval.api.Mode;
import io.werval.api.Plugin;
import io.werval.api.routes.Route;
import io.werval.api.routes.RouteBuilder;
import java.util.List;

/**
 * Documentation Plugin.
 *
 * Provide routes that serve HTML documentation and Javadocs at {@literal /@doc} and configuration and version
 * information as JSON respectively at {@literal /@config} and {@literal /@version}.
 * <p>
 * Due to DevShell classloading mechanism, this should not use Javassist hence use Route parsing instead of the API.
 * Moreover, this class shall not be put in `org.qiweb.devshell`, nor `io.werval.spi.dev` because the loading of classes
 * in theses packages is done in the original DevShell classloader hierarchy, not in the application one.
 */
public class DocumentationPlugin
    extends Plugin.Void
{
    @Override
    public List<Route> firstRoutes( Mode mode, RouteBuilder routeBuilder )
    {
        return routeBuilder.parse().routes(
            "GET /@config io.werval.controllers.Introspect.config",
            "GET /@version io.werval.controllers.Introspect.version",
            "GET /@doc org.qiweb.doc.CoreDocumentations.index",
            "GET /@doc/modules/index.html org.qiweb.doc.DynamicDocumentations.index",
            "GET /@doc/modules/:id/*path org.qiweb.doc.DynamicDocumentations.resource( String id, String path )",
            "GET /@doc/*path org.qiweb.doc.CoreDocumentations.catchAll( String path )"
        );
    }
}
