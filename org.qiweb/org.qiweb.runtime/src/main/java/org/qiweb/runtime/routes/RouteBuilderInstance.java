/*
 * Copyright (c) 2013-2014 the original author or authors
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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.IllegalRouteException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.Method;
import org.qiweb.api.routes.ControllerCallRecorder;
import org.qiweb.api.routes.ControllerParams;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.internal.RouteBuilderContext;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_SET;
import static org.qiweb.api.util.Strings.EMPTY;
import static org.qiweb.runtime.ConfigKeys.QIWEB_ROUTES_IMPORTEDPACKAGES;

/**
 * RouteBuilder Instance.
 */
public class RouteBuilderInstance
    implements org.qiweb.api.routes.RouteBuilder
{
    private final Application app;
    private final String pathPrefix;

    /**
     * Create a new RouteBuilder instance that cannot parse.
     *
     * See {@link #RouteBuilderInstance(org.qiweb.api.Application)}.
     */
    public RouteBuilderInstance()
    {
        this( null, null );
    }

    /**
     * Create a new RouteBuilder instance that cannot parse and apply a path prefix to all built routes.
     *
     * See {@link #RouteBuilderInstance(org.qiweb.api.Application)}.
     *
     * @param pathPrefix Path prefix
     */
    public RouteBuilderInstance( String pathPrefix )
    {
        this( null, pathPrefix );
    }

    /**
     * Create a new RouteBuilder instance.
     *
     * @param app ApplicationSPI
     */
    public RouteBuilderInstance( Application app )
    {
        this( app, EMPTY );
    }

    /**
     * Create a new RouteBuilder instance that apply a path prefix to all built routes.
     *
     * @param app        ApplicationSPI
     * @param pathPrefix Path prefix
     */
    public RouteBuilderInstance( Application app, String pathPrefix )
    {
        this.app = app;
        if( Strings.isEmpty( pathPrefix ) )
        {
            this.pathPrefix = EMPTY;
        }
        else
        {
            // Prepend / if necessary
            String actualPrefix = pathPrefix.startsWith( "/" )
                                  ? pathPrefix
                                  : "/" + pathPrefix;
            // Remove trailing / if necessary
            this.pathPrefix = actualPrefix.endsWith( "/" )
                              ? actualPrefix.substring( 0, actualPrefix.length() - 1 )
                              : actualPrefix;
        }
    }

    @Override
    public RouteDeclaration route()
    {
        return new RouteDeclarationInstance(
            null, pathPrefix, null, null, null, ControllerParams.EMPTY, EMPTY_SET
        );
    }

    @Override
    public RouteDeclaration route( String httpMethod )
    {
        return new RouteDeclarationInstance(
            Method.valueOf( httpMethod ), pathPrefix, null, null, null, ControllerParams.EMPTY, EMPTY_SET
        );
    }

    @Override
    public RouteDeclaration route( Method httpMethod )
    {
        return new RouteDeclarationInstance(
            httpMethod, pathPrefix, null, null, null, ControllerParams.EMPTY, EMPTY_SET
        );
    }

    @Override
    public RouteParser parse()
    {
        if( app == null )
        {
            throw new IllegalStateException( "Cannot parse Routes without an Application" );
        }
        return new RouteParserInstance( app, pathPrefix );
    }

    private static final class RouteDeclarationInstance
        implements RouteDeclaration
    {
        private final Method httpMethod;
        private final String pathPrefix;
        private final String path;
        private final Class<?> controllerType;
        private final String controllerMethodName;
        private final ControllerParams controllerParams;
        private final Set<String> modifiers;

        public RouteDeclarationInstance(
            Method httpMethod,
            String pathPrefix, String path,
            Class<?> controllerType, String controllerMethodName, ControllerParams controllerParams,
            Set<String> modifiers
        )
        {
            this.httpMethod = httpMethod;
            this.pathPrefix = pathPrefix;
            this.path = path;
            this.controllerType = controllerType;
            this.controllerMethodName = controllerMethodName;
            this.controllerParams = controllerParams;
            this.modifiers = modifiers;
        }

        @Override
        public RouteDeclaration method( String httpMethod )
        {
            return new RouteDeclarationInstance(
                Method.valueOf( httpMethod ),
                pathPrefix, path,
                controllerType, controllerMethodName, controllerParams,
                modifiers
            );
        }

        @Override
        public RouteDeclaration on( String path )
        {
            return new RouteDeclarationInstance(
                httpMethod,
                pathPrefix, path,
                controllerType, controllerMethodName, controllerParams,
                modifiers
            );
        }

        @Override
        public <T> RouteDeclaration to( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
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
                        public Object invoke( Object proxy, java.lang.reflect.Method method, Object[] args )
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
                            public Object invoke( Object self, java.lang.reflect.Method controllerMethod,
                                                  java.lang.reflect.Method proceed, Object[] args
                            )
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
                // Belt & Braces
                RouteBuilderContext.clear();

                // Record
                try
                {
                    callRecorder.recordCall( controllerProxy );
                }
                catch( Exception ex )
                {
                    throw new QiWebException(
                        "Error while recording Controller call for Route building: " + ex.getMessage(),
                        ex
                    );
                }

                // Done!
                return new RouteDeclarationInstance(
                    httpMethod,
                    pathPrefix, path,
                    controllerType, methodNameHolder.get(),
                    new ControllerParams( RouteBuilderContext.recordedControllerParams() ),
                    modifiers
                );
            }
            finally
            {
                // Clean-up
                RouteBuilderContext.clear();
            }
        }

        @Override
        public RouteDeclaration modifiedBy( String... modifiers )
        {
            return new RouteDeclarationInstance(
                httpMethod,
                pathPrefix, path,
                controllerType, controllerMethodName, controllerParams,
                new LinkedHashSet<>( Arrays.asList( modifiers ) )
            );
        }

        @Override
        public Route build()
        {
            return new RouteInstance(
                httpMethod,
                pathPrefix + path,
                controllerType, controllerMethodName, controllerParams,
                modifiers
            );
        }
    }

    private static final class RouteParserInstance
        implements RouteParser
    {
        private static final Logger LOG = LoggerFactory.getLogger( RouteParser.class );
        private final Application app;
        private final String pathPrefix;

        private RouteParserInstance( Application app, String pathPrefix )
        {
            this.app = app;
            this.pathPrefix = pathPrefix;
        }

        @Override
        public List<Route> routes( String routesString )
        {
            List<Route> routes = new ArrayList<>();
            Scanner scanner = new Scanner( routesString );
            while( scanner.hasNextLine() )
            {
                String routeString = scanner.nextLine().trim().replaceAll( "\\s+", " " );
                if( !routeString.startsWith( "#" ) && routeString.length() > 0 )
                {
                    routes.add( route( routeString ) );
                }
            }
            return routes;
        }

        @Override
        public Route route( String routeString )
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
                    controllerTypeEnd = cleanRouteString
                        .substring( 0, cleanRouteString.indexOf( '(', pathEnd ) )
                        .lastIndexOf( '.' );
                    controllerMethodEnd = cleanRouteString.indexOf( '(', controllerTypeEnd );
                    controllerParamsEnd = cleanRouteString.lastIndexOf( ')' );
                    modifiersEnd = cleanRouteString.length() > controllerParamsEnd + 1 ? cleanRouteString.length() : -1;
                }
                else if( cleanRouteString.indexOf( ' ', pathEnd + 1 ) > 0 )
                {
                    // No parenthesis with modifiers
                    controllerTypeEnd = cleanRouteString
                        .substring( 0, cleanRouteString.indexOf( ' ', pathEnd + 1 ) )
                        .lastIndexOf( '.' );
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

                final String httpMethod = cleanRouteString.substring( 0, methodEnd );
                final String path = cleanRouteString.substring( methodEnd + 1, pathEnd );
                final String controllerTypeName = cleanRouteString.substring( pathEnd + 1, controllerTypeEnd );
                final String controllerMethodName = cleanRouteString.substring(
                    controllerTypeEnd + 1,
                    controllerMethodEnd
                );
                final String controllerMethodParams;
                if( controllerParamsEnd != -1 )
                {
                    controllerMethodParams = cleanRouteString
                        .substring( controllerMethodEnd + 1, controllerParamsEnd )
                        .trim();
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
                Class<?> controllerType = app.classLoader().loadClass( controllerTypeName );

                // Parse controller method parameters
                ControllerParams controllerParams = parseControllerParams( app, routeString, controllerMethodParams );

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
                return new RouteInstance(
                    Method.valueOf( httpMethod ),
                    pathPrefix + path,
                    controllerType, controllerMethodName, controllerParams,
                    modifiers
                );
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

        private static ControllerParams parseControllerParams(
            Application application,
            String routeString,
            String controllerMethodParams
        )
            throws ClassNotFoundException
        {
            Map<String, ControllerParams.Param> controllerParams = new LinkedHashMap<>();
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
                        controllerParams.put( name, new ControllerParams.Param( name, type, forcedValue ) );
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
                            new ControllerParams.Param( paramName, lookupParamType( application, paramTypeName ) )
                        );
                    }
                }
            }
            return new ControllerParams( controllerParams );
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
    }
}
