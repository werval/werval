/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.runtime.context;

import io.werval.api.Application;
import io.werval.api.MetaData;
import io.werval.api.context.Context;
import io.werval.api.http.Request;
import io.werval.api.http.ResponseHeader;
import io.werval.api.http.Session;
import io.werval.api.outcomes.Outcomes;
import io.werval.api.routes.Route;
import io.werval.runtime.http.ResponseHeaderInstance;
import io.werval.runtime.outcomes.OutcomesInstance;
import java.util.concurrent.ExecutorService;

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
    private final ResponseHeader response;
    private final Outcomes outcomes;
    private final MetaData metaData;
    private final ExecutorService executor;

    public ContextInstance(
        Application application, Session session, Route route, Request request,
        ResponseHeaderInstance responseHeader, ExecutorService executor
    )
    {
        this.application = application;
        this.session = session;
        this.route = route;
        this.request = request;
        this.response = responseHeader;
        this.outcomes = new OutcomesInstance( application.config(), application().mimeTypes(), responseHeader );
        this.metaData = new MetaData();
        this.executor = executor;
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
    public ResponseHeader response()
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

    @Override
    public ExecutorService executor()
    {
        return executor;
    }
}
