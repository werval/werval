package org.qiweb.runtime.controllers;

import org.qiweb.api.Application;
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

    public ContextInstance( Application application, Session session, Route route, Request request, Response response )
    {
        this.application = application;
        this.session = session;
        this.route = route;
        this.request = request;
        this.response = response;
        this.outcomes = new OutcomesInstance( application.config(), response.headers(), response.cookies() );
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
}
