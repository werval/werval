/*
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime.routes;

import java.util.List;
import org.qiweb.api.Application;
import org.qiweb.api.routes.Route;

/**
 * RouteProvider that parse routes from a string.
 */
public class RoutesParserProvider
    implements RoutesProvider
{
    private final String routesString;

    /**
     * Empty routes provider.
     */
    public RoutesParserProvider()
    {
        this( "" );
    }

    public RoutesParserProvider( String routesString )
    {
        this.routesString = routesString;
    }

    @Override
    public List<Route> routes( Application application )
    {
        return RouteBuilder.parseRoutes( application, routesString );
    }
}
