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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ForkJoinPool Named ThreadFactory.
 */
public final class ForkJoinPoolNamedThreadFactory
    implements ForkJoinPool.ForkJoinWorkerThreadFactory
{
    private final AtomicInteger count = new AtomicInteger();
    private final String name;

    public ForkJoinPoolNamedThreadFactory( String name )
    {
        this.name = name;
    }

    @Override
    public ForkJoinWorkerThread newThread( ForkJoinPool pool )
    {
        ForkJoinNamedWorkerThread thread = new ForkJoinNamedWorkerThread( pool );
        thread.setName( name + "-" + count.incrementAndGet() );
        return thread;
    }

    private static final class ForkJoinNamedWorkerThread
        extends ForkJoinWorkerThread
    {
        private ForkJoinNamedWorkerThread( ForkJoinPool pool )
        {
            super( pool );
        }
    }
}
