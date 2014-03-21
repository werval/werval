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

import java.util.List;
import org.qiweb.api.Plugin;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.routes.RouteBuilder;

import static java.util.Collections.singletonList;
import static org.qiweb.api.context.CurrentContext.outcomes;

/**
 * WithRoutesPlugin.
 */
public class WithRoutesPlugin
    extends Plugin.Void
{
    public static class PluginController
    {
        public Outcome prepended()
        {
            return outcomes().ok( "prepended" ).build();
        }

        public Outcome appended()
        {
            return outcomes().ok( "appended" ).build();
        }
    }

    @Override
    public List<Route> firstRoutes()
    {
        return singletonList( RouteBuilder
            .route( "GET" )
            .on( "/prepended" )
            .to( PluginController.class, c -> c.prepended() )
            .newInstance()
        );
    }

    @Override
    public List<Route> lastRoutes()
    {
        return singletonList( RouteBuilder
            .route( "GET" )
            .on( "/appended" )
            .to( PluginController.class, c -> c.appended() )
            .newInstance()
        );
    }
}
