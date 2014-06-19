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
package org.qiweb.runtime.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Named ThreadFactory.
 */
public final class NamedThreadFactory
    implements ThreadFactory
{
    private final AtomicInteger count = new AtomicInteger();
    private final String name;
    private final Thread.UncaughtExceptionHandler exceptionHandler;

    public NamedThreadFactory( String name )
    {
        this( name, null );
    }

    public NamedThreadFactory( String name, Thread.UncaughtExceptionHandler exceptionHandler )
    {
        this.name = name;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Thread newThread( Runnable runnable )
    {
        Thread thread = Executors.defaultThreadFactory().newThread( runnable );
        thread.setName( name + "-" + count.incrementAndGet() );
        if( exceptionHandler != null )
        {
            thread.setUncaughtExceptionHandler( exceptionHandler );
        }
        return thread;
    }
}
