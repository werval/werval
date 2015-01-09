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
package io.werval.api.context;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import io.werval.api.Application;
import io.werval.api.ApplicationExecutors;
import io.werval.api.Crypto;
import io.werval.api.MetaData;
import io.werval.api.cache.Cache;
import io.werval.api.exceptions.WervalException;
import io.werval.api.http.HttpBuilders;
import io.werval.api.http.Request;
import io.werval.api.http.ResponseHeader;
import io.werval.api.http.Session;
import io.werval.api.mime.MimeTypes;
import io.werval.api.outcomes.Outcomes;
import io.werval.api.routes.ReverseRoutes;
import io.werval.api.templates.Templates;

/**
 * Current Context.
 * <p>
 * Static utility methods to get a hand on the thread local Context.
 *
 * @navcomposed 0 - 1 Context
 */
public final class CurrentContext
{
    /* package */ static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * @return Current Request Context
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Context get()
    {
        Context context = CONTEXT_THREAD_LOCAL.get();
        if( context == null )
        {
            throw new WervalException( "No Context in this Thread (" + Thread.currentThread().getName() + ")" );
        }
        return context;
    }

    /**
     * @return Optional Current Request Context
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Optional<Context> optional()
    {
        return Optional.ofNullable( CONTEXT_THREAD_LOCAL.get() );
    }

    /**
     * @return Current Context MetaData
     *
     * @throws WervalException if no Context in current Thread
     */
    public static MetaData metaData()
    {
        return get().metaData();
    }

    /**
     * @return Current Application
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Application application()
    {
        return get().application();
    }

    /**
     * @return Current Crypto
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Crypto crypto()
    {
        return get().application().crypto();
    }

    /**
     * @return Current default Application ExecutorService
     *
     * @throws WervalException if no Context in current Thread
     */
    public static ExecutorService executor()
    {
        return get().application().executor();
    }

    /**
     * @return Current Application Executors
     *
     * @throws WervalException if no Context in current Thread
     */
    public static ApplicationExecutors executors()
    {
        return get().application().executors();
    }

    /**
     * Lookup a Plugin's API.
     * <p>
     * Don't hold references to the Plugins API instances in order to make your code {@link io.werval.api.Mode#DEV}
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
     * <p>
     * Don't hold references to the Plugins API instances in order to make your code {@link io.werval.api.Mode#DEV}
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
     * @throws WervalException if no Context in current Thread
     */
    public static ReverseRoutes reverseRoutes()
    {
        return get().application().reverseRoutes();
    }

    /**
     * @return Current Request Session
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Session session()
    {
        return get().session();
    }

    /**
     * @return Current Request
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Request request()
    {
        return get().request();
    }

    /**
     * @return Current Response
     *
     * @throws WervalException if no Context in current Thread
     */
    public static ResponseHeader response()
    {
        return get().response();
    }

    /**
     * @return Current Outcome builder
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Outcomes outcomes()
    {
        return get().outcomes();
    }

    /**
     * @return Current HTTP Objects Builder
     *
     * @throws WervalException if no Context in current Thread
     */
    public static HttpBuilders builders()
    {
        return get().application().httpBuilders();
    }

    /**
     * @return Application MimeTypes
     *
     * @throws WervalException if no Context in current Thread
     */
    public static MimeTypes mimeTypes()
    {
        return get().application().mimeTypes();
    }

    /**
     * @return Application Cache
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Cache cache()
    {
        return get().application().cache();
    }

    /**
     * @return Application Templates
     *
     * @throws WervalException if no Context in current Thread
     */
    public static Templates templates()
    {
        return get().application().templates();
    }

    private CurrentContext()
    {
    }
}
