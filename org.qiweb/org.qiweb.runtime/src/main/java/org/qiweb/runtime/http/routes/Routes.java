package org.qiweb.runtime.http.routes;

import org.qiweb.api.http.HttpRequestHeader;

/**
 * Routes.
 */
public interface Routes
    extends Iterable<Route>
{

    /**
     * Get a Route satisfiedBy a HttpRequest.
     * <p/>
     * @param requestHeader any HttpRequestHeader
     * @return a Route satisfiedBy the HttpRequest
     * @throws RouteNotFoundException when no Route is satisfiedBy the HttpRequest
     */
    Route route( HttpRequestHeader requestHeader )
        throws RouteNotFoundException;
}
