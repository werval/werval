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
package org.qiweb.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.qiweb.api.Config;
import org.qiweb.api.Error;
import org.qiweb.api.Errors;

import static org.qiweb.runtime.ConfigKeys.APP_ERRORS_RECORD_MAX;

/**
 * Application Errors Instance.
 */
// TODO Document configuration of ErrorsInstance
public class ErrorsInstance
    implements Errors
{

    private static final class ErrorInstance
        implements Error, Comparable<Error>
    {

        final Long timestamp;
        final String errorId;
        final String requestId;
        final String message;
        final Throwable cause;

        private ErrorInstance( long timestamp, String errorId, String requestId, String message, Throwable cause )
        {
            this.timestamp = timestamp;
            this.errorId = errorId;
            this.requestId = requestId;
            this.message = message;
            this.cause = cause;
        }

        @Override
        public long timestamp()
        {
            return timestamp;
        }

        @Override
        public String errorId()
        {
            return errorId;
        }

        @Override
        public String requestId()
        {
            return requestId;
        }

        @Override
        public String message()
        {
            return message;
        }

        @Override
        public Throwable cause()
        {
            return cause;
        }

        @Override
        public int compareTo( Error o )
        {
            return timestamp.compareTo( o.timestamp() );
        }

        @Override
        public String toString()
        {
            return "Error( " + errorId + ", " + requestId + ", " + cause.getClass().getSimpleName() + " )";
        }
    }
    private static final String ERROR_IDENTITY_PREFIX = UUID.randomUUID().toString() + "-";
    private static final AtomicLong ERROR_IDENTITY_COUNT = new AtomicLong();

    private static String generateNewErrorIdentity()
    {
        return ERROR_IDENTITY_PREFIX + ERROR_IDENTITY_COUNT.getAndIncrement();
    }
    private final Config config;
    private final Map<String, Error> errors;

    public ErrorsInstance( Config config )
    {
        this.config = config;
        this.errors = new TreeMap<>();
    }

    @Override
    public Error record( String requestId, String message, Throwable cause )
    {
        String errorId = generateNewErrorIdentity();
        Error error = new ErrorInstance( System.currentTimeMillis(), errorId, requestId, message, cause );
        errors.put( errorId, error );
        while( errors.size() > config.intNumber( APP_ERRORS_RECORD_MAX ) )
        {
            errors.remove( errors.keySet().iterator().next() );
        }
        return error;
    }

    @Override
    public int size()
    {
        return errors.size();
    }

    @Override
    public void clear()
    {
        errors.clear();
    }

    @Override
    public Error get( String errorId )
    {
        return errors.get( errorId );
    }

    @Override
    public List<Error> ofRequest( String requestId )
    {
        List<Error> requestErrors = new ArrayList<>();
        for( Map.Entry<String, Error> entry : errors.entrySet() )
        {
            Error error = entry.getValue();
            if( requestId.equals( error.requestId() ) )
            {
                requestErrors.add( error );
            }
        }
        Collections.reverse( requestErrors );
        return Collections.unmodifiableList( requestErrors );
    }

    @Override
    public Iterator<Error> iterator()
    {
        return errors.values().iterator();
    }
}
