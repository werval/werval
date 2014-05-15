/*
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
import org.qiweb.api.http.HttpBuilders;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.ResponseHeader;
import org.qiweb.api.http.Session;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.outcomes.Outcomes;
import org.qiweb.api.routes.ReverseRoutes;

/**
 * Current Context.
 *
 * Static utility methods to get a hand on the thread local Context.
 */
public final class CurrentContext
{
    /* package */ static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * @return Current Request Context
     *
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
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static Application application()
    {
        return get().application();
    }

    /**
     * Lookup a Plugin's API.
     *
     * Don't hold references to the Plugins API instances in order to make your code {@link org.qiweb.api.Mode#DEV}
     * friendly.
     *
     * @param <T>           Parameterized Plugin API type
     * @param pluginApiType Plugin type
     *
     * @return The first Plugin API found that match given type (Type equals first, then assignable).
     *
     * @throws IllegalArgumentException if no {@literal Plugin} is found for the given API type
     * @throws IllegalStateException    if the {@literal Application} is not active
     */
    public static <T> T plugin( Class<T> pluginApiType )
    {
        return application().plugin( pluginApiType );
    }

    /**
     * Lookup possibly several Plugin's API.
     *
     * Don't hold references to the Plugins API instances in order to make your code {@link org.qiweb.api.Mode#DEV}
     * friendly.
     *
     * @param <T>           Parameterized Plugin API type
     * @param pluginApiType Plugin type
     *
     * @return All Plugin APIs found that match the he given type (Type equals first, then assignables), or none if no
     *         Plugin is found for the given API type.
     *
     * @throws IllegalStateException if the {@literal Application} is not active
     */
    public static <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        return application().plugins( pluginApiType );
    }

    /**
     * @return Current ReverseRoutes
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static ReverseRoutes reverseRoutes()
    {
        return get().application().reverseRoutes();
    }

    /**
     * @return Current Request Session
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static Session session()
    {
        return get().session();
    }

    /**
     * @return Current Request
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static Request request()
    {
        return get().request();
    }

    /**
     * @return Current Response
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static ResponseHeader response()
    {
        return get().response();
    }

    /**
     * @return Current Outcome builder
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static Outcomes outcomes()
    {
        return get().outcomes();
    }

    /**
     * @return Current HTTP Objects Builder
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static HttpBuilders builders()
    {
        return get().application().httpBuilders();
    }

    /**
     * @return Application MimeTypes
     *
     * @throws QiWebException if no Context in current Thread
     */
    public static MimeTypes mimeTypes()
    {
        return get().application().mimeTypes();
    }

    private CurrentContext()
    {
    }
}
