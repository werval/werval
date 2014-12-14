/*
 * Copyright (c) 2014 the original author or authors
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
package io.werval.api.events;

/**
 * Connection Event.
 * <p>
 * Marker interface for all connection events.
 */
public abstract class ConnectionEvent
    implements Event
{
    protected final String remoteHostString;

    protected ConnectionEvent( String remoteHostString )
    {
        this.remoteHostString = remoteHostString;
    }

    public final String remoteHostString()
    {
        return remoteHostString;
    }

    /**
     * Connection Opened Event.
     */
    public static final class Opened
        extends ConnectionEvent
    {
        public Opened( String remoteHostString )
        {
            super( remoteHostString );
        }

        @Override
        public String toString()
        {
            return "Connection Opened (" + remoteHostString + ')';
        }
    }

    /**
     * Connection Closed Event.
     */
    public static final class Closed
        extends ConnectionEvent
    {
        public Closed( String remoteHostString )
        {
            super( remoteHostString );
        }

        @Override
        public String toString()
        {
            return "Connection Closed (" + remoteHostString + ')';
        }
    }
}
