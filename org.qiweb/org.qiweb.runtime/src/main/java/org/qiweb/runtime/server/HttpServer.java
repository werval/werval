package org.qiweb.runtime.server;

import org.qiweb.api.exceptions.QiWebException;

/**
 * QiWeb Http Server.
 */
public interface HttpServer
{

    /**
     * Activate the QiWeb Http Server.
     */
    void activate()
        throws QiWebException;

    /**
     * Passivate the QiWeb Http Server.
     */
    void passivate()
        throws QiWebException;
}
