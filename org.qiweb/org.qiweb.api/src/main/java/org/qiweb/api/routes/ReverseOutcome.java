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
import java.util.Collections;
import java.util.List;
import org.qiweb.api.http.Headers;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.http.StatusClass;

/**
 * Reverse Outcome.
 * @hidden
 */
public final class ReverseOutcome
    implements Outcome
{

    private final String httpMethod;
    private final Class<?> controllerType;
    private final Method controllerMethod;
    private final List<Object> parameters;

    /* package */ ReverseOutcome( String method, Class<?> controllerType, Method controllerMethod, List<Object> parameters )
    {
        this.httpMethod = method;
        this.controllerType = controllerType;
        this.controllerMethod = controllerMethod;
        this.parameters = parameters;
    }

    /**
     * @return HTTP Method
     */
    public String httpMethod()
    {
        return httpMethod;
    }

    /**
     * @return Controller type
     */
    public Class<?> controllerType()
    {
        return controllerType;
    }

    /**
     * @return Controller method
     */
    public Method controllerMethod()
    {
        return controllerMethod;
    }

    /**
     * @return Controller parameters
     */
    public List<Object> parameters()
    {
        return Collections.unmodifiableList( parameters );
    }

    @Override
    public int status()
    {
        throw new UnsupportedOperationException( "ReverseOutcome has no status." );
    }

    @Override
    public StatusClass statusClass()
    {
        throw new UnsupportedOperationException( "ReverseOutcome has no status." );
    }

    @Override
    public Headers headers()
    {
        throw new UnsupportedOperationException( "ReverseOutcome has no headers." );
    }

}
