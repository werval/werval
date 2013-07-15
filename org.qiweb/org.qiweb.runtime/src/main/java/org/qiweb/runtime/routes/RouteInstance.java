package org.qiweb.runtime.routes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.routes.ControllerParams;
import org.qiweb.api.routes.ControllerParams.ControllerParam;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.api.routes.Route;

import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.ensureNotEmpty;
import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.ensureNotNull;
import static org.qi4j.functional.Iterables.addAll;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.matchesAny;
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

        // Ensure all parameters in path are bound to controller parameters and "vice et versa".
        // Allow multiple occurences of a single parameter name in the path

        Iterable<String> controllerParamsNames = controllerParams.names();
        Iterable<String> pathParamsNames = map( new Function<String, String>()
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
        }, iterable( path.substring( 1 ).split( "/" ) ) ) );

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
            if( !controllerParams.get( paramName ).hasForcedValue() && !matchesAny( in( paramName ), pathParamsNames ) )
            {
                throw new IllegalRouteException( toString(),
                                                 "Parameter '" + paramName + "' is not present in path." );
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
    public Map<String, Object> bindPath( PathBinders pathBinders, String path )
    {
        Matcher matcher = pathRegex.matcher( path );
        if( !matcher.matches() )
        {
            throw new IllegalArgumentException( "Unable to bind, Route is not satified by path: " + path );
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
                String unboundValue = matcher.group( param.name() );
                if( unboundValue == null )
                {
                    throw new IllegalArgumentException( "Parameter named '" + param.name() + "' not found in path." );
                }
                boundParams.put( param.name(), pathBinders.bind( param.type(), param.name(), unboundValue ) );
            }
        }
        return boundParams;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String unbindPath( PathBinders pathBinders, Map<String, Object> parameters )
    {
        StringBuilder unboundPath = new StringBuilder( "/" );
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
                        // QUID is this class cast legal?
                        unboundPath.append( pathBinders.unbind( (Class<Object>) controllerParam.type(),
                                                                controllerParam.name(),
                                                                value ) );
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
