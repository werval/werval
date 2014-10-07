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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.http.Method;
import org.qiweb.api.routes.ControllerCallRecorder;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;

import static org.qiweb.api.http.Method.CONNECT;
import static org.qiweb.api.http.Method.DELETE;
import static org.qiweb.api.http.Method.GET;
import static org.qiweb.api.http.Method.HEAD;
import static org.qiweb.api.http.Method.OPTIONS;
import static org.qiweb.api.http.Method.PATCH;
import static org.qiweb.api.http.Method.POST;
import static org.qiweb.api.http.Method.PUT;
import static org.qiweb.api.http.Method.TRACE;
import static org.qiweb.util.IllegalArguments.ensureNotNull;

public class ReverseRoutesInstance
    implements ReverseRoutes
{
    private final Application application;

    public ReverseRoutesInstance( Application application )
    {
        this.application = application;
    }

    @Override
    public <T> ReverseRoute options( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( OPTIONS, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute get( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( GET, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute head( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( HEAD, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute post( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( POST, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute put( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( PUT, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute delete( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( DELETE, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute trace( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( TRACE, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute patch( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( PATCH, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute connect( Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( CONNECT, controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute of( String httpMethod, Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        return of( Method.valueOf( httpMethod ), controllerType, callRecorder );
    }

    @Override
    public <T> ReverseRoute of( Method httpMethod, Class<T> controllerType, ControllerCallRecorder<T> callRecorder )
    {
        ensureNotNull( "HTTP method", httpMethod );
        ensureNotNull( "Controller Type", controllerType );
        ensureNotNull( "Call Recorder", callRecorder );

        // Prepare recording proxy
        T controllerProxy;
        ControllerCallHandler handler = new ControllerCallHandler();
        if( controllerType.isInterface() )
        {
            Class<?>[] interfaces = new Class<?>[ 1 ];
            interfaces[0] = controllerType;
            controllerProxy = (T) java.lang.reflect.Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                interfaces,
                handler
            );
        }
        else
        {
            try
            {
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass( controllerType );
                controllerProxy = (T) proxyFactory.createClass().newInstance();
                ( (Proxy) controllerProxy ).setHandler( handler );
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new QiWebException( "Unable to reverse route", ex );
            }
        }

        // Record Controller Method Call
        try
        {
            callRecorder.recordCall( controllerProxy );
        }
        catch( Exception ex )
        {
            throw new QiWebException(
                "Error while recording Controller call for Reverse routing: " + ex.getMessage(),
                ex
            );
        }

        // Find Route
        for( Route route : application.routes() )
        {
            if( route.httpMethod().equals( httpMethod )
                && route.controllerType().getName().equals( controllerType.getName() )
                && route.controllerMethodName().equals( handler.methodName )
                && Arrays.equals( route.controllerMethod().getParameterTypes(), handler.paramsTypes ) )
            {
                // Found!
                RouteInstance instance = (RouteInstance) route;
                Map<String, Object> parameters = new LinkedHashMap<>();
                int idx = 0;
                for( String paramName : instance.controllerParams().names() )
                {
                    parameters.put( paramName, handler.paramsValues[idx] );
                    idx++;
                }
                String unboundPath = route.unbindParameters( application.parameterBinders(), parameters );
                return new ReverseRouteInstance( httpMethod, unboundPath, application.defaultCharset() );
            }
        }
        // No matching Route Found
        throw new RouteNotFoundException(
            httpMethod,
            controllerType + "." + handler.methodName
            + "(" + Arrays.toString( handler.paramsTypes ) + ")"
        );
    }

    private static class ControllerCallHandler
        implements MethodHandler, InvocationHandler
    {
        private String methodName;
        private Class<?>[] paramsTypes;
        private Object[] paramsValues;

        @Override
        public Object invoke( Object proxy, java.lang.reflect.Method controllerMethod,
                              java.lang.reflect.Method proceed, Object[] args )
            throws Throwable
        {
            return invoke( proxy, controllerMethod, args );
        }

        @Override
        public Object invoke( Object proxy, java.lang.reflect.Method controllerMethod, Object[] args )
            throws Throwable
        {
            methodName = controllerMethod.getName();
            paramsTypes = Arrays.stream( args )
                .map( obj -> obj.getClass() )
                .collect( Collectors.toList() )
                .toArray( new Class<?>[ 0 ] );
            paramsValues = args;
            return null;
        }
    }
}
