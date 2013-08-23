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
package org.qiweb.api.exceptions;

/**
 * Thrown when trying to build a Route instance with an illegal route string.
 *
 * @extends QiWebException
 */
public class IllegalRouteException
    extends QiWebException
{

    private static final long serialVersionUID = 1L;
    private String routeString;

    public IllegalRouteException( String routeString, String message )
    {
        super( routeString + "\n" + message );
        this.routeString = routeString;
    }

    public IllegalRouteException( String routeString, String message, Throwable cause )
    {
        super( routeString + "\n" + message, cause );
    }

    public String routeString()
    {
        return routeString;
    }
}
