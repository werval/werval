package org.qiweb.api.controllers;

import org.qiweb.api.Application;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;

/**
 * HTTP Interaction Context.
 * <p>Accessible using {@link Controller#context()} and other helpers in {@link Controller}.</p>
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
}
