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
package org.qiweb.api.outcomes;

import org.qiweb.api.http.Headers;

/**
 * Outcome of a HTTP Request processing.
 */
public interface Outcome
{

    /**
     * Outcome Status Class.
     */
    enum StatusClass
    {

        /**
         * 1xx Informational.
         */
        INFORMATIONAL,
        /**
         * 2xx Success.
         */
        SUCCESS,
        /**
         * 3xx Redirection.
         */
        REDIRECTION,
        /**
         * 4xx Client Error.
         */
        CLIENT_ERROR,
        /**
         * 5xx Server Error.
         */
        SERVER_ERROR,
        /**
         * 0xx or >= 6xx Unknown
         */
        UNKNOWN;

        // CHECKSTYLE:OFF
        public static StatusClass valueOf( int status )
        {
            if( status < 100 )
            {
                // 0xx
                return UNKNOWN;
            }
            if( status < 200 )
            {
                // 1xx
                return INFORMATIONAL;
            }
            if( status < 300 )
            {
                // 2xx
                return SUCCESS;
            }
            if( status < 400 )
            {
                // 3xx
                return REDIRECTION;
            }
            if( status < 500 )
            {
                // 4xx
                return CLIENT_ERROR;
            }
            if( status < 600 )
            {
                // 5xx
                return SERVER_ERROR;
            }
            // >= 6xx
            return UNKNOWN;
        }

        // CHECKSTYLE:ON
    }

    /**
     * @return Outcome HTTP status
     */
    int status();

    /**
     * @return Outcome HTTP status class
     */
    StatusClass statusClass();

    /**
     @return Outcome HTTP headers
     */
    Headers headers();

}
