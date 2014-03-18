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
package org.qiweb.api.routes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.outcomes.Outcome;

import static java.util.Arrays.asList;

/**
 * Reverse Routes.
 */
// TODO Remove Javassist dependency in API!
public abstract class ReverseRoutes
{
    /**
     * Generate controller dynamic proxy that record method calls for a OPTIONS method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T OPTIONS( Class<T> controllerType )
    {
        return HTTP( "OPTIONS", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a GET method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T GET( Class<T> controllerType )
    {
        return HTTP( "GET", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a HEAD method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T HEAD( Class<T> controllerType )
    {
        return HTTP( "HEAD", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a POST method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T POST( Class<T> controllerType )
    {
        return HTTP( "POST", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a PUT method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T PUT( Class<T> controllerType )
    {
        return HTTP( "PUT", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a DELETE method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T DELETE( Class<T> controllerType )
    {
        return HTTP( "DELETE", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls for a TRACE method.
     *
     * @param <T>            Parameterized controller type
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    public static <T> T TRACE( Class<T> controllerType )
    {
        return HTTP( "TRACE", controllerType );
    }

    /**
     * Generate controller dynamic proxy that record method calls.
     *
     * @param <T>            Parameterized controller type
     * @param httpMethod     HTTP Method
     * @param controllerType controller type
     *
     * @return Dynamic proxy of the controller type that record method calls
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T HTTP( final String httpMethod, final Class<T> controllerType )
    {
        T controllerProxy;
        if( controllerType.isInterface() )
        {
            controllerProxy = (T) java.lang.reflect.Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]
                {
                    controllerType
                },
                new InvocationHandler()
                {
                    @Override
                    public Object invoke( Object proxy, Method controllerMethod, Object[] args )
                    {
                        return new ReverseOutcome( httpMethod, controllerType, controllerMethod, asList( args ) );
                    }
                } );
        }
        else
        {
            try
            {
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass( controllerType );
                controllerProxy = (T) proxyFactory.createClass().newInstance();
                ( (Proxy) controllerProxy ).setHandler( new MethodHandler()
                {
                    @Override
                    public Object invoke( Object self, Method controllerMethod, Method proceed, Object[] args )
                    {
                        return new ReverseOutcome( httpMethod, controllerType, controllerMethod, asList( args ) );
                    }
                } );
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new QiWebException( "Unable to reverse route", ex );
            }
        }
        return controllerProxy;
    }

    /**
     * Get the ReverseRoute of a Controller call.
     *
     * @param reverseOutcome Reverse Outcome
     *
     * @return a ReverseRoute
     *
     * @throws IllegalArgumentException when the given Outcome is not appropriate
     */
    public abstract ReverseRoute of( Outcome reverseOutcome );
}
