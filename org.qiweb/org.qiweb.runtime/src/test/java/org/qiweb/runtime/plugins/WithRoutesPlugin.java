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
package org.qiweb.runtime.plugins;

import java.io.IOException;
import java.util.List;
import org.qiweb.api.Plugin;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.RouteBuilder;

import static java.util.Collections.singletonList;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.routes.RouteBuilder.p;

/**
 * WithRoutesPlugin.
 */
public class WithRoutesPlugin
    extends Plugin.Void
{
    public static class PluginController
    {
        public Outcome prepended( String test )
            throws IOException
        {
            return outcomes().ok( "prepended " + test ).build();
        }

        public Outcome appended( String test )
        {
            return outcomes().ok( "appended " + test ).build();
        }
    }

    @Override
    public List<Route> firstRoutes( RouteBuilder routeBuilder )
    {
        return singletonList( routeBuilder
            .route( "GET" )
            .on( "/prepended/:test" )
            .to( PluginController.class, c -> c.prepended( p( "test", String.class ) ) )
            .build()
        );
    }

    @Override
    public List<Route> lastRoutes( RouteBuilder routeBuilder )
    {
        return singletonList( routeBuilder
            .route( "GET" )
            .on( "/appended/:test" )
            .to( PluginController.class, c -> c.appended( p( "test", String.class ) ) )
            .build()
        );
    }
}
