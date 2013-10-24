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
import org.qiweb.api.MetaData;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.outcomes.Outcomes;
import org.qiweb.api.routes.Route;

/**
 * HTTP Interaction Context.
 * <p>Accessible using {@link CurrentContext#get()} and other helpers in {@link CurrentContext}.</p>
 *
 * @navassoc - - - org.qiweb.api.Application
 * @navassoc - - - org.qiweb.api.http.Session
 * @navassoc - - - org.qiweb.api.http.Request
 * @navassoc - - - org.qiweb.api.routes.Route
 * @navassoc - - - org.qiweb.api.MetaData
 * @depend - - - org.qiweb.api.http.Response
 * @depend - - - Outcomes
 */
public interface Context
{

    /**
     * @return Current Application
     */
    Application application();

    /**
     * @return Current Session
     */
    Session session();

    /**
     * @return Current Route
     */
    Route route();

    /**
     * @return Current Request
     */
    Request request();

    /**
     * @return Current Response
     */
    Response response();

    /**
     * @return Current Outcome builder
     */
    Outcomes outcomes();

    /**
     * @return Current Context MetaData
     */
    MetaData metaData();
    //
    // TODO State on Context ThreadFactory in API
    // ThreadFactory that carry the current context to another thread
    // Maybe somewhere else ...
    // Could be useful for small needs or bigger integrations
    // How to share a ThreadLocal to the new thread?
    // All context objects should then be made thread safe!
    // Could be someting like: ThreadFactory threadFactory()

}
