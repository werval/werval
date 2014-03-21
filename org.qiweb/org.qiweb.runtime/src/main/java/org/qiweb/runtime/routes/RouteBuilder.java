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
package org.qiweb.runtime.routes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.routes.ControllerParams.ControllerParam;
import org.qiweb.runtime.routes.ControllerParamsInstance.ControllerParamInstance;
import org.qiweb.runtime.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.runtime.ConfigKeys.QIWEB_ROUTES_IMPORTEDPACKAGES;

/**
 * Fluent builder to create new Route and Routes instances.
 *
 * Routes can be parsed from a string or created using this very builder. The toString() method of Route instances
 * created using this builder output a String that is parseable by this builder.
 * <p>
 * Let's see some routes:
 * <pre>
 * GET / controllers.Acme.index()
 * GET /login controllers.Acme.loginForm()
 * POST /login controllers.Acme.login()
 * POST /subscribe controllers.Acme.subscribe()
 * GET /:nickname controllers.Acme.home( String nickname )
 * </pre>
 * Here is a complete example using the API:
 * <pre>
 * Route route = route( GET ).on( "/foo/:slug/bar/:id" ).
 *     to( MyController.class, new MethodRecorder&lt;MyController&gt;() {
 *         protected void call( MyController ctrl ) {
 *             ctrl.another( p( "id", String.class ), p( "slug", Integer.class ) );
 *         }
 *     } ).newInstance();
 *
 * System.out.println( route.toString() );
 * </pre>
 * The last line would output:
 * <pre>GET /foo/:slug/bar/:id f.q.n.MyController.another( String id, Integer slug )</pre>
 * If you happen to use Java 8 you'll be able to shorten the code a bit:
 * <pre>
 * Route route = route( GET ).on( "/foo/:slug/bar/:id" ).
 *     to( MyController.class, ctrl -&gt; ctrl.another( p( "id", String.class ), p( "slug", Integer.class ) ) ).
 *     newInstance();
 * </pre>
 * <strong>Important:</strong> when using the fluent api to declare routes you can only use interfaces to declare
 * controllers.
 */
public final class RouteBuilder
{
    private static final ThreadLocal<Map<String, ControllerParam>> CTRL_PARAM_RECORD = new ThreadLocal<Map<String, ControllerParam>>()
    {
        @Override
        protected Map<String, ControllerParam> initialValue()
        {
            return new LinkedHashMap<>();
        }
    };
    private static final Logger LOG = LoggerFactory.getLogger( RouteBuilder.class );

    /**
     * Record a new method parameter.
     *
     * @param <T>  the parameterized parameter type
     * @param name the parameter name
     * @param type the parmeter type
     *
     * @return an ignored default value
     */
    public static final <T> T p( String name, Class<T> type )
    {
        CTRL_PARAM_RECORD.get().put( name, new ControllerParamInstance( name, type ) );
        return null;
    }

    /**
     * Record a new method parameter with forced value.
     *
     * @param <T>         the parameterized parameter type
     * @param name        the parameter name
     * @param type        the parmeter type
     * @param forcedValue the forced value
     *
     * @return the forced value
     */
    public static final <T> T p( String name, Class<T> type, T forcedValue )
    {
        CTRL_PARAM_RECORD.get().put( name, new ControllerParamInstance( name, type, forcedValue ) );
        return forcedValue;
    }

    /**
     * Parse a textual definition of multiple routes, one per line.
     *
     * Ignore lines that begins with a # or are empty.
     *
     * @param application  Application
     * @param routesString Routes as String
     *
     * @return Parsed Routes
     */
    public static List<Route> parseRoutes( Application application, String routesString )
    {
        List<Route> routes = new ArrayList<>();
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
     *
     * @param routes Route instances
     *
     * @return a new Routes instance
     */
    public static Routes routes( Route... routes )
    {
        return new RoutesInstance( routes );
    }

    /**
     * Create a Routes instances containing, in order, the given Route instances.
     *
     * @param routes Route instances
     *
     * @return a new Routes instance
     */
    public static Routes routes( Iterable<Route> routes )
    {
        return new RoutesInstance( routes );
    }

    /**
     * Parse a textual route definition to a Route instance.
     *
     * @param application Application
     * @param routeString a textual route definition
     *
     * @return a new Route instance
     *
     * @throws IllegalRouteException when the textual route definition is invalid
     */
    public static Route parseRoute( Application application, final String routeString )
    {
        if( Strings.isEmpty( routeString ) )
        {
            throw new IllegalRouteException( "null", "Unable to parse null or empty String." );
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
                LOG.trace(
                    "Parsing route string, indices: {}\n"
                    + "\tMethod end:             {}\n"
                    + "\tPath end:               {}\n"
                    + "\tController Type end:    {}\n"
                    + "\tController Method end:  {}\n"
                    + "\tController Params end:  {}",
                    new Object[]
                    {
                        cleanRouteString, methodEnd, pathEnd,
                        controllerTypeEnd, controllerMethodEnd, controllerParamsEnd
                    }
                );
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
                LOG.trace(
                    "Parsing route string, values: {}\n"
                    + "\tMethod:             {}\n"
                    + "\tPath:               {}\n"
                    + "\tController Type:    {}\n"
                    + "\tController Method:  {}\n"
                    + "\tController Params:  {}",
                    new Object[]
                    {
                        cleanRouteString, httpMethod, path,
                        controllerTypeName, controllerMethodName, controllerMethodParams
                    }
                );
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
                    Object forcedValue = application.parameterBinders().bind( type, name, forcedValueString );
                    controllerParams.put( name, new ControllerParamInstance( name, type, forcedValue ) );
                }
                else
                {
                    // Route without forced values
                    String[] splitted = controllerMethodParamSegment.split( " " );
                    if( splitted.length != 2 )
                    {
                        throw new IllegalRouteException(
                            routeString,
                            "Unable to parse parameter: " + controllerMethodParamSegment
                        );
                    }
                    String paramName = splitted[1];
                    String paramTypeName = splitted[0];
                    controllerParams.put(
                        paramName,
                        new ControllerParamInstance( paramName, lookupParamType( application, paramTypeName ) )
                    );
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
            LOG.trace( "Lookup Param Type - {} not found", paramTypeName );
            List<String> typesTried = new ArrayList<>();
            typesTried.add( paramTypeName );
            // Try in configured imported packages
            List<String> importedPackages = application.config().stringList( QIWEB_ROUTES_IMPORTEDPACKAGES );
            for( String importedPackage : importedPackages )
            {
                String fqcn = importedPackage + "." + paramTypeName;
                try
                {
                    Class<?> clazz = application.classLoader().loadClass( fqcn );
                    LOG.trace( "Lookup Param Type - {} found", fqcn );
                    return clazz;
                }
                catch( ClassNotFoundException importedNotFound )
                {
                    LOG.trace( "Lookup Param Type - {} not found", fqcn );
                }
            }
            throw new ClassNotFoundException( "Param type not found, tried " + typesTried.toString() );
        }
    }

    /**
     * Create a new RouteBuilder for a Http Method.
     *
     * @param method HTTP Method
     *
     * @return new RouteBuilder
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
     *
     * @param path Route path
     *
     * @return this RouteBuilder
     */
    public RouteBuilder on( String path )
    {
        this.path = path;
        return this;
    }

    /**
     * Set Route controller type, method and parameters.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType the controller type
     * @param callRecorder   a Consumer for the controller type to record a method invocation
     *
     * @return this RouteBuilder
     */
    @SuppressWarnings( "unchecked" )
    public <T> RouteBuilder to( final Class<T> controllerType, Consumer<T> callRecorder )
    {
        final Holder<String> methodNameHolder = new Holder<>();
        T controllerProxy;
        if( controllerType.isInterface() )
        {
            controllerProxy = (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]
                {
                    controllerType
                },
                new InvocationHandler()
                {
                    @Override
                    public Object invoke( Object proxy, Method method, Object[] args )
                    {
                        methodNameHolder.set( method.getName() );
                        return null;
                    }
                }
            );
        }
        else
        {
            try
            {
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass( controllerType );
                controllerProxy = (T) proxyFactory.createClass().newInstance();
                ( (javassist.util.proxy.Proxy) controllerProxy ).setHandler(
                    new MethodHandler()
                    {
                        @Override
                        public Object invoke( Object self, Method controllerMethod, Method proceed, Object[] args )
                        {
                            methodNameHolder.set( controllerMethod.getName() );
                            return null;
                        }
                    }
                );
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new QiWebException( "Unable to record controller method in RouteBuilder", ex );
            }
        }

        try
        {
            callRecorder.accept( controllerProxy );

            this.controllerType = controllerType;
            this.controllerMethodName = methodNameHolder.get();
            this.controllerParams = new ControllerParamsInstance( CTRL_PARAM_RECORD.get() );

            return this;
        }
        finally
        {
            CTRL_PARAM_RECORD.remove();
        }
    }

    public RouteBuilder modifiedBy( String... modifiers )
    {
        this.modifiers.addAll( Arrays.asList( modifiers ) );
        return this;
    }

    /**
     * Create a new Route instance.
     *
     * @return a new Route instance.
     */
    public Route newInstance()
    {
        return new RouteInstance( httpMethod, path, controllerType, controllerMethodName, controllerParams, modifiers );
    }
}
