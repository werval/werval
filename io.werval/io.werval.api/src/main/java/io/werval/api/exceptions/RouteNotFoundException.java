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
package io.werval.api.exceptions;

import io.werval.api.http.Method;

/**
 * Thrown when no satisfying Route found.
 */
public class RouteNotFoundException
    extends WervalException
{
    private static final long serialVersionUID = 1L;
    private final Method method;
    private final String uri;

    /**
     * Create a new RouteNotFoundException.
     *
     * @param method HTTP method
     * @param uri    HTTP URI
     */
    public RouteNotFoundException( Method method, String uri )
    {
        super( "No route for " + method + " " + uri );
        this.method = method;
        this.uri = uri;
    }

    /**
     * @return HTTP method
     */
    public final Method method()
    {
        return method;
    }

    /**
     * @return HTTP URI
     */
    public final String uri()
    {
        return uri;
    }
}
