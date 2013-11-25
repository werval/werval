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
package org.qiweb.api.context;

import org.qiweb.api.Application;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.outcomes.Outcomes;
import org.qiweb.api.routes.ReverseRoutes;

/**
 * Current Context.
 * <p>Static utility methods to get a hand on the thread local Context.</p>
 */
public class CurrentContext
{

    /* package */ static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * @return Current Request Context
     * @throws QiWebException if no Context in current Thread
     */
    public static Context get()
    {
        Context context = CONTEXT_THREAD_LOCAL.get();
        if( context == null )
        {
            throw new QiWebException( "No Context in this Thread (" + Thread.currentThread().getName() + ")" );
        }
        return context;
    }

    /**
     * @return Current Application
     * @throws QiWebException if no Context in current Thread
     */
    public static Application application()
    {
        return get().application();
    }

    /**
     * @return Current ReverseRoutes
     * @throws QiWebException if no Context in current Thread
     */
    public static ReverseRoutes reverseRoutes()
    {
        return get().application().reverseRoutes();
    }

    /**
     * @return Current Request Session
     * @throws QiWebException if no Context in current Thread
     */
    public static Session session()
    {
        return get().session();
    }

    /**
     * @return Current Request
     * @throws QiWebException if no Context in current Thread
     */
    public static Request request()
    {
        return get().request();
    }

    /**
     * @return Current Response
     * @throws QiWebException if no Context in current Thread
     */
    public static Response response()
    {
        return get().response();
    }

    /**
     * @return Current Outcome builder
     * @throws QiWebException if no Context in current Thread
     */
    public static Outcomes outcomes()
    {
        return get().outcomes();
    }

    private CurrentContext()
    {
    }

}
