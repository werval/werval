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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.http.QueryString;
import org.qiweb.runtime.routes.ControllerParams.ControllerParam;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.Route;

import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.ensureNotEmpty;
import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.ensureNotNull;
import static org.qi4j.functional.Iterables.addAll;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.matchesAny;
import static org.qi4j.functional.Iterables.toList;
import static org.qi4j.functional.Specifications.in;

/**
 * Instance of a Route.
 */
/* package */ final class RouteInstance
    implements Route
{

    private final String httpMethod;
    private final String path;
    private final Class<?> controllerType;
    private Method controllerMethod;
    private final String controllerMethodName;
    private final ControllerParams controllerParams;
    private final Set<String> modifiers;
    private final Pattern pathRegex;

    /* package */ RouteInstance( String httpMethod, String path,
                   Class<?> controllerType,
                   String controllerMethodName,
                   ControllerParams controllerParams,
                   Set<String> modifiers )
    {
        ensureNotNull( "HTTP Method", httpMethod );
        ensureNotEmpty( "Path", path );
        ensureNotNull( "Controller Type", controllerType );
        ensureNotEmpty( "Controller Method Name", controllerMethodName );
        this.httpMethod = httpMethod;
        this.path = path;
        this.controllerType = controllerType;
        this.controllerMethodName = controllerMethodName;
        this.controllerParams = controllerParams;
        this.modifiers = new LinkedHashSet<>( modifiers );
        validateRoute();
        this.pathRegex = Pattern.compile( generatePathRegex() );
    }

    private void validateRoute()
    {
        if( !path.startsWith( "/" ) )
        {
            throw new IllegalRouteException( toString(), "Invalid path: " + path );
        }

        Iterable<String> controllerParamsNames = controllerParams.names();
        List<String> pathParamsNames = toList( map( new Function<String, String>()
        {
            @Override
            public String map( String from )
            {
                return from.substring( 1 );
            }
        }, filter( new Specification<String>()
        {
            @Override
            public boolean satisfiedBy( String pathSegment )
            {
                return pathSegment.startsWith( ":" ) || pathSegment.startsWith( "*" );
            }
        }, iterable( path.substring( 1 ).split( "/" ) ) ) ) );

        // Disallow multiple occurences of a single parameter name in the path
        for( String paramName : pathParamsNames )
        {
            if( Collections.frequency( pathParamsNames, paramName ) > 1 )
            {
                throw new IllegalRouteException( toString(),
                                                 "Parameter '" + paramName + "' is present several times in path." );
            }
        }

        // Ensure all parameters in path are bound to controller parameters and "vice et versa".
        // Allow params absent from path that should come form QueryString
        Set<String> allParamsNames = new LinkedHashSet<>();
        addAll( allParamsNames, controllerParamsNames );
        addAll( allParamsNames, pathParamsNames );
        for( String paramName : allParamsNames )
        {
            if( !matchesAny( in( paramName ), controllerParamsNames ) )
            {
                throw new IllegalRouteException( toString(),
                                                 "Parameter '" + paramName + "' is present in path but not bound." );
            }
        }

        // Ensure controller method exists and return an Outcome
        Class<?>[] controllerParamsTypes = controllerParams.types();
        try
        {
            controllerMethod = controllerType.getMethod( controllerMethodName, controllerParamsTypes );
            if( !controllerMethod.getReturnType().isAssignableFrom( Outcome.class ) )
            {
                throw new IllegalRouteException( toString(), "Controller Method '" + controllerType.getSimpleName()
                                                             + "#" + controllerMethodName
                                                             + "( " + Arrays.toString( controllerParamsTypes ) + " )' "
                                                             + "do not return an Outcome." );
            }
        }
        catch( NoSuchMethodException ex )
        {
            throw new IllegalRouteException( toString(), "Controller Method '" + controllerType.getSimpleName()
                                                         + "#" + controllerMethodName
                                                         + "( " + Arrays.toString( controllerParamsTypes ) + " )' "
                                                         + "not found.", ex );
        }
    }

    /**
     * @return Regex for parameter extraction from path with one named group per path parameter
     */
    private String generatePathRegex()
    {
        StringBuilder regex = new StringBuilder( "/" );
        String[] pathElements = path.substring( 1 ).split( "/" );
        for( int idx = 0; idx < pathElements.length; idx++ )
        {
            String pathElement = pathElements[idx];
            if( pathElement.length() > 1 )
            {
                switch( pathElement.substring( 0, 1 ) )
                {
                    case ":":
                        regex.append( "(?<" ).append( pathElement.substring( 1 ) ).append( ">[^/]+)" );
                        break;
                    case "*":
                        regex.append( "(?<" ).append( pathElement.substring( 1 ) ).append( ">.+)" );
                        break;
                    default:
                        regex.append( pathElement );
                        break;
                }
            }
            else
            {
                regex.append( pathElement );
            }
            if( idx + 1 < pathElements.length )
            {
                regex.append( "/" );
            }
        }
        return regex.toString();
    }

    @Override
    public boolean satisfiedBy( RequestHeader requestHeader )
    {
        if( !httpMethod.equals( requestHeader.method() ) )
        {
            return false;
        }
        return pathRegex.matcher( requestHeader.path() ).matches();
    }

    @Override
    public String httpMethod()
    {
        return httpMethod;
    }

    @Override
    public String path()
    {
        return path;
    }

    @Override
    public Class<?> controllerType()
    {
        return controllerType;
    }

    @Override
    public Method controllerMethod()
    {
        return controllerMethod;
    }

    @Override
    public String controllerMethodName()
    {
        return controllerMethodName;
    }

    @Override
    public Set<String> modifiers()
    {
        return Collections.unmodifiableSet( modifiers );
    }

    @Override
    public Map<String, Object> bindParameters( ParameterBinders parameterBinders, String path, QueryString queryString )
    {
        Matcher matcher = pathRegex.matcher( path );
        if( !matcher.matches() )
        {
            throw new IllegalArgumentException( "Unable to bind, Route is not satisfied by path: " + path );
        }
        Map<String, Object> boundParams = new LinkedHashMap<>();
        for( ControllerParam param : controllerParams )
        {
            if( param.hasForcedValue() )
            {
                boundParams.put( param.name(), param.forcedValue() );
            }
            else
            {
                String unboundValue = null;
                try
                {
                    unboundValue = matcher.group( param.name() );
                }
                catch( IllegalArgumentException noMatchingGroupInPath )
                {
                    if( queryString.names().contains( param.name() ) )
                    {
                        // FIXME first or last value!
                        unboundValue = queryString.singleValue( param.name() );
                    }
                }
                if( unboundValue == null )
                {
                    throw new IllegalArgumentException( "Parameter named '" + param.name()
                                                        + "' not found in path nor in query string." );
                }
                boundParams.put( param.name(), parameterBinders.bind( param.type(), param.name(), unboundValue ) );
            }
        }
        return boundParams;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String unbindParameters( ParameterBinders parameterBinders, Map<String, Object> parameters )
    {
        List<String> paramsToUnbind = Iterables.toList( controllerParams.names() );
        StringBuilder unboundPath = new StringBuilder( "/" );

        // Unbinding path
        String[] pathElements = path.substring( 1 ).split( "/" );
        for( int idx = 0; idx < pathElements.length; idx++ )
        {
            String pathElement = pathElements[idx];
            if( pathElement.length() > 1 )
            {
                switch( pathElement.substring( 0, 1 ) )
                {
                    case ":":
                    case "*":
                        ControllerParam controllerParam = controllerParams.get( pathElement.substring( 1 ) );
                        Object value = controllerParam.hasForcedValue()
                                       ? controllerParam.forcedValue()
                                       : parameters.get( controllerParam.name() );
                        unboundPath.append( parameterBinders.unbind( (Class<Object>) controllerParam.type(),
                                                                     controllerParam.name(),
                                                                     value ) );
                        paramsToUnbind.remove( controllerParam.name() );
                        break;
                    default:
                        unboundPath.append( pathElement );
                        break;
                }
            }
            else
            {
                unboundPath.append( pathElement );
            }
            if( idx + 1 < pathElements.length )
            {
                unboundPath.append( "/" );
            }
        }

        // Unbinding query string
        if( !paramsToUnbind.isEmpty() )
        {
            unboundPath.append( "?" );
            Iterator<String> qsit = paramsToUnbind.iterator();
            while( qsit.hasNext() )
            {
                String qsParam = qsit.next();
                ControllerParam controllerParam = controllerParams.get( qsParam );
                Object value = controllerParam.hasForcedValue()
                               ? controllerParam.forcedValue()
                               : parameters.get( controllerParam.name() );
                unboundPath.append( qsParam ).append( "=" );
                unboundPath.append( parameterBinders.unbind( (Class<Object>) controllerParam.type(),
                                                             controllerParam.name(),
                                                             value ) );
                if( qsit.hasNext() )
                {
                    unboundPath.append( "&" );
                }
            }
        }

        return unboundPath.toString();
    }

    public ControllerParams controllerParams()
    {
        return controllerParams;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( httpMethod ).append( " " ).append( path ).append( " " ).
            append( controllerType.getName() ).append( "." ).append( controllerMethodName ).append( "(" );
        Iterator<ControllerParam> it = controllerParams.iterator();
        if( it.hasNext() )
        {
            sb.append( " " );
            while( it.hasNext() )
            {
                ControllerParam param = it.next();
                sb.append( param.type().getSimpleName() ).append( " " ).append( param.name() );
                if( param.hasForcedValue() )
                {
                    sb.append( " = '" ).append( param.forcedValue() ).append( "'" );
                }
                if( it.hasNext() )
                {
                    sb.append( ", " );
                }
            }
            sb.append( " " );
        }
        sb.append( ")" );
        Iterator<String> modifiersIt = modifiers.iterator();
        if( modifiersIt.hasNext() )
        {
            sb.append( " " );
            while( modifiersIt.hasNext() )
            {
                sb.append( modifiersIt.next() );
                if( modifiersIt.hasNext() )
                {
                    sb.append( " " );
                }
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        // CHECKSTYLE:OFF
        int hash = 7;
        hash = 59 * hash + ( this.httpMethod != null ? this.httpMethod.hashCode() : 0 );
        hash = 59 * hash + ( this.path != null ? this.path.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerType != null ? this.controllerType.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerMethodName != null ? this.controllerMethodName.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerParams != null ? this.controllerParams.hashCode() : 0 );
        hash = 59 * hash + ( this.modifiers != null ? this.modifiers.hashCode() : 0 );
        return hash;
        // CHECKSTYLE:ON
    }

    @Override
    @SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
    public boolean equals( Object obj )
    {
        // CHECKSTYLE:OFF
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final RouteInstance other = (RouteInstance) obj;
        if( this.httpMethod != other.httpMethod && ( this.httpMethod == null || !this.httpMethod.equals( other.httpMethod ) ) )
        {
            return false;
        }
        if( ( this.path == null ) ? ( other.path != null ) : !this.path.equals( other.path ) )
        {
            return false;
        }
        if( this.controllerType != other.controllerType && ( this.controllerType == null || !this.controllerType.equals( other.controllerType ) ) )
        {
            return false;
        }
        if( ( this.controllerMethodName == null ) ? ( other.controllerMethodName != null ) : !this.controllerMethodName.equals( other.controllerMethodName ) )
        {
            return false;
        }
        if( this.controllerParams != other.controllerParams && ( this.controllerParams == null || !this.controllerParams.equals( other.controllerParams ) ) )
        {
            return false;
        }
        if( this.modifiers != other.modifiers && ( this.modifiers == null || !this.modifiers.equals( other.modifiers ) ) )
        {
            return false;
        }
        return true;
        // CHECKSTYLE:ON
    }
}
