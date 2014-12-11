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

import io.werval.api.Application;
import io.werval.api.exceptions.IllegalRouteException;
import io.werval.api.exceptions.WervalException;
import io.werval.api.http.Method;
import io.werval.api.routes.ControllerCallRecorder;
import io.werval.api.routes.ControllerParams;
import io.werval.api.routes.ControllerParams.Param;
import io.werval.api.routes.ControllerParams.ParamValue;
import io.werval.api.routes.Route;
import io.werval.api.routes.internal.RouteBuilderContext;
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
import org.qiweb.runtime.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.isEmpty;
import static io.werval.util.Strings.withHead;
import static io.werval.util.Strings.withoutTrail;
import static java.util.Collections.EMPTY_SET;
import static org.qiweb.runtime.ConfigKeys.QIWEB_ROUTES_IMPORTEDPACKAGES;

/**
 * RouteBuilder Instance.
 */
public class RouteBuilderInstance
    implements io.werval.api.routes.RouteBuilder
{
    private final Application app;
    private final String pathPrefix;

    /**
     * Create a new RouteBuilder instance that cannot parse.
     *
     * See {@link #RouteBuilderInstance(io.werval.api.Application)}.
     */
    public RouteBuilderInstance()
    {
        this( null, null );
    }

    /**
     * Create a new RouteBuilder instance that cannot parse and apply a path prefix to all built routes.
     *
     * See {@link #RouteBuilderInstance(io.werval.api.Application)}.
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
        if( isEmpty( pathPrefix ) )
        {
            this.pathPrefix = EMPTY;
        }
        else
        {
            // Prepend / and remove trailing / if necessary
            this.pathPrefix = withoutTrail( withHead( pathPrefix, "/" ), "/" );
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
            T controllerProxy = controllerType.isInterface()
                                ? newJavaProxy( controllerType, methodNameHolder )
                                : newJavassistProxy( controllerType, methodNameHolder );
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
                    throw new WervalException(
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

        private <T> T newJavaProxy( final Class<T> controllerType, final Holder<String> methodNameHolder )
        {
            return (T) Proxy.newProxyInstance(
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

        private <T> T newJavassistProxy( final Class<T> controllerType, final Holder<String> methodNameHolder )
        {
            ProxyFactory.ClassLoaderProvider previousJavassistLoader = ProxyFactory.classLoaderProvider;
            try
            {
                ProxyFactory.classLoaderProvider = new ProxyFactory.ClassLoaderProvider()
                {
                    @Override
                    public ClassLoader get( ProxyFactory proxyFactory )
                    {
                        return Thread.currentThread().getContextClassLoader();
                    }
                };
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass( controllerType );
                T controllerProxy = (T) proxyFactory.createClass().newInstance();
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
                return controllerProxy;
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new WervalException( "Unable to record controller method in RouteBuilder", ex );
            }
            finally
            {
                ProxyFactory.classLoaderProvider = previousJavassistLoader;
            }
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
        public List<Route> routes( String... routeStrings )
        {
            List<Route> routes = new ArrayList<>();
            if( routeStrings != null )
            {
                for( String routeString : routeStrings )
                {
                    routes.add( route( routeString ) );
                }
            }
            return routes;
        }

        @Override
        public Route route( String routeString )
        {
            if( isEmpty( routeString ) )
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
            Map<String, Param> controllerParams = new LinkedHashMap<>();
            if( controllerMethodParams.length() > 0 )
            {
                List<String> controllerMethodParamsSegments = splitControllerMethodParams( controllerMethodParams );
                for( String controllerMethodParamSegment : controllerMethodParamsSegments )
                {
                    switch( valueKindOfSegment( controllerMethodParamSegment ) )
                    {
                        case DEFAULTED:
                            // Parameter with default value
                            String[] dEqualSplitted = controllerMethodParamSegment.split( "\\?=", 2 );
                            String[] dTypeAndName = dEqualSplitted[0].trim().split( " " );
                            String dName = dTypeAndName[1];
                            Class<?> dType = lookupParamType( application, dTypeAndName[0] );
                            String defaultValueString = dEqualSplitted[1].trim();
                            if( defaultValueString.startsWith( "'" ) && defaultValueString.endsWith( "'" ) )
                            {
                                defaultValueString = defaultValueString
                                    .substring( 1, defaultValueString.length() - 1 )
                                    .replaceAll( "\\\\'", "'" );
                            }
                            Object defaultValue = application.parameterBinders().bind(
                                dType, dName, defaultValueString
                            );
                            controllerParams.put(
                                dName,
                                new Param( dName, dType, ParamValue.DEFAULTED, defaultValue )
                            );
                            break;
                        case FORCED:
                            // Parameter with forced value
                            String[] fEqualSplitted = controllerMethodParamSegment.split( "=", 2 );
                            String[] fTypeAndName = fEqualSplitted[0].trim().split( " " );
                            String fName = fTypeAndName[1];
                            Class<?> fType = lookupParamType( application, fTypeAndName[0] );
                            String forcedValueString = fEqualSplitted[1].trim();
                            if( forcedValueString.startsWith( "'" ) && forcedValueString.endsWith( "'" ) )
                            {
                                forcedValueString = forcedValueString
                                    .substring( 1, forcedValueString.length() - 1 )
                                    .replaceAll( "\\\\'", "'" );
                            }
                            Object forcedValue = application.parameterBinders().bind(
                                fType, fName, forcedValueString
                            );
                            controllerParams.put( fName, new Param( fName, fType, ParamValue.FORCED, forcedValue ) );
                            break;
                        case NONE:
                            // Parameter without forced value
                            String[] splitted = controllerMethodParamSegment.split( " " );
                            if( splitted.length != 2 )
                            {
                                throw new IllegalRouteException(
                                    routeString,
                                    "Unable to parse parameter: " + controllerMethodParamSegment
                                );
                            }
                            String name = splitted[1];
                            String paramTypeName = splitted[0];
                            Class<?> type = lookupParamType( application, paramTypeName );
                            controllerParams.put( name, new Param( name, type ) );
                            break;
                        default:
                    }
                }
            }
            return new ControllerParams( controllerParams );
        }

        private static List<String> splitControllerMethodParams( String controllerMethodParams )
        {
            List<String> segments = new ArrayList<>();
            boolean insideQuotes = false;
            int previous = controllerMethodParams.codePointAt( 0 );
            StringBuilder sb = new StringBuilder().append( Character.toChars( previous ) );
            for( int idx = Character.charCount( previous ); idx < controllerMethodParams.length(); )
            {
                int character = controllerMethodParams.codePointAt( idx );
                if( character == '\'' && previous != '\\' )
                {
                    insideQuotes = !insideQuotes;
                }
                if( insideQuotes || character != ',' )
                {
                    sb.append( Character.toChars( character ) );
                }
                else
                {
                    segments.add( sb.toString().trim() );
                    sb = new StringBuilder();
                }
                previous = character;
                idx += Character.charCount( character );
            }
            segments.add( sb.toString().trim() );
            return segments;
        }

        private static ParamValue valueKindOfSegment( String segment )
        {
            if( segment.length() < 2 )
            {
                return ParamValue.NONE;
            }
            boolean insideQuotes = false;
            int previous = segment.codePointAt( 0 );
            for( int idx = Character.charCount( previous ); idx < segment.length(); )
            {
                int character = segment.codePointAt( idx );
                if( character == '\'' && previous != '\\' )
                {
                    insideQuotes = !insideQuotes;
                }
                if( '=' == character && !insideQuotes )
                {
                    if( '?' == previous )
                    {
                        return ParamValue.DEFAULTED;
                    }
                    return ParamValue.FORCED;
                }
                previous = character;
                idx += Character.charCount( character );
            }
            return ParamValue.NONE;
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
