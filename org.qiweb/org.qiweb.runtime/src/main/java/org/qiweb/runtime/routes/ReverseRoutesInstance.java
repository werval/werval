package org.qiweb.runtime.routes;

import io.netty.handler.codec.http.QueryStringEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.codeartisans.java.toolbox.Strings;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.qiweb.api.Application;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.util.Comparators;

import static io.netty.util.CharsetUtil.UTF_8;
import static org.qiweb.api.controllers.Controller.request;
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
        NullArgumentException.ensureNotNull( "Controller call for Reverse Routing", outcome );
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
                return new ReverseRouteInstance( reverseOutcome.httpMethod(), unboundPath );
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
        private final Map<String, List<String>> appendedQueryString = new TreeMap<>( Comparators.LOWER_CASE );
        private String fragmentIdentifier;

        public ReverseRouteInstance( String method, String uri )
        {
            this.method = method;
            this.uri = uri;
        }

        @Override
        public String method()
        {
            return method;
        }

        @Override
        public String uri()
        {
            QueryStringEncoder encoder = new QueryStringEncoder( uri, UTF_8 );
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
            NullArgumentException.ensureNotEmpty( "key", key );
            NullArgumentException.ensureNotNull( "value", value );
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
            NullArgumentException.ensureNotNull( "parameters", parameters );
            for( Map.Entry<String, ?> entry : parameters.entrySet() )
            {
                String key = entry.getKey();
                NullArgumentException.ensureNotEmpty( "parameter key", key );
                NullArgumentException.ensureNotNull( "parameter value for '" + key + "'", entry.getValue() );
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
            absoluteUrl.append( "://" ).append( request().host() );
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
