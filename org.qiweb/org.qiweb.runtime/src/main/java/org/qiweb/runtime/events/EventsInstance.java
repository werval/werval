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
package org.qiweb.runtime.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.qiweb.api.events.Event;
import org.qiweb.api.events.Registration;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.events.EventsSPI;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Application Events Instance.
 */
public final class EventsInstance
    implements EventsSPI
{
    private final ApplicationSPI application;
    private final List<Consumer<Event>> listeners;

    public EventsInstance( ApplicationSPI application )
    {
        this.application = application;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public Registration registerListener( Consumer<Event> listener )
    {
        listeners.add( listener );
        return () -> listeners.remove( listener );
    }

    @Override
    public void emit( Event event )
    {
        if( application.executors().inDefaultExecutor() )
        {
            doEmit( event );
        }
        else
        {
            runAsync( () -> doEmit( event ), application.executor() );
        }
    }

    private void doEmit( Event event )
    {
        // Fail-safe
        List<Exception> errors = new ArrayList<>();
        for( Consumer<Event> listener : listeners )
        {
            try
            {
                listener.accept( event );
            }
            catch( Exception ex )
            {
                errors.add( ex );
            }
        }
        if( !errors.isEmpty() )
        {
            RuntimeException ex = new RuntimeException( "There were errors during " + event + " emission" );
            errors.forEach( error -> ex.addSuppressed( error ) );
            throw ex;
        }
    }

    public void unregisterAll()
    {
        listeners.clear();
    }
}
