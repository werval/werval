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
package io.werval.runtime.events;

import java.util.function.Consumer;

import io.werval.api.events.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Events Logger.
 */
public class EventsLogger
    implements Consumer<Event>
{
    private static final Logger LOG = LoggerFactory.getLogger( EventsLogger.class );

    @Override
    public void accept( Event event )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( event.toString() );
        }
    }
}
