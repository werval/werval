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
package org.qiweb.api.http;

import java.util.EnumSet;

/**
 * HTTP Status Class.
 */
public enum StatusClass
{
    /**
     * 1xx Informational.
     */
    INFORMATIONAL( "Informational" ),
    /**
     * 2xx Success.
     */
    SUCCESS( "Successful" ),
    /**
     * 3xx Redirection.
     */
    REDIRECTION( "Redirection" ),
    /**
     * 4xx Client Error.
     */
    CLIENT_ERROR( "Client Error" ),
    /**
     * 5xx Server Error.
     */
    SERVER_ERROR( "Server Error" ),
    /**
     * {@literal 0xx} or {@literal >= 6xx} Unknown.
     */
    UNKNOWN( "Unknown Status" );

    private static final EnumSet<StatusClass> FORCE_CLOSE = EnumSet.of( CLIENT_ERROR, SERVER_ERROR, UNKNOWN );
    private static final int ONE_HUNDRED = 100;
    private static final int TWO_HUNDRED = 200;
    private static final int THREE_HUNDRED = 300;
    private static final int FOUR_HUNDRED = 400;
    private static final int FIVE_HUNDRED = 500;
    private static final int SIX_HUNDRED = 600;

    /**
     * @param status Status
     *
     * @return StatusClass for the given Status
     */
    public static StatusClass valueOf( Status status )
    {
        return valueOf( status.code() );
    }

    /**
     * @param status Status code
     *
     * @return StatusClass for the given status code
     */
    public static StatusClass valueOf( int status )
    {
        if( status < ONE_HUNDRED )
        {
            // 0xx
            return UNKNOWN;
        }
        if( status < TWO_HUNDRED )
        {
            // 1xx
            return INFORMATIONAL;
        }
        if( status < THREE_HUNDRED )
        {
            // 2xx
            return SUCCESS;
        }
        if( status < FOUR_HUNDRED )
        {
            // 3xx
            return REDIRECTION;
        }
        if( status < FIVE_HUNDRED )
        {
            // 4xx
            return CLIENT_ERROR;
        }
        if( status < SIX_HUNDRED )
        {
            // 5xx
            return SERVER_ERROR;
        }
        // >= 6xx
        return UNKNOWN;
    }

    private final String reasonPhrase;

    private StatusClass( String reasonPhrase )
    {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * @return Status class reason phrase
     */
    public String reasonPhrase()
    {
        return reasonPhrase;
    }

    /**
     * @return {@literal true} if this StatusClass force the connection close,
     *         otherwise return {@literal false}
     */
    public boolean isForceClose()
    {
        return FORCE_CLOSE.contains( this );
    }
}
