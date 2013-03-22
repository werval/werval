package org.qiweb.runtime.http.routes;

/**
 * Thrown when no satisfying Route found.
 */
public class RouteNotFoundException
    extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    private final String method;
    private final String uri;

    public RouteNotFoundException( String method, String uri )
    {
        super( "No route for " + method + " " + uri );
        this.method = method;
        this.uri = uri;
    }

    public String method()
    {
        return method;
    }

    public String uri()
    {
        return uri;
    }
}
