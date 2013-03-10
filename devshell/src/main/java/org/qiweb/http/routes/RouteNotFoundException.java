package org.qiweb.http.routes;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Thrown when no satisfying Route found.
 */
public class RouteNotFoundException
    extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    private final HttpMethod method;
    private final String uri;

    public RouteNotFoundException( HttpMethod method, String uri )
    {
        super( "No route for " + method.name() + " " + uri );
        this.method = method;
        this.uri = uri;
    }

    public HttpMethod method()
    {
        return method;
    }

    public String uri()
    {
        return uri;
    }
}
