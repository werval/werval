package org.qiweb.api.controllers;

import org.qiweb.api.Application;
import org.qiweb.api.MetaData;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Response;
import org.qiweb.api.http.Session;
import org.qiweb.api.routes.Route;

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
    // ThreadFactory threadFactory();
}
