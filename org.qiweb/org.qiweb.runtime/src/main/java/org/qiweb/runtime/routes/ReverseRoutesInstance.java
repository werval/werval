/**
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseOutcome;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;

public class ReverseRoutesInstance
    extends ReverseRoutes
{

    private final Application application;

    public ReverseRoutesInstance( Application application )
    {
        this.application = application;
    }

    @Override
    public ReverseRoute of( Outcome outcome )
    {
        ensureNotNull( "Controller call for Reverse Routing", outcome );
        if( !( outcome instanceof ReverseOutcome ) )
        {
            throw new IllegalArgumentException( "Bad API usage! Given Outcome is not an instance of ReverseOutcome." );
        }
        ReverseOutcome reverseOutcome = (ReverseOutcome) outcome;
        List<Class<?>> parametersTypes = new ArrayList<>();
        for( Object param : reverseOutcome.parameters() )
        {
            parametersTypes.add( param.getClass() );
        }
        for( Route route : application.routes() )
        {
            if( route.httpMethod().equals( reverseOutcome.httpMethod() )
                && route.controllerType().getName().equals( reverseOutcome.controllerType().getName() )
                && route.controllerMethodName().equals( reverseOutcome.controllerMethod().getName() )
                && Arrays.equals( route.controllerMethod().getParameterTypes(), parametersTypes.toArray() ) )
            {
                // Found!
                RouteInstance instance = (RouteInstance) route;
                Map<String, Object> parameters = new LinkedHashMap<>();
                int idx = 0;
                for( String paramName : instance.controllerParams().names() )
                {
                    parameters.put( paramName, reverseOutcome.parameters().get( idx ) );
                    idx++;
                }
                String unboundPath = route.unbindParameters( application.parameterBinders(), parameters );
                return new ReverseRouteInstance( reverseOutcome.httpMethod(), unboundPath,
                                                 application.defaultCharset() );
            }
        }
        throw new RouteNotFoundException( reverseOutcome.httpMethod(),
                                          reverseOutcome.controllerType() + "."
                                          + reverseOutcome.controllerMethod()
                                          + "(" + parametersTypes + ")" );
    }

}
