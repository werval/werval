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
package io.werval.runtime.routes;

import io.werval.api.exceptions.IllegalRouteException;
import io.werval.api.exceptions.ParameterBinderException;
import io.werval.api.http.Method;
import io.werval.api.http.QueryString;
import io.werval.api.http.RequestHeader;
import io.werval.api.outcomes.Outcome;
import io.werval.api.routes.ControllerParams;
import io.werval.api.routes.ControllerParams.ParamValue;
import io.werval.api.routes.ParameterBinders;
import io.werval.api.routes.Route;
import io.werval.runtime.util.TypeResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.IllegalArguments.ensureNotNull;
import static io.werval.util.Iterables.addAll;
import static io.werval.util.Iterables.toList;
import static io.werval.util.Strings.SPACE;
import static io.werval.util.Strings.rightPad;

/**
 * Instance of a Route.
 */
/* package */ final class RouteInstance
    implements Route
{
    private final Method httpMethod;
    private final String path;
    private final Class<?> controllerType;
    private java.lang.reflect.Method controllerMethod;
    private final String controllerMethodName;
    private final ControllerParams controllerParams;
    private final Set<String> modifiers;
    private final Pattern pathRegex;

    /* package */ RouteInstance(
        Method httpMethod, String path,
        Class<?> controllerType,
        String controllerMethodName,
        ControllerParams controllerParams,
        Set<String> modifiers
    )
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
        List<String> pathParamsNames = new ArrayList<>();
        for( String pathSegment : path.substring( 1 ).split( "/" ) )
        {
            if( pathSegment.startsWith( ":" ) || pathSegment.startsWith( "*" ) )
            {
                pathParamsNames.add( pathSegment.substring( 1 ) );
            }
        }

        // Disallow multiple occurences of a single parameter name in the path
        for( String paramName : pathParamsNames )
        {
            if( Collections.frequency( pathParamsNames, paramName ) > 1 )
            {
                throw new IllegalRouteException(
                    toString(),
                    "Parameter '" + paramName + "' is present several times in path."
                );
            }
        }

        // Ensure all parameters in path are bound to controller parameters and "vice et versa".
        // Allow params absent from path that should come form QueryString
        Set<String> allParamsNames = new LinkedHashSet<>();
        addAll( allParamsNames, controllerParamsNames );
        addAll( allParamsNames, pathParamsNames );
        for( String paramName : allParamsNames )
        {
            boolean found = false;
            for( String controllerParamName : controllerParamsNames )
            {
                if( paramName.equals( controllerParamName ) )
                {
                    found = true;
                    break;
                }
            }
            if( !found )
            {
                throw new IllegalRouteException(
                    toString(),
                    "Parameter '" + paramName + "' is present in path but not bound."
                );
            }
        }

        // Ensure controller method exists and return an Outcome or a CompletableFuture<Outcome>
        Class<?>[] controllerParamsTypes = controllerParams.types();
        try
        {
            controllerMethod = controllerType.getMethod( controllerMethodName, controllerParamsTypes );
            boolean correctReturnType = false;
            Throwable incorrectReturnTypeCause = null;
            if( Outcome.class.isAssignableFrom( controllerMethod.getReturnType() ) )
            {
                correctReturnType = true;
            }
            else if( CompletableFuture.class.isAssignableFrom( controllerMethod.getReturnType() ) )
            {
                try
                {
                    Class<?> futureOutcomeType = TypeResolver.resolveArgument(
                        controllerMethod.getGenericReturnType(),
                        CompletableFuture.class
                    );
                    if( futureOutcomeType.isAssignableFrom( Outcome.class ) )
                    {
                        correctReturnType = true;
                    }
                }
                catch( IllegalArgumentException ex )
                {
                    incorrectReturnTypeCause = ex;
                }
            }
            if( !correctReturnType )
            {
                String message = "Controller Method '" + controllerType.getSimpleName()
                                 + "#" + controllerMethodName
                                 + "( " + Arrays.toString( controllerParamsTypes ) + " )' "
                                 + "do not return an Outcome nor a CompletableFuture<Outcome>.";
                IllegalRouteException ex = new IllegalRouteException( toString(), message );
                if( incorrectReturnTypeCause != null )
                {
                    ex.initCause( incorrectReturnTypeCause );
                }
                throw ex;
            }
        }
        catch( NoSuchMethodException ex )
        {
            throw new IllegalRouteException(
                toString(),
                "Controller Method '" + controllerType.getSimpleName()
                + "#" + controllerMethodName
                + "( " + Arrays.toString( controllerParamsTypes ) + " )' "
                + "not found.",
                ex
            );
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
    public Method httpMethod()
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
    public java.lang.reflect.Method controllerMethod()
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
        for( ControllerParams.Param param : controllerParams )
        {
            if( ParamValue.FORCED == param.valueKind() )
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
                    if( queryString.keys().contains( param.name() ) )
                    {
                        // QUID We currently requires a single value to bind QueryString parameter.
                        // QUID Should we provide a config property to use first/last value?
                        // QUID Or binding to collectionish types from all values?
                        unboundValue = queryString.singleValue( param.name() );
                    }
                }
                if( unboundValue == null )
                {
                    if( ParamValue.DEFAULTED == param.valueKind() )
                    {
                        boundParams.put( param.name(), param.defaultedValue() );
                    }
                    else
                    {
                        throw new ParameterBinderException(
                            "Parameter named '" + param.name() + "' not found in path nor in query string."
                        );
                    }
                }
                else
                {
                    boundParams.put( param.name(), parameterBinders.bind( param.type(), param.name(), unboundValue ) );
                }
            }
        }
        return boundParams;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String unbindParameters( ParameterBinders parameterBinders, Map<String, Object> parameters )
    {
        List<String> paramsToUnbind = toList( controllerParams.names() );
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
                        ControllerParams.Param controllerParam = controllerParams.get( pathElement.substring( 1 ) );
                        Object value;
                        if( ParamValue.FORCED == controllerParam.valueKind() )
                        {
                            value = controllerParam.forcedValue();
                        }
                        else if( ParamValue.DEFAULTED == controllerParam.valueKind()
                                 && !parameters.containsKey( controllerParam.name() ) )
                        {
                            value = controllerParam.defaultedValue();
                        }
                        else
                        {
                            value = parameters.get( controllerParam.name() );
                        }
                        unboundPath.append(
                            parameterBinders.unbind(
                                (Class<Object>) controllerParam.type(),
                                controllerParam.name(),
                                value
                            )
                        );
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
        // WARN Only controller parameters are unbound, not all given parameters
        if( !paramsToUnbind.isEmpty() )
        {
            unboundPath.append( "?" );
            for( Iterator<String> qsit = paramsToUnbind.iterator(); qsit.hasNext(); )
            {
                String qsParam = qsit.next();
                ControllerParams.Param controllerParam = controllerParams.get( qsParam );
                Object value;
                if( ParamValue.FORCED == controllerParam.valueKind() )
                {
                    value = controllerParam.forcedValue();
                }
                else if( ParamValue.DEFAULTED == controllerParam.valueKind()
                         && !parameters.containsKey( controllerParam.name() ) )
                {
                    value = controllerParam.defaultedValue();
                }
                else
                {
                    value = parameters.get( controllerParam.name() );
                }
                unboundPath.append( qsParam ).append( "=" );
                unboundPath.append(
                    parameterBinders.unbind(
                        (Class<Object>) controllerParam.type(),
                        controllerParam.name(),
                        value
                    )
                );
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
        return toString( null, null, null );
    }

    public String toString( Integer methodPadLen, Integer pathPadLen, Integer actionPadLen )
    {
        StringBuilder builder = new StringBuilder();

        // HTTP Method
        builder.append( methodPadLen == null ? httpMethod : rightPad( methodPadLen, httpMethod.name() ) );
        builder.append( SPACE );

        // Path
        builder.append( pathPadLen == null ? path : rightPad( pathPadLen, path ) );
        builder.append( SPACE );

        // Action
        StringBuilder actionBuilder = new StringBuilder();
        actionBuilder.append( controllerType.getName() ).append( '.' ).append( controllerMethodName ).append( '(' );
        Iterator<ControllerParams.Param> it = controllerParams.iterator();
        if( it.hasNext() )
        {
            actionBuilder.append( SPACE );
            while( it.hasNext() )
            {
                ControllerParams.Param param = it.next();
                actionBuilder.append( param.type().getSimpleName() ).append( SPACE ).append( param.name() );
                switch( param.valueKind() )
                {
                    case FORCED:
                        actionBuilder.append( " = '" ).append( param.forcedValue() ).append( "'" );
                        break;
                    case DEFAULTED:
                        actionBuilder.append( " ?= '" ).append( param.defaultedValue() ).append( "'" );
                        break;
                    default:
                }
                if( it.hasNext() )
                {
                    actionBuilder.append( ", " );
                }
            }
            actionBuilder.append( SPACE );
        }
        actionBuilder.append( ')' );
        String action = actionBuilder.toString();
        builder.append( actionPadLen == null ? action : rightPad( actionPadLen, action ) );

        // Modifiers
        Iterator<String> modifiersIt = modifiers.iterator();
        if( modifiersIt.hasNext() )
        {
            builder.append( SPACE );
            while( modifiersIt.hasNext() )
            {
                builder.append( modifiersIt.next() );
                if( modifiersIt.hasNext() )
                {
                    builder.append( SPACE );
                }
            }
        }
        return builder.toString();
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
        if( this == obj )
        {
            return true;
        }
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
        return this.modifiers == other.modifiers || ( this.modifiers != null && this.modifiers.equals( other.modifiers ) );
        // CHECKSTYLE:ON
    }
}
