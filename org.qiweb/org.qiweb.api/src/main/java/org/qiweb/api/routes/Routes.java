package org.qiweb.api.routes;

import org.qiweb.api.http.RequestHeader;

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
    Route route( RequestHeader requestHeader );
}
