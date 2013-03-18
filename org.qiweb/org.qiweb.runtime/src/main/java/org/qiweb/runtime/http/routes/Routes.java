package org.qiweb.runtime.http.routes;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Routes.
 */
public interface Routes
    extends Iterable<Route>
{

    /**
     * Get a Route satisfiedBy a HttpRequest.
     * <p/>
     * @param httpRequest any HttpRequest
     * @return a Route satisfiedBy the HttpRequest
     * @throws RouteNotFoundException when no Route is satisfiedBy the HttpRequest
     */
    Route route( HttpRequest httpRequest )
        throws RouteNotFoundException;
}
