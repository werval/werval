/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.api.context;

import java.util.concurrent.Callable;
import org.slf4j.MDC;

import static io.werval.api.http.Headers.Names.X_WERVAL_CLIENT_IP;
import static io.werval.api.http.Headers.Names.X_WERVAL_REQUEST_ID;

/**
 * Current Thread Context Helper.
 */
public final class ThreadContextHelper
{
    /**
     * Run a {@literal Runnable} with a Context.
     * <p>
     * Use a {@link ThreadContextHelper} instance, see its methods documentation.
     *
     * @param context  Context
     * @param runnable Runnable
     */
    public static void withContext( Context context, Runnable runnable )
    {
        ThreadContextHelper helper = new ThreadContextHelper();
        try
        {
            helper.setOnCurrentThread( context );
            runnable.run();
        }
        finally
        {
            helper.clearCurrentThread();
        }
    }

    /**
     * Run a {@literal Callable} with a Context.
     * <p>
     * Use a {@link ThreadContextHelper} instance, see its methods documentation.
     *
     * @param <T>      Parameterized type of the Callable result
     * @param context  Context
     * @param callable Callable
     *
     * @return Callable result
     *
     * @throws java.lang.Exception if the Callable was unable to compute a result
     */
    public static <T> T withContext( Context context, Callable<T> callable )
        throws Exception
    {
        ThreadContextHelper helper = new ThreadContextHelper();
        try
        {
            helper.setOnCurrentThread( context );
            return callable.call();
        }
        finally
        {
            helper.clearCurrentThread();
        }
    }

    private ClassLoader previousLoader = null;
    private boolean logRequestId = false;
    private boolean logClientIp = false;

    /**
     * Set {@literal Context} on current {@literal Thread}.
     * <p>
     * In order:
     * <ul>
     * <li>Keep previous thread context {@link ClassLoader}.</li>
     * <li>Set thread {@link ClassLoader}.</li>
     * <li>Set thread Context {@literal ThreadLocal}.</li>
     * <li>
     * Put current Request ID in SLF4J MDC at the {@link io.werval.api.http.Headers.Names#X_WERVAL_REQUEST_ID} key.
     * </li>
     * <li>
     * Put current Request Client IP in SLF4J MDC at the
     * {@link io.werval.api.http.Headers.Names#X_WERVAL_CLIENT_IP} key if enabled in the configuration.
     * </li>
     * </ul>
     *
     * @param context Context
     */
    public void setOnCurrentThread( Context context )
    {
        previousLoader = Thread.currentThread().getContextClassLoader();
        if( context != null )
        {
            Thread.currentThread().setContextClassLoader( context.application().classLoader() );
            logRequestId = context.application().config().bool( "werval.http.log.context.request_id" );
            if( logRequestId )
            {
                MDC.put( X_WERVAL_REQUEST_ID, context.request().identity() );
            }
            logClientIp = context.application().config().bool( "werval.http.log.context.client_ip" );
            if( logClientIp )
            {
                MDC.put( X_WERVAL_CLIENT_IP, context.request().remoteAddress() );
            }
        }
        CurrentContext.CONTEXT_THREAD_LOCAL.set( context );
    }

    /**
     * Remove {@literal Context} from current {@literal Thread}.
     * <p>
     * In order:
     * <ul>
     * <li>Remove current Request ID from SLF4J MDC.</li>
     * <li>Set thread {@link ClassLoader} to previous one.</li>
     * <li>Remove thread Context {@literal ThreadLocal}.</li>
     * </ul>
     */
    public void clearCurrentThread()
    {
        if( logRequestId )
        {
            MDC.remove( X_WERVAL_REQUEST_ID );
        }
        if( logClientIp )
        {
            MDC.remove( X_WERVAL_CLIENT_IP );
        }
        Thread.currentThread().setContextClassLoader( previousLoader );
        previousLoader = null;
        CurrentContext.CONTEXT_THREAD_LOCAL.remove();
    }
}
