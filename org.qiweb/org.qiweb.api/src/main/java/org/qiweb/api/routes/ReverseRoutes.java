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
package org.qiweb.api.routes;

/**
 * Reverse Routes.
 */
public interface ReverseRoutes
{
    /**
     * ReverseRoute for an OPTIONS method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute options( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a GET method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute get( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a HEAD method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute head( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a POST method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute post( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a PUT method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute put( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a DELETE method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute delete( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a TRACE method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute trace( Class<T> controllerType, ControllerCallRecorder<T> callRecorder );

    /**
     * ReverseRoute for a given method on a Controller call.
     *
     * @param <T>            Parameterized type of the Controller
     * @param httpMethod     HTTP method
     * @param controllerType Controller Type
     * @param callRecorder   Closure to call the target Controller Method
     *
     * @return a ReverseRoute
     *
     * @throws org.qiweb.api.exceptions.RouteNotFoundException if a corresponding {@literal Route} cannot be found
     */
    <T> ReverseRoute of( String httpMethod, Class<T> controllerType, ControllerCallRecorder<T> callRecorder );
}
