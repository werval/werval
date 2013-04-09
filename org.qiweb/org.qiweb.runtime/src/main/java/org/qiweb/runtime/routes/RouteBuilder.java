package org.qiweb.runtime.routes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.codeartisans.java.toolbox.ObjectHolder;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.routes.ControllerParams;
import org.qiweb.api.routes.ControllerParams.ControllerParam;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.routes.ControllerParamsInstance.ControllerParamInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fluent builder to create new Route and Routes instances.
 * <p>
 *     Routes can be parsed from a string or created using this very builder. The toString() method of Route instances
 *     created using this builder output a String that is parseable by this builder.
 * </p>
 * <p>Let's see some routes:</p>
 * <pre>
 * GET / controllers.Acme.index()
 * GET /login controllers.Acme.loginForm()
 * POST /login controllers.Acme.login()
 * POST /subscribe controllers.Acme.subscribe()
 * GET /:nickname controllers.Acme.home( String nickname )
 * </pre>
 * <p>Here is a complete example using the API:</p>
 * <pre>
 * Route route = route( GET ).on( "/foo/:slug/bar/:id" ).
 *     to( MyController.class, new MethodRecorder&lt;MyController&gt;()
 * {
 *     @Override
 *     protected void call( MyController ctrl )
 *     {
 *         ctrl.another( p( "id", String.class ), p( "slug", Integer.class ) );
 *     }
 * } ).newInstance();
 * 
 * System.out.println( route.toString() );
 * </pre>
 * <p>The last line would output:</p>
 * <pre>GET /foo/:slug/bar/:id f.q.n.MyController.another( String id, Integer slug )</pre>
 * <p>If you happen to use Java 8 you'll be able to shorten the code a bit:</p>
 * <pre>
 * Route route = route( GET ).on( "/foo/:slug/bar/:id" ).
 *     to( MyController.class, ctrl -> ctrl.another( p( "id", String.class ), p( "slug", Integer.class ) ) ).
 *     newInstance();
 * </pre>
 * <p>
 *     <strong>Important:</strong> when using the fluent api to declare routes you can only use interfaces to declare
 *     controllers.
 * </p>
 */
public final class RouteBuilder
{

    private static final Logger LOG = LoggerFactory.getLogger( RouteBuilder.class );

    /**
     * @param <T> the controller type
     */
    public abstract static class MethodRecorder<T>
    {

        private final Map<String, ControllerParam> controllerParams = new LinkedHashMap<>();

        /**
         * Record a new method parameter.
         * @param <T> the parametrized parameter type
         * @param name the parameter name
         * @param type the parmeter type
         * @return an ignored default value
         */
        protected final <T> T p( String name, Class<T> type )
        {
            controllerParams.put( name, new ControllerParamInstance( name, type ) );
            return null;
        }

        /**
         * Record a new method parameter with forced value.
         * @param <T> the parametrized parameter type
         * @param name the parameter name
         * @param type the parmeter type
         * @param forcedValue the forced value
         * @return the forced value
         */
        protected final <T> T p( String name, Class<T> type, T forcedValue )
        {
            controllerParams.put( name, new ControllerParamInstance( name, type, forcedValue ) );
            return forcedValue;
        }

        /**
         * @return Recorded method parameters
         */
        public final Map<String, ControllerParam> controllerParams()
        {
            return Collections.unmodifiableMap( controllerParams );
        }

        /**
         * Method recorder callback.
         * <p>
         *  Call the method of your choice on the given controller and use the
         *  {@link #p(java.lang.String, java.lang.Class)} method to record method parameters.
         * </p>
         * @param controller a dynamic proxy that will record a method call
         */
        protected abstract void call( T controller );
    }

    /**
     * Parse a textual definition of multiple routes, one per line.
     * <p>Ignore lines that begins with a # or are empty.</p>
     */
    public static Routes parseRoutes( Application application, String routesString )
    {
        RoutesInstance routes = new RoutesInstance();
        Scanner scanner = new Scanner( routesString );
        while( scanner.hasNextLine() )
        {
            String routeString = scanner.nextLine().trim().replaceAll( "\\s+", " " );
            if( !routeString.startsWith( "#" ) && routeString.length() > 0 )
            {
                routes.add( parseRoute( application, routeString ) );
            }
        }
        return routes;
    }

    /**
     * Create a Routes instances containing, in order, the given Route instances.
     * @param routes Route instances
     * @return a new Routes instance
     */
    public static Routes routes( Route... routes )
    {
        RoutesInstance instance = new RoutesInstance();
        instance.addAll( Arrays.asList( routes ) );
        return instance;
    }

    /**
     * Parse a textual route definition to a Route instance.
     * @param loader The ClassLoader to use to find Controller classes
     * @param routeString a textual route definition
     * @return a new Route instance
     * @throws IllegalRouteException when the textual route definition is invalid
     */
    public static Route parseRoute( Application application, final String routeString )
    {
        if( Strings.isEmpty( routeString ) )
        {
            throw new IllegalRouteException( "null", "Unable to parse null or empty String, was I?" );
        }
        final String cleanRouteString = routeString.trim().replaceAll( "\\s+", " " );
        try
        {
            // Split route String
            final int methodEnd = cleanRouteString.indexOf( ' ' );
            final int pathEnd = cleanRouteString.indexOf( ' ', methodEnd + 1 );
            final int controllerTypeEnd;
            final int controllerMethodEnd;
            final int controllerParamsEnd;
            final int modifiersEnd;
            if( cleanRouteString.indexOf( '(', pathEnd + 1 ) > 0 )
            {
                // Parenthesis, with or without parameters or modifiers
                controllerTypeEnd = cleanRouteString.substring( 0, cleanRouteString.indexOf( '(', pathEnd ) ).lastIndexOf( '.' );
                controllerMethodEnd = cleanRouteString.indexOf( '(', controllerTypeEnd );
                controllerParamsEnd = cleanRouteString.lastIndexOf( ')' );
                modifiersEnd = cleanRouteString.length() > controllerParamsEnd + 1 ? cleanRouteString.length() : -1;
            }
            else if( cleanRouteString.indexOf( ' ', pathEnd + 1 ) > 0 )
            {
                // No parenthesis with modifiers
                controllerTypeEnd = cleanRouteString.substring( 0, cleanRouteString.indexOf( ' ', pathEnd + 1 ) ).lastIndexOf( '.' );
                controllerMethodEnd = cleanRouteString.indexOf( ' ', controllerTypeEnd );
                controllerParamsEnd = -1;
                modifiersEnd = cleanRouteString.length();
            }
            else
            {
                // No parenthesis without modifiers
                // eg. POST /foo/bar com.acme.app.FakeController.test
                controllerTypeEnd = cleanRouteString.lastIndexOf( '.' );
                controllerMethodEnd = cleanRouteString.length();
                controllerParamsEnd = -1;
                modifiersEnd = -1;
            }

            if( LOG.isTraceEnabled() )
            {
                LOG.trace( "Parsing route string: {}\n"
                           + "\tMethod end:             {}\n"
                           + "\tPath end:               {}\n"
                           + "\tController Type end:    {}\n"
                           + "\tController Method end:  {}\n"
                           + "\tController Params end:  {}",
                           new Object[]
                {
                    cleanRouteString, methodEnd, pathEnd, controllerTypeEnd, controllerMethodEnd, controllerParamsEnd
                } );
            }

            String httpMethod = cleanRouteString.substring( 0, methodEnd );
            String path = cleanRouteString.substring( methodEnd + 1, pathEnd );
            String controllerTypeName = cleanRouteString.substring( pathEnd + 1, controllerTypeEnd );
            String controllerMethodName = cleanRouteString.substring( controllerTypeEnd + 1, controllerMethodEnd );
            String controllerMethodParams;
            if( controllerParamsEnd != -1 )
            {
                controllerMethodParams = cleanRouteString.substring( controllerMethodEnd + 1, controllerParamsEnd ).trim();
            }
            else
            {
                controllerMethodParams = "";
            }

            if( LOG.isTraceEnabled() )
            {
                LOG.trace( "Parsing route string: {}\n"
                           + "\tMethod:             {}\n"
                           + "\tPath:               {}\n"
                           + "\tController Type:    {}\n"
                           + "\tController Method:  {}\n"
                           + "\tController Params:  {}",
                           new Object[]
                {
                    cleanRouteString, httpMethod, path, controllerTypeName, controllerMethodName, controllerMethodParams
                } );
            }

            // Parse controller type
            Class<?> controllerType = application.classLoader().loadClass( controllerTypeName );

            // Parse controller method parameters
            ControllerParams controllerParams = parseControllerParams( application, routeString, controllerMethodParams );

            // Eventually parse modifiers
            Set<String> modifiers = new LinkedHashSet<>();
            if( modifiersEnd != -1 )
            {
                String modifiersString = controllerParamsEnd != -1
                                         ? cleanRouteString.substring( controllerParamsEnd + 2 )
                                         : cleanRouteString.substring( controllerMethodEnd + 1 );
                modifiers.addAll( Arrays.asList( modifiersString.trim().split( " " ) ) );
            }

            // Create new Route instance
            return new RouteInstance( httpMethod, path, controllerType, controllerMethodName, controllerParams, modifiers );
        }
        catch( IllegalRouteException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new IllegalRouteException( routeString, ex.getMessage(), ex );
        }
    }

    private static ControllerParams parseControllerParams( Application application, String routeString, String controllerMethodParams )
        throws ClassNotFoundException
    {
        Map<String, ControllerParam> controllerParams = new LinkedHashMap<>();
        if( controllerMethodParams.length() > 0 )
        {
            List<String> controllerMethodParamsSegments = splitControllerMethodParams( controllerMethodParams );
            for( String controllerMethodParamSegment : controllerMethodParamsSegments )
            {
                if( controllerMethodParamSegment.contains( "=" ) )
                {
                    // Route with forced values
                    String[] equalSplitted = controllerMethodParamSegment.split( "=", 2 );
                    String[] typeAndName = equalSplitted[0].split( " " );
                    String name = typeAndName[1];
                    Class<?> type = lookupParamType( application, typeAndName[0] );
                    String forcedValueString = equalSplitted[1].substring( 1 ).trim();
                    if( forcedValueString.startsWith( "'" ) )
                    {
                        forcedValueString = forcedValueString.
                            substring( 1, forcedValueString.length() - 1 ).
                            replaceAll( "\\'", "'" );
                    }
                    Object forcedValue = application.pathBinders().bind( type, name, forcedValueString );
                    controllerParams.put( name, new ControllerParamInstance( name, type, forcedValue ) );
                }
                else
                {
                    // Route without forced values
                    String[] splitted = controllerMethodParamSegment.split( " " );
                    if( splitted.length != 2 )
                    {
                        throw new IllegalRouteException( routeString,
                                                         "Unable to parse parameter: " + controllerMethodParamSegment );
                    }
                    String paramName = splitted[1];
                    String paramTypeName = splitted[0];
                    controllerParams.put( paramName, new ControllerParamInstance( paramName, lookupParamType( application, paramTypeName ) ) );
                }
            }
        }
        return new ControllerParamsInstance( controllerParams );
    }

    private static List<String> splitControllerMethodParams( String controllerMethodParams )
    {
        List<String> segments = new ArrayList<>();
        boolean insideQuotes = false;
        char previous = controllerMethodParams.charAt( 0 );
        StringBuilder sb = new StringBuilder().append( previous );
        for( int idx = 1; idx < controllerMethodParams.length(); idx++ )
        {
            char character = controllerMethodParams.charAt( idx );
            if( character == '\'' && previous != '\\' )
            {
                insideQuotes = !insideQuotes;
            }
            if( insideQuotes || character != ',' )
            {
                sb.append( character );
            }
            else
            {
                segments.add( sb.toString().trim() );
                sb = new StringBuilder();
            }
        }
        segments.add( sb.toString().trim() );
        return segments;
    }

    private static Class<?> lookupParamType( Application application, String paramTypeName )
        throws ClassNotFoundException
    {
        try
        {
            // First try parameter FQCN
            return application.classLoader().loadClass( paramTypeName );
        }
        catch( ClassNotFoundException fqcnNotFound )
        {
            LOG.trace( "Param type {} not found", paramTypeName, fqcnNotFound );
            List<String> typesTried = new ArrayList<>();
            typesTried.add( paramTypeName );
            // Try in configured imported packages
            List<String> importedPackages = application.config().getStringList( "qiweb.routes.imported-packages" );
            for( String importedPackage : importedPackages )
            {
                String fqcn = importedPackage + "." + paramTypeName;
                try
                {
                    return application.classLoader().loadClass( fqcn );
                }
                catch( ClassNotFoundException importedNotFound )
                {
                    LOG.trace( "Param type {} not found in {}", paramTypeName, importedPackage, importedNotFound );
                }
            }
            throw new ClassNotFoundException( "Param type not found, tried " + typesTried.toString() );
        }
    }

    /**
     * Create a new RouteBuilder for a Http Method.
     */
    public static RouteBuilder route( String method )
    {
        return new RouteBuilder( method );
    }
    private final String httpMethod;
    private String path;
    private Class<?> controllerType;
    private String controllerMethodName;
    private ControllerParams controllerParams = new ControllerParamsInstance();
    private final Set<String> modifiers = new LinkedHashSet<>();

    private RouteBuilder( String httpMethod )
    {
        this.httpMethod = httpMethod;
    }

    /**
     * Set Route path.
     * @param path Route path
     * @return this RouteBuilder
     */
    public RouteBuilder on( String path )
    {
        this.path = path;
        return this;
    }

    /**
     * Set Route controller type, method and parameters.
     * @param controllerType the controller type
     * @param methodRecorder a {@link MethodRecorder}
     * @return this RouteBuilder
     */
    public <T> RouteBuilder to( final Class<T> controllerType, MethodRecorder<T> methodRecorder )
    {
        final ObjectHolder<String> methodNameHolder = new ObjectHolder<>();
        Class<?>[] types = new Class<?>[]
        {
            controllerType
        };
        @SuppressWarnings( "unchecked" )
        T controller = (T) Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
                                                   types,
                                                   new InvocationHandler()
        {
            @Override
            public Object invoke( Object proxy, Method method, Object[] args )
            {
                methodNameHolder.setHolded( method.getName() );
                return null;
            }
        } );

        methodRecorder.call( controller );

        this.controllerType = controllerType;
        this.controllerMethodName = methodNameHolder.getHolded();
        this.controllerParams = new ControllerParamsInstance( methodRecorder.controllerParams() );

        return this;
    }

    public RouteBuilder modifiedBy( String... modifiers )
    {
        this.modifiers.addAll( Arrays.asList( modifiers ) );
        return this;
    }

    /**
     * Create a new Route instance.
     * @return a new Route instance.
     */
    public Route newInstance()
    {
        return new RouteInstance( httpMethod, path, controllerType, controllerMethodName, controllerParams, modifiers );
    }
}
