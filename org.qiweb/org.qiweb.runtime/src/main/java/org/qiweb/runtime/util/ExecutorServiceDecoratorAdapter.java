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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ExecutorServiceDecoratorAdapter.
 */
public class ExecutorServiceDecoratorAdapter
    implements ExecutorService
{
    protected final ExecutorService decorated;

    public ExecutorServiceDecoratorAdapter( ExecutorService decorated )
    {
        this.decorated = decorated;
    }

    @Override
    public void shutdown()
    {
        decorated.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return decorated.shutdownNow();
    }

    @Override
    public boolean isShutdown()
    {
        return decorated.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return decorated.isTerminated();
    }

    @Override
    public boolean awaitTermination( long timeout, TimeUnit unit )
        throws InterruptedException
    {
        return decorated.awaitTermination( timeout, unit );
    }

    @Override
    public <T> Future<T> submit( Callable<T> task )
    {
        return decorated.submit( task );
    }

    @Override
    public <T> Future<T> submit( Runnable task, T result )
    {
        return decorated.submit( task, result );
    }

    @Override
    public Future<?> submit( Runnable task )
    {
        return decorated.submit( task );
    }

    @Override
    public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks )
        throws InterruptedException
    {
        return decorated.invokeAll( tasks );
    }

    @Override
    public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
        throws InterruptedException
    {
        return decorated.invokeAll( tasks, timeout, unit );
    }

    @Override
    public <T> T invokeAny( Collection<? extends Callable<T>> tasks )
        throws InterruptedException, ExecutionException
    {
        return decorated.invokeAny( tasks );
    }

    @Override
    public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return decorated.invokeAny( tasks, timeout, unit );
    }

    @Override
    public void execute( Runnable command )
    {
        decorated.execute( command );
    }
}
