package org.qiweb.api.controllers;

import org.qiweb.api.Application;
import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;

/**
 * HTTP Interaction Context.
 * <p>Accessible using {@link Controller#context()} and other helpers in {@link Controller}.</p>
 */
public interface Context
{

    Application application();

    Session session();

    Request request();

    Response response();

    Flash flash();
}
