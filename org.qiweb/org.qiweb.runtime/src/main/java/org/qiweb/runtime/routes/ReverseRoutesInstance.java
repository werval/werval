package org.qiweb.runtime.routes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.qiweb.api.Application;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;

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
                String unboundPath = route.unbindPath( application.pathBinders(), parameters );
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
            return uri;
        }

        @Override
        public String httpUrl( boolean secure )
        {
            return absoluteUrl( "http", secure );
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
            absoluteUrl.append( uri );
            return absoluteUrl.toString();
        }

        @Override
        public String toString()
        {
            return uri;
        }
    }
}
