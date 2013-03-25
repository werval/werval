package org.qiweb.runtime.controllers;

import java.lang.reflect.Field;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Controller;
import org.qiweb.runtime.QiWebRuntimeException;

public final class ControllerContext
{

    private ClassLoader previousLoader = null;

    public void setOnCurrentThread( ClassLoader loader, Context context )
    {
        previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( loader );
        getControllerContextThreadLocal().set( context );
    }

    public void clearCurrentThread()
    {
        Thread.currentThread().setContextClassLoader( previousLoader );
        previousLoader = null;
        getControllerContextThreadLocal().remove();
    }

    @SuppressWarnings( "unchecked" )
    private static ThreadLocal<Context> getControllerContextThreadLocal()
    {
        try
        {
            Field field = Controller.class.getDeclaredField( "CONTEXT_THREAD_LOCAL" );
            if( !field.isAccessible() )
            {
                field.setAccessible( true );
            }
            return (ThreadLocal<Context>) field.get( null );
        }
        catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
        {
            throw new QiWebRuntimeException( ex.getMessage(), ex );
        }
    }
}
