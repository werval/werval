package org.qiweb.runtime.http.routes;

import io.netty.handler.codec.http.HttpMethod;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.codeartisans.java.toolbox.Couple;
import org.codeartisans.java.toolbox.ObjectHolder;
import org.codeartisans.java.toolbox.Strings;

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
 *     to( MyController.class, new MethodRecorder<MyController>()
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
 */
public final class RouteBuilder
{

    /**
     * @param <T> the controller type
     */
    public static abstract class MethodRecorder<T>
    {

        private final List<Couple<String, Class<?>>> controllerParams = new ArrayList<>();

        /**
         * Record a new method parameter.
         * @param <ParamType> the parametrized parameter type
         * @param name the parameter name
         * @param type the parmeter type
         * @return an ignored default value
         */
        protected final <ParamType> ParamType p( String name, Class<ParamType> type )
        {
            controllerParams.add( new Couple<String, Class<?>>( name, type ) );
            return null;
        }

        /**
         * @return Recorded method parameters
         */
        public final List<Couple<String, Class<?>>> controllerParams()
        {
            return Collections.unmodifiableList( controllerParams );
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
    public static Routes parseRoutes( String routesString )
    {
        return parseRoutes( RouteBuilder.class.getClassLoader(), routesString );
    }

    public static Routes parseRoutes( ClassLoader loader, String routesString )
    {
        RoutesInstance routes = new RoutesInstance();
        Scanner scanner = new Scanner( routesString );
        while( scanner.hasNextLine() )
        {
            String routeString = scanner.nextLine().trim().replaceAll( "\\s+", " " );
            if( !routeString.startsWith( "#" ) && routeString.length() > 0 )
            {
                routes.add( parseRoute( loader, routeString ) );
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
     * @param routeString a textual route definition
     * @return a new Route instance
     * @throws IllegalRouteException when the textual route definition is invalid
     */
    public static Route parseRoute( final String routeString )
        throws IllegalRouteException
    {
        return parseRoute( RouteBuilder.class.getClassLoader(), routeString );
    }

    public static Route parseRoute( ClassLoader loader, final String routeString )
        throws IllegalRouteException
    {
        if( Strings.isEmpty( routeString ) )
        {
            throw new IllegalRouteException( "null", "Unable to parse null or empty String, was I?" );
        }
        final String cleanRouteString = routeString.trim().replaceAll( "\\s+", " " );
        // System.out.println( cleanRouteString );
        try
        {
            // Split route String
            int methodEnd = cleanRouteString.indexOf( ' ' );
            int pathEnd = cleanRouteString.indexOf( ' ', methodEnd + 1 );
            int controllerTypeEnd = cleanRouteString.lastIndexOf( '.' );
            int controllerMethodEnd = cleanRouteString.indexOf( '(', controllerTypeEnd );
            int controllerParamsEnd = cleanRouteString.lastIndexOf( ')' );
            String method = cleanRouteString.substring( 0, methodEnd );
            //System.out.println( "method = '" + method + "'" );
            String path = cleanRouteString.substring( methodEnd + 1, pathEnd );
            //System.out.println( "path = '" + path + "'" );
            String controllerTypeName = cleanRouteString.substring( pathEnd + 1, controllerTypeEnd );
            //System.out.println( "controller type = '" + controllerTypeName + "'" );
            String controllerMethodName = cleanRouteString.substring( controllerTypeEnd + 1, controllerMethodEnd );
            //System.out.println( "controller method = '" + controllerMethod + "'" );
            String controllerMethodParams = cleanRouteString.substring( controllerMethodEnd + 1, controllerParamsEnd ).trim();
            //System.out.println( "controller method params = '" + controllerMethodParams + "'" );

            // Parse HTTP Method
            HttpMethod httpMethod = HttpMethod.valueOf( method );

            // Parse controller type
            Class<?> controllerType = loader.loadClass( controllerTypeName );

            // Parse controller method parameters
            List<Couple<String, Class<?>>> controllerParams = new ArrayList<>();
            if( controllerMethodParams.length() > 0 )
            {
                String[] controllerMethodParamsSegments = controllerMethodParams.split( "," );
                for( String controllerMethodParamSegment : controllerMethodParamsSegments )
                {
                    String[] splitted = controllerMethodParamSegment.trim().replaceAll( "\\s+", " " ).split( " " );
                    if( splitted.length != 2 )
                    {
                        throw new IllegalRouteException( routeString,
                                                         "Unable to parse parameter: " + controllerMethodParamSegment );
                    }
                    String paramName = splitted[1];
                    String paramTypeName = splitted[0];

                    // Lookup parameter type
                    Class<?> paramType;
                    try
                    {
                        paramType = Class.forName( paramTypeName );
                    }
                    catch( ClassNotFoundException ex )
                    {
                        paramType = Class.forName( "java.lang." + paramTypeName );
                    }
                    controllerParams.add( new Couple<String, Class<?>>( paramName, paramType ) );
                }
            }

            // Eventually parse modifiers
            if( cleanRouteString.length() > controllerParamsEnd + 1 )
            {
                String modifiersString = cleanRouteString.substring( controllerParamsEnd + 2 );
                String[] modifiers = modifiersString.split( " " );
                System.out.println( "############# MODIFIERS! " + Arrays.toString( modifiers ) );
            }

            // Create new Route instance
            return new RouteInstance( httpMethod, path, controllerType, controllerMethodName, controllerParams );
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

    /**
     * Create a new RouteBuilder for a HttpMethod.
     */
    public static RouteBuilder route( HttpMethod method )
    {
        return new RouteBuilder( method );
    }
    private final HttpMethod httpMethod;
    private String path;
    private Class<?> controllerType;
    private String controllerMethodName;
    private final List<Couple<String, Class<?>>> controllerParams = new ArrayList<>();

    private RouteBuilder( HttpMethod httpMethod )
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
                throws Throwable
            {
                methodNameHolder.setHolded( method.getName() );
                return null;
            }
        } );

        methodRecorder.call( controller );

        this.controllerType = controllerType;
        this.controllerMethodName = methodNameHolder.getHolded();
        this.controllerParams.clear();
        this.controllerParams.addAll( methodRecorder.controllerParams() );

        return this;
    }

    /**
     * Create a new Route instance.
     * @return a new Route instance.
     */
    public Route newInstance()
    {
        return new RouteInstance( httpMethod, path, controllerType, controllerMethodName, controllerParams );
    }
}
