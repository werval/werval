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
                                             + "something is broken! " + ex.getMessage(), ex );
        }
    }

    private ClassLoader previousLoader = null;

    public void setOnCurrentThread( ClassLoader loader, Context context )
    {
        previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( loader );
        getCurrentContextThreadLocal().set( context );
    }

    public void clearCurrentThread()
    {
        Thread.currentThread().setContextClassLoader( previousLoader );
        previousLoader = null;
        getCurrentContextThreadLocal().remove();
    }

}