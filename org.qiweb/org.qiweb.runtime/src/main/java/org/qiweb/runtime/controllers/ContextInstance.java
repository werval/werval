package org.qiweb.runtime.controllers;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;

/**
 * A HTTP Interaction Context instance.
 */
public class ContextInstance
    implements Context
{

    private final Session session;
    private final Request request;
    private final Response response;
    private final Flash flash;

    public ContextInstance( Session session, Request request, Response response, Flash flash )
    {
        this.session = session;
        this.request = request;
        this.response = response;
        this.flash = flash;
    }

    @Override
    public Session session()
    {
        return session;
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
    public Flash flash()
    {
        return flash;
    }
}
