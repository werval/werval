package org.qiweb.api.controllers;

import org.qiweb.api.QiWebException;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
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
     * @return Current Request Context
     * @throws QiWebException if no Context in current Thread
     */
    public static Context context()
    {
        Context context = CONTEXT_THREAD_LOCAL.get();
        if( context == null )
        {
            throw new QiWebException( "No Context in this Thread (" + Thread.currentThread().getName() + ")" );
        }
        return context;
    }

    /**
     * @return Current Request
     * @throws QiWebException if no Context in current Thread
     */
    public static Request request()
    {
        return context().request();
    }

    /**
     * @return Current Response or null if no Context
     * @throws QiWebException if no Context in current Thread
     */
    public static Response response()
    {
        return context().response();
    }

    /**
     * @return Current Request Flash or null if no Context
     * @throws QiWebException if no Context in current Thread
     */
    public static Flash flash()
    {
        return context().flash();
    }

    /**
     * @return Current Request Session or null if no Context
     * @throws QiWebException if no Context in current Thread
     */
    public static Session session()
    {
        return context().session();
    }

    public Controller()
    {
        // NOOP
    }
}
