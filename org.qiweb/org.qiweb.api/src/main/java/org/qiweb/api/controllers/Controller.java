package org.qiweb.api.controllers;

import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Session;

/**
 * Controller.
 * <p>This class only provide static helpers so you can extend it or not, as you like.</p>
 */
public class Controller
{

    private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * @return Current Thread Context or null
     */
    public static Context context()
    {
        return CONTEXT_THREAD_LOCAL.get();
    }

    /**
     * @return Current Thread Request or null
     */
    public static Request request()
    {
        Context context = context();
        if( context == null )
        {
            return null;
        }
        return context.request();
    }

    /**
     * @return Current Thread Flash or null
     */
    public static Flash flash()
    {
        Context context = context();
        if( context == null )
        {
            return null;
        }
        return context.flash();
    }

    /**
     * @return Current Thread Session or null
     */
    public static Session session()
    {
        Context context = context();
        if( context == null )
        {
            return null;
        }
        return context.session();
    }
}
