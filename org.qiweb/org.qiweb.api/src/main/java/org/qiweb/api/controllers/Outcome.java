package org.qiweb.api.controllers;

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
        informational,
        /**
         * 2xx Success.
         */
        success,
        /**
         * 3xx Redirection.
         */
        redirection,
        /**
         * 4xx Client Error.
         */
        clientError,
        /**
         * 5xx Server Error.
         */
        serverError,
        /**
         * 0xx or >= 6xx Unknown
         */
        unknown;

        public static StatusClass valueOf( int status )
        {
            if( status < 100 )
            {
                // 0xx
                return unknown;
            }
            if( status < 200 )
            {
                // 1xx
                return informational;
            }
            if( status < 300 )
            {
                // 2xx
                return success;
            }
            if( status < 400 )
            {
                // 3xx
                return redirection;
            }
            if( status < 500 )
            {
                // 4xx
                return clientError;
            }
            if( status < 600 )
            {
                // 5xx
                return serverError;
            }
            // >= 6xx
            return unknown;
        }
    }

    int status();

    StatusClass statusClass();

    Headers headers();
}
