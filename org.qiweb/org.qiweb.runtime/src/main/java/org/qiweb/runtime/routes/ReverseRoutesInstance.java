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

import io.netty.handler.codec.http.QueryStringEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.qiweb.api.Application;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.controllers.Controller.request;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTP_PORT;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTPS_PORT;

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

    public static class ReverseRouteInstance
        implements ReverseRoute
    {

        private final String method;
        private final String uri;
        private final Charset charset;
        private final Map<String, List<String>> appendedQueryString = new TreeMap<>( Comparators.LOWER_CASE );
        private String fragmentIdentifier;

        public ReverseRouteInstance( String method, String uri, Charset charset )
        {
            this.method = method;
            this.uri = uri;
            this.charset = charset;
        }

        @Override
        public String method()
        {
            return method;
        }

        @Override
        public String uri()
        {
            QueryStringEncoder encoder = new QueryStringEncoder( uri, charset );
            for( Map.Entry<String, List<String>> entry : appendedQueryString.entrySet() )
            {
                String key = entry.getKey();
                for( String value : entry.getValue() )
                {
                    encoder.addParam( key, value );
                }
            }
            StringBuilder sb = new StringBuilder( encoder.toString() );
            if( !Strings.isEmpty( fragmentIdentifier ) )
            {
                sb.append( "#" ).append( fragmentIdentifier );
            }
            return sb.toString();
        }

        @Override
        public ReverseRoute appendQueryString( String key, String value )
        {
            ensureNotEmpty( "key", key );
            ensureNotNull( "value", value );
            if( !appendedQueryString.containsKey( key ) )
            {
                appendedQueryString.put( key, new ArrayList<String>() );
            }
            appendedQueryString.get( key ).add( value );
            return this;
        }

        @Override
        public ReverseRoute appendQueryString( Map<String, ?> parameters )
        {
            ensureNotNull( "parameters", parameters );
            for( Map.Entry<String, ?> entry : parameters.entrySet() )
            {
                String key = entry.getKey();
                ensureNotEmpty( "parameter key", key );
                ensureNotNull( "parameter value for '" + key + "'", entry.getValue() );
                if( !appendedQueryString.containsKey( key ) )
                {
                    appendedQueryString.put( key, new ArrayList<String>() );
                }
                if( entry.getValue() instanceof List )
                {
                    for( Object value : (List) entry.getValue() )
                    {
                        appendedQueryString.get( key ).add( value.toString() );
                    }
                }
                else
                {
                    appendedQueryString.get( key ).add( entry.getValue().toString() );
                }
            }
            return this;
        }

        @Override
        public ReverseRoute withFragmentIdentifier( String fragmentIdentifier )
        {
            this.fragmentIdentifier = fragmentIdentifier;
            return this;
        }

        @Override
        public String httpUrl()
        {
            return httpUrl( false );
        }

        @Override
        public String httpUrl( boolean secure )
        {
            return absoluteUrl( "http", secure );
        }

        @Override
        public String webSocketUrl()
        {
            return webSocketUrl( false );
        }

        @Override
        public String webSocketUrl( boolean secure )
        {
            return absoluteUrl( "ws", secure );
        }

        private String absoluteUrl( String protocol, boolean secure )
        {
            StringBuilder absoluteUrl = new StringBuilder( protocol );
            if( secure )
            {
                absoluteUrl.append( "s" );
            }
            absoluteUrl.append( "://" ).append( request().domain() );
            if( ( !secure && request().port() != DEFAULT_HTTP_PORT )
                || ( secure && request().port() != DEFAULT_HTTPS_PORT ) )
            {
                // With custom port
                absoluteUrl.append( ":" ).append( request().port() );
            }
            absoluteUrl.append( uri() );
            return absoluteUrl.toString();
        }

        @Override
        public String toString()
        {
            return uri();
        }
    }
}
