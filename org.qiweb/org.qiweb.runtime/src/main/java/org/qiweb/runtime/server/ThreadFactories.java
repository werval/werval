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
package org.qiweb.runtime.server;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Internal Thread Factories.
 */
@SuppressWarnings( "PackageVisibleInnerClass" )
/* package */ interface ThreadFactories
{
    /**
     * Acceptor Threads Factory.
     */
    /* package */ static class Acceptors
        implements ThreadFactory
    {
        private final AtomicLong count = new AtomicLong( 0L );

        @Override
        public Thread newThread( Runnable runnable )
        {
            return new Thread( runnable, "qiweb-acceptor-" + count.getAndIncrement() );
        }
    }

    /**
     * I/O Threads Factory.
     */
    /* package */ static class IO
        implements ThreadFactory
    {
        private final AtomicLong count = new AtomicLong( 0L );

        @Override
        public Thread newThread( Runnable runnable )
        {
            return new Thread( runnable, "qiweb-io-" + count.getAndIncrement() );
        }
    }

    /**
     * HTTP Executor Threads Factory.
     */
    /* package */ static class HttpExecutors
        implements ThreadFactory
    {
        private final AtomicLong count = new AtomicLong( 0L );

        @Override
        public Thread newThread( Runnable runnable )
        {
            return new Thread( runnable, "http-executor-" + count.getAndIncrement() );
        }
    }

}
