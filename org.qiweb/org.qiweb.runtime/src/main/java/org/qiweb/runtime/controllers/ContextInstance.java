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
package org.qiweb.runtime.controllers;

import org.qiweb.api.Application;
import org.qiweb.api.MetaData;
import org.qiweb.api.controllers.Context;
import org.qiweb.api.controllers.Outcomes;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.routes.Route;

/**
 * A HTTP Interaction Context instance.
 */
public final class ContextInstance
    implements Context
{

    private final Application application;
    private final Session session;
    private final Route route;
    private final Request request;
    private final Response response;
    private final Outcomes outcomes;
    private final MetaData metaData;

    public ContextInstance( Application application, Session session, Route route, Request request, Response response )
    {
        this.application = application;
        this.session = session;
        this.route = route;
        this.request = request;
        this.response = response;
        this.outcomes = new OutcomesInstance( application.config(), response.headers(), response.cookies() );
        this.metaData = new MetaData();
    }

    @Override
    public Application application()
    {
        return application;
    }

    @Override
    public Session session()
    {
        return session;
    }

    @Override
    public Route route()
    {
        return route;
    }

    @Override
    public Request request()
    {
        return request;
    }

    @Override
    public Response response()
    {
        return response;
    }

    @Override
    public Outcomes outcomes()
    {
        return outcomes;
    }

    @Override
    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public MetaData metaData()
    {
        return metaData;
    }
}
