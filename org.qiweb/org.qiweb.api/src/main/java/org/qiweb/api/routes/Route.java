/**
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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestHeader;

/**
 * Route.
 * <p>HTTP RequestHeader satisfiedBy.</p>
 */
public interface Route
{

    /**
     * @return HTTP Method
     */
    String httpMethod();

    /**
     * @return Path
     */
    String path();

    /**
     * @return Controller type
     */
    Class<?> controllerType();

    /**
     * @return Controller method
     */
    Method controllerMethod();

    /**
     * @return Controller method name
     */
    String controllerMethodName();

    /**
     * @param requestHeader Request header
     * @return TRUE if the Route is satisfied by the given request header, otherwise return FALSE
     */
    boolean satisfiedBy( RequestHeader requestHeader );

    /**
     * @return Set of modifiers
     */
    Set<String> modifiers();

    /**
     * Bind route parameters from path and query string to a Map&lt;String,Object&gt;.
     *
     * @param parameterBinders Parameter binders
     * @param path Path
     * @param queryString Query String
     * @return Map of bound parameters
     */
    Map<String, Object> bindParameters( ParameterBinders parameterBinders, String path, QueryString queryString );

    /**
     * Unbind route URI (path and query string) from parameters values Map.
     *
     * @param parameterBinders Parameter binders
     * @param parameters Parameters values Map
     * @return String representation to a URI of this route with the given parameters values
     */
    String unbindParameters( ParameterBinders parameterBinders, Map<String, Object> parameters );

}
