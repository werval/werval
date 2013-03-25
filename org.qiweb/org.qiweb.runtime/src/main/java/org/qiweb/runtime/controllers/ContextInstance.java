package org.qiweb.runtime.controllers;

import org.qiweb.api.controllers.Context;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Session;

/**
 * A Context instance.
 */
public class ContextInstance
    implements Context
{

    private final Request request;
    private final Flash flash;
    private final Session session;

    public ContextInstance( Request request, Flash flash, Session session )
    {
        this.request = request;
        this.flash = flash;
        this.session = session;
    }

    @Override
    public Request request()
    {
        return request;
    }

    @Override
    public Flash flash()
    {
        return flash;
    }

    @Override
    public Session session()
    {
        return session;
    }
}
