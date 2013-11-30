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
import java.util.Objects;
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
public final class ErrorsInstance
    implements Errors
{

    private static final class ErrorInstance
        implements Error
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
        public String toString()
        {
            return "Error( " + errorId + ", " + requestId + ", " + cause.getClass().getSimpleName() + " )";
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode( this.errorId );
            return hash;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( obj == null )
            {
                return false;
            }
            if( getClass() != obj.getClass() )
            {
                return false;
            }
            final ErrorInstance other = (ErrorInstance) obj;
            return Objects.equals( this.errorId, other.errorId );
        }

    }

    private final Config config;
    private final Map<String, Error> errors;
    private String errorIdentityPrefix;
    private AtomicLong errorIdentityCount;

    public ErrorsInstance( Config config )
    {
        this.config = config;
        this.errors = new TreeMap<>( (o1, o2) -> o2.compareTo( o1 ) );
        resetErrorIdentity();
    }

    private void resetErrorIdentity()
    {
        errorIdentityPrefix = UUID.randomUUID().toString() + "-";
        errorIdentityCount = new AtomicLong();
    }

    private String generateNewErrorIdentity()
    {
        // Left pad incremented error count with zeroes
        // Pad size is max recorded error length + 1
        return errorIdentityPrefix
               + String.format( "%0" + ( config.string( APP_ERRORS_RECORD_MAX ).length() + 1 ) + "d",
                                errorIdentityCount.getAndIncrement() );
    }

    @Override
    public Iterator<Error> iterator()
    {
        return errors.values().iterator();
    }

    @Override
    public List<Error> asList()
    {
        return Collections.unmodifiableList( new ArrayList<>( errors.values() ) );
    }

    @Override
    public Error record( String requestId, String message, Throwable cause )
    {
        String errorId = generateNewErrorIdentity();
        Error error = new ErrorInstance( System.currentTimeMillis(), errorId, requestId, message, cause );
        errors.put( errorId, error );
        while( errors.size() > config.intNumber( APP_ERRORS_RECORD_MAX ) )
        {
            List<String> keys = new ArrayList<>( errors.keySet() );
            errors.remove( keys.get( keys.size() - 1 ) );
        }
        return error;
    }

    @Override
    public int count()
    {
        return errors.size();
    }

    @Override
    public synchronized void clear()
    {
        errors.clear();
        resetErrorIdentity();
    }

    @Override
    public Error get( String errorId )
    {
        return errors.get( errorId );
    }

    @Override
    public Error last()
    {
        if( errors.isEmpty() )
        {
            return null;
        }
        return errors.get( errors.keySet().iterator().next() );
    }

    @Override
    public List<Error> ofRequest( String requestId )
    {
        if( errors.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<Error> requestErrors = new ArrayList<>();
        for( Map.Entry<String, Error> entry : errors.entrySet() )
        {
            Error error = entry.getValue();
            if( requestId.equals( error.requestId() ) )
            {
                requestErrors.add( error );
            }
        }
        return Collections.unmodifiableList( requestErrors );
    }

    @Override
    public Error lastOfRequest( String requestIdentity )
    {
        if( errors.isEmpty() )
        {
            return null;
        }
        List<Error> ofRequest = ofRequest( requestIdentity );
        if( ofRequest.isEmpty() )
        {
            return null;
        }
        return ofRequest.get( 0 );
    }

}
