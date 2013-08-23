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
 *
 * @navassoc - - - ParameterBinders
 */
public interface Route
{

    String httpMethod();

    String path();

    Class<?> controllerType();

    Method controllerMethod();

    String controllerMethodName();

    boolean satisfiedBy( RequestHeader requestHeader );

    Set<String> modifiers();

    Map<String, Object> bindParameters( ParameterBinders parameterBinders, String path, QueryString queryString );

    String unbindParameters( ParameterBinders parameterBinders, Map<String, Object> parameters );
}
