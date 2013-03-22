package org.qiweb.runtime.http.routes;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.codeartisans.java.toolbox.Couple;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;
import org.qiweb.api.http.HttpRequestHeader;
import org.qiweb.runtime.util.Couples;
import org.qiweb.api.http.Result;

import static org.qi4j.api.util.NullArgumentException.validateNotEmpty;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import static org.qi4j.functional.Iterables.addAll;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.matchesAny;
import static org.qi4j.functional.Iterables.toArray;
import static org.qi4j.functional.Iterables.toList;
import static org.qi4j.functional.Specifications.in;

/**
 * Instance of a Route.
 */
/* package */ class RouteInstance
    implements Route
{

    private final String httpMethod;
    private final String path;
    private final Class<?> controllerType;
    private Method controllerMethod;
    private final String controllerMethodName;
    private final Iterable<Couple<String, Class<?>>> controllerParams;

    /* package */ RouteInstance( String httpMethod, String path,
                   Class<?> controllerType,
                   String controllerMethodName,
                   Iterable<Couple<String, Class<?>>> controllerParams )
    {
        validateNotNull( "HTTP Method", httpMethod );
        validateNotEmpty( "Path", path );
        validateNotNull( "Controller Type", controllerType );
        validateNotEmpty( "Controller Method Name", controllerMethodName );
        this.httpMethod = httpMethod;
        this.path = path;
        this.controllerType = controllerType;
        this.controllerMethodName = controllerMethodName;
        this.controllerParams = toList( controllerParams );
        validate();
    }

    private void validate()
    {
        if( !path.startsWith( "/" ) )
        {
            throw new IllegalRouteException( toString(), "Invalid path: " + path );
        }

        // Ensure all parameters in path are bound to controller parameters and "vice et versa".
        // Allow multiple occurences of a single parameter name in the path

        Iterable<String> controllerParamsNames = Couples.<String>left( controllerParams );
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
                return pathSegment.startsWith( ":" );
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
            if( !matchesAny( in( paramName ), pathParamsNames ) )
            {
                throw new IllegalRouteException( toString(),
                                                 "Parameter '" + paramName + "' is not present in path." );
            }
        }

        // Ensure controller method exists and return a Result

        Class[] controllerParamsTypes = toArray( Class.class, Couples.<Class>right( controllerParams ) );
        try
        {
            controllerMethod = controllerType.getMethod( controllerMethodName, controllerParamsTypes );
            if( !controllerMethod.getReturnType().isAssignableFrom( Result.class ) )
            {
                throw new IllegalRouteException( toString(),
                                                 "Controller Method '" + controllerType.getSimpleName() + "#"
                                                 + controllerMethodName + "( " + controllerParamsTypes + " )' "
                                                 + "do not return a Result." );
            }
        }
        catch( NoSuchMethodException ex )
        {
            throw new IllegalRouteException( toString(),
                                             "Controller Method '" + controllerType.getSimpleName() + "#"
                                             + controllerMethodName + "( " + controllerParamsTypes + " )' not found.",
                                             ex );
        }
    }

    @Override
    public boolean satisfiedBy( HttpRequestHeader requestHeader )
    {
        String requestMethod = requestHeader.method();
        String requestPath = requestHeader.path();
        String[] requestPathSegments = requestPath.substring( 1 ).split( "/" );
        if( !httpMethod.equals( requestMethod ) )
        {
            return false;
        }
        String[] routePathSegments = path.substring( 1 ).split( "/" );
        if( routePathSegments.length != requestPathSegments.length )
        {
            return false;
        }
        for( int idx = 0; idx < routePathSegments.length; idx++ )
        {
            String routePathSegment = routePathSegments[idx];
            String requestPathSegment = requestPathSegments[idx];
            if( !routePathSegment.startsWith( ":" ) && !routePathSegment.equals( requestPathSegment ) )
            {
                return false;
            }
        }
        return true;
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
    public Iterable<Couple<String, Class<?>>> controllerParams()
    {
        return controllerParams;
    }

    @Override
    public String controllerParamPathValue( final String paramName, String path )
    {
        String[] routePathSegments = this.path.split( "/" );
        int segmentIndex = 0;
        boolean segmentFound = false;
        for( String routePathSegment : routePathSegments )
        {
            if( routePathSegment.equals( ":" + paramName ) )
            {
                segmentFound = true;
                break;
            }
            segmentIndex++;
        }
        if( !segmentFound )
        {
            throw new IllegalArgumentException( "Parameter named '" + paramName + "' not found." );
        }
        return path.split( "/" )[segmentIndex];
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( httpMethod ).append( " " ).append( path ).append( " " ).
            append( controllerType.getName() ).append( "." ).append( controllerMethodName ).append( "(" );
        Iterator<Couple<String, Class<?>>> it = controllerParams.iterator();
        if( it.hasNext() )
        {
            sb.append( " " );
            while( it.hasNext() )
            {
                Couple<String, Class<?>> param = it.next();
                sb.append( param.right().getSimpleName() ).append( " " ).append( param.left() );
                if( it.hasNext() )
                {
                    sb.append( ", " );
                }
            }
            sb.append( " " );
        }
        sb.append( ")" );
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + ( this.httpMethod != null ? this.httpMethod.hashCode() : 0 );
        hash = 59 * hash + ( this.path != null ? this.path.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerType != null ? this.controllerType.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerMethodName != null ? this.controllerMethodName.hashCode() : 0 );
        hash = 59 * hash + ( this.controllerParams != null ? this.controllerParams.hashCode() : 0 );
        return hash;
    }

    @Override
    @SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
    public boolean equals( Object obj )
    {
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
        return true;
    }
}
