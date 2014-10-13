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
package org.qiweb.api;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Application Executors.
 */
public interface ApplicationExecutors
{
    /**
     * Default Application Executor.
     *
     * @return Default Application Executor
     */
    ExecutorService defaultExecutor();

    /**
     * Check if current thread is in the default Application Executor.
     *
     * @return {@literal TRUE} if in default Application Executor, {@literal FALSE} otherwise
     */
    boolean inDefaultExecutor();

    /**
     * Application Executor by name.
     *
     * @param executorName The name of a configured Application Executor
     *
     * @return The Application Executor or null if no Executor for the given name
     */
    ExecutorService executor( String executorName );

    /**
     * Check if current thread is in a configured Application Executor.
     *
     * @param executorName The name of a configured Application Executor
     *
     * @return {@literal TRUE} if in the Application Executor named {@literal name}, {@literal FALSE} otherwise.
     */
    boolean inExecutor( String executorName );

    /**
     * Executes the given command at some time in the future in the default Application Executor.
     * <p>
     * See {@link ExecutorService#execute(java.lang.Runnable)}.
     *
     * @param task The runnable task
     */
    default void execute( Runnable task )
    {
        defaultExecutor().execute( task );
    }

    /**
     * Executes the given command at some time in the future in a configured Application Executor.
     * <p>
     * See {@link ExecutorService#execute(java.lang.Runnable)}.
     *
     * @param executorName The name of a configured Application Executor
     * @param task         The runnable task
     */
    default void execute( String executorName, Runnable task )
    {
        executor( executorName ).execute( task );
    }

    /**
     * Submits a value-returning task to the default Application Executor and returns a Future representing the pending
     * results of the task.
     * <p>
     * See {@link ExecutorService#submit(java.util.concurrent.Callable)}.
     *
     * @param <T>  the type of the task's result
     * @param task the task to submit
     *
     * @return a Future representing pending completion of the task
     */
    default <T> Future<T> submit( Callable<T> task )
    {
        return defaultExecutor().submit( task );
    }

    /**
     * Submits a value-returning task to a configured Application Executor and returns a Future representing the pending
     * results of the task.
     * <p>
     * See {@link ExecutorService#submit(java.util.concurrent.Callable)}.
     *
     * @param <T>          The type of the task's result
     * @param executorName The name of a configured Application Executor
     * @param task         The task to submit
     *
     * @return a Future representing pending completion of the task
     */
    default <T> Future<T> submit( String executorName, Callable<T> task )
    {
        return executor( executorName ).submit( task );
    }

    /**
     * Submits a Runnable task to the default Application Executor and returns a Future representing that task.
     * <p>
     * The Future's get method will return null upon successful completion
     * See {@link ExecutorService#submit(java.lang.Runnable)}.
     *
     * @param task The task to submit
     *
     * @return a Future representing pending completion of the task
     */
    default Future<?> submit( Runnable task )
    {
        return defaultExecutor().submit( task );
    }

    /**
     * Submits a Runnable task to a configured Application Executor and returns a Future representing that task.
     * <p>
     * The Future's get method will return null upon successful completion
     * See {@link ExecutorService#submit(java.lang.Runnable)}.
     *
     * @param executorName The name of a configured Application Executor
     * @param task         The task to submit
     *
     * @return a Future representing pending completion of the task
     */
    default Future<?> submit( String executorName, Runnable task )
    {
        return executor( executorName ).submit( task );
    }

    /**
     * Submits a Runnable task to the default Application Executor and returns a Future representing that task.
     * <p>
     * The Future's get method will return the given result upon successful completion.
     * See {@link ExecutorService#submit(java.lang.Runnable)}.
     *
     * @param <T>    The type of the result
     * @param task   The task to submit
     * @param result The result to return
     *
     * @return a Future representing pending completion of the task
     */
    default <T> Future<T> submit( Runnable task, T result )
    {
        return defaultExecutor().submit( task, result );
    }

    /**
     * Submits a Runnable task to a configured Application Executor and returns a Future representing that task.
     * <p>
     * The Future's get method will return the given result upon successful completion.
     * See {@link ExecutorService#submit(java.lang.Runnable)}.
     *
     * @param <T>          The type of the result
     * @param executorName The name of a configured Application Executor
     * @param task         The task to submit
     * @param result       The result to return
     *
     * @return a Future representing pending completion of the task
     */
    default <T> Future<T> submit( String executorName, Runnable task, T result )
    {
        return executor( executorName ).submit( task, result );
    }

    /**
     * Executes the given tasks on the default Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do.
     * <p>
     * See {@link ExecutorService#invokeAny(java.util.Collection)}.
     *
     * @param <T>   The type of the values returned from the tasks
     * @param tasks The collection of tasks
     *
     * @return The result returned by one of the tasks
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException   if no task successfully completes
     */
    default <T> T invokeAny( Collection<? extends Callable<T>> tasks )
        throws InterruptedException, ExecutionException
    {
        return defaultExecutor().invokeAny( tasks );
    }

    /**
     * Executes the given tasks on the default Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do.
     * <p>
     * See {@link ExecutorService#invokeAny(java.util.Collection)}.
     *
     * @param <T>          The type of the values returned from the tasks
     * @param executorName The name of a configured Application Executor
     * @param tasks        The collection of tasks
     *
     * @return The result returned by one of the tasks
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException   if no task successfully completes
     */
    default <T> T invokeAny( String executorName, Collection<? extends Callable<T>> tasks )
        throws InterruptedException, ExecutionException
    {
        return executor( executorName ).invokeAny( tasks );
    }

    /**
     * Executes the given tasks on the default Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do before the given timeout elapses.
     * <p>
     * See {@link ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)}.
     *
     * @param <T>     The type of the values returned from the tasks
     * @param tasks   The collection of tasks
     * @param timeout The maximum time to wait
     * @param unit    The time unit of the timeout argument
     *
     * @return The result returned by one of the tasks
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException   if no task successfully completes
     * @throws TimeoutException     if the given timeout elapses before any task successfully completes
     */
    default <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return defaultExecutor().invokeAny( tasks, timeout, unit );
    }

    /**
     * Executes the given tasks on a configured Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do before the given timeout elapses.
     * <p>
     * See {@link ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)}.
     *
     * @param <T>          The type of the values returned from the tasks
     * @param executorName The name of a configured Application Executor
     * @param tasks        The collection of tasks
     * @param timeout      The maximum time to wait
     * @param unit         The time unit of the timeout argument
     *
     * @return The result returned by one of the tasks
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException   if no task successfully completes
     * @throws TimeoutException     if the given timeout elapses before any task successfully completes
     */
    default <T> T invokeAny( String executorName, Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return executor( executorName ).invokeAny( tasks, timeout, unit );
    }

    /**
     * Executes the given tasks in the default Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do.
     * <p>
     * See {@link ExecutorService#invokeAll(java.util.Collection)}.
     *
     * @param <T>   The type of the values returned from the tasks
     * @param tasks The collection of tasks
     *
     * @return a list of Futures representing the tasks, in the same sequential order as produced by the iterator for
     *         the given task list, each of which has completed
     *
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled
     */
    default <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks )
        throws InterruptedException
    {
        return defaultExecutor().invokeAll( tasks );
    }

    /**
     * Executes the given tasks in a configured Application Executor, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do.
     * <p>
     * See {@link ExecutorService#invokeAll(java.util.Collection)}.
     *
     * @param <T>          The type of the values returned from the tasks
     * @param executorName The name of a configured Application Executor
     * @param tasks        The collection of tasks
     *
     * @return a list of Futures representing the tasks, in the same sequential order as produced by the iterator for
     *         the given task list, each of which has completed
     *
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled
     */
    default <T> List<Future<T>> invokeAll( String executorName, Collection<? extends Callable<T>> tasks )
        throws InterruptedException
    {
        return executor( executorName ).invokeAll( tasks );
    }

    /**
     * Executes the given tasks in the default Application Executor, returning a list of Futures holding their status
     * and results when all complete or the timeout expires, whichever happens first.
     * <p>
     * See {@link ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)}.
     *
     * @param <T>     The type of the values returned from the tasks
     * @param tasks   The collection of tasks
     * @param timeout The maximum time to wait
     * @param unit    The time unit of the timeout argument
     *
     * @return a list of Futures representing the tasks, in the same sequential order as produced by the iterator for
     *         the given task list. If the operation did not time out, each task will have completed. If it did time
     *         out, some of these tasks will not have completed.
     *
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled
     */
    default <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
        throws InterruptedException
    {
        return defaultExecutor().invokeAll( tasks, timeout, unit );
    }

    /**
     * Executes the given tasks in a configured Application Executor, returning a list of Futures holding their status
     * and results when all complete or the timeout expires, whichever happens first.
     * <p>
     * See {@link ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)}.
     *
     * @param <T>          The type of the values returned from the tasks
     * @param executorName The name of a configured Application Executor
     * @param tasks        The collection of tasks
     * @param timeout      The maximum time to wait
     * @param unit         The time unit of the timeout argument
     *
     * @return a list of Futures representing the tasks, in the same sequential order as produced by the iterator for
     *         the given task list. If the operation did not time out, each task will have completed. If it did time
     *         out, some of these tasks will not have completed.
     *
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled
     */
    default <T> List<Future<T>> invokeAll(
        String executorName, Collection<? extends Callable<T>> tasks,
        long timeout, TimeUnit unit
    )
        throws InterruptedException
    {
        return executor( executorName ).invokeAll( tasks, timeout, unit );
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the default Application
     * Executor after it runs the given action.
     *
     * @param runnable The action to run before completing the returned CompletableFuture
     *
     * @return the new CompletableFuture
     */
    default CompletableFuture<Void> runAsync( Runnable runnable )
    {
        return CompletableFuture.runAsync( runnable, defaultExecutor() );
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in a configured Application
     * Executor after it runs the given action.
     *
     * @param executorName The name of a configured Application Executor
     * @param runnable     The action to run before completing the returned CompletableFuture
     *
     * @return the new CompletableFuture
     */
    default CompletableFuture<Void> runAsync( String executorName, Runnable runnable )
    {
        return CompletableFuture.runAsync( runnable, executor( executorName ) );
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the default Application
     * Executor with the value obtained by calling the given Supplier.
     *
     * @param <U>      The function's return type
     * @param supplier a function returning the value to be used to complete the returned CompletableFuture
     *
     * @return the new CompletableFuture
     */
    default <U> CompletableFuture<U> supplyAsync( Supplier<U> supplier )
    {
        return CompletableFuture.supplyAsync( supplier, defaultExecutor() );
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in a configured Application
     * Executor with the value obtained by calling the given Supplier.
     *
     * @param <U>          The function's return type
     * @param executorName The name of a configured Application Executor
     * @param supplier     a function returning the value to be used to complete the returned CompletableFuture
     *
     * @return the new CompletableFuture
     */
    default <U> CompletableFuture<U> supplyAsync( String executorName, Supplier<U> supplier )
    {
        return CompletableFuture.supplyAsync( supplier, executor( executorName ) );
    }
}
