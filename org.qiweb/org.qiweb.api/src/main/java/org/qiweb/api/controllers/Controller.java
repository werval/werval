package org.qiweb.api.controllers;

import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Session;

/**
 * Controller.
 * <p>
 *     This class only provide static helpers backed by a ThreadLocal<Context> so you can extend it or not,
 *     as you like.
 * </p>
 */
public class Controller
{

    private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * @return Current Request Context or null if no Context
     */
    public static Context context()
    {
        return CONTEXT_THREAD_LOCAL.get();
    }

    /**
     * @return Current Request or null if no Context
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
     * @return Current Request Headers or null if no Context
     */
    public static Headers headers()
    {
        Context context = context();
        if( context == null )
        {
            return null;
        }
        return context.request().header().headers();
    }

    /**
     * @return Current Request Cookies or null if no Context
     */
    public static Cookies cookies()
    {
        Context context = context();
        if( context == null )
        {
            return null;
        }
        return context.request().header().cookies();
    }

    /**
     * @return Current Request Flash or null if no Context
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
     * @return Current Request Session or null if no Context
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
