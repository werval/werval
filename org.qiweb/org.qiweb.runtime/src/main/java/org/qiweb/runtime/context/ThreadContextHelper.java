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
package org.qiweb.runtime.context;

import java.lang.reflect.Field;
import org.qiweb.api.context.Context;
import org.qiweb.api.context.CurrentContext;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.slf4j.MDC;

import static org.qiweb.api.http.Headers.Names.X_QIWEB_REQUEST_ID;

/**
 * Current Thread Context Helper.
 */
public final class ThreadContextHelper
{

    @SuppressWarnings( "unchecked" )
    private static ThreadLocal<Context> getCurrentContextThreadLocal()
    {
        try
        {
            Field field = CurrentContext.class.getDeclaredField( "CONTEXT_THREAD_LOCAL" );
            if( !field.isAccessible() )
            {
                field.setAccessible( true );
            }
            return (ThreadLocal<Context>) field.get( null );
        }
        catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
        {
            throw new QiWebRuntimeException( "QiWeb API mismatch, unable to get Current Context Thread Local, "
                                             + "something is broken! Please report. " + ex.getMessage(), ex );
        }
    }

    /**
     * Run a {@literal Runnable} with a Context.
     * <p>Use a {@link ThreadContextHelper} instance, see its methods documentation.</p>
     * @param context Context
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
    private ClassLoader previousLoader = null;

    /**
     * Set {@literal Context} on current {@literal Thread}.
     *
     * <p>In order:</p>
     * <ul>
     *     <li>Keep previous thread context {@link ClassLoader}.</li>
     *     <li>Set thread {@link ClassLoader}.</li>
     *     <li>Set thread Context {@literal ThreadLocal}.</li>
     *     <li>Put current Request ID in SLF4J MDC at the {@link #X_QIWEB_REQUEST_ID} key.</li>
     * </ul>
     *
     * @param context Context
     */
    public void setOnCurrentThread( Context context )
    {
        previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.application().classLoader() );
        getCurrentContextThreadLocal().set( context );
        MDC.put( X_QIWEB_REQUEST_ID, context.request().identity() );
    }

    /**
     * Remove {@literal Context} from current {@literal Thread}.
     *
     * <p>In order:</p>
     * <ul>
     *     <li>Remove current Request ID from SLF4J MDC.</li>
     *     <li>Set thread {@link ClassLoader} to previous one.</li>
     *     <li>Remove thread Context {@literal ThreadLocal}.</li>
     * </ul>
     */
    public void clearCurrentThread()
    {
        MDC.remove( X_QIWEB_REQUEST_ID );
        Thread.currentThread().setContextClassLoader( previousLoader );
        previousLoader = null;
        getCurrentContextThreadLocal().remove();
    }

}
