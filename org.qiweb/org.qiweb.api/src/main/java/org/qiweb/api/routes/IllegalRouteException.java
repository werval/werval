package org.qiweb.api.routes;

/**
 * Thrown when trying to build a Route instance with an illegal route string.
 */
public class IllegalRouteException
    extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    private String routeString;

    public IllegalRouteException( String routeString, String message )
    {
        super( routeString + "\n" + message );
        this.routeString = routeString;
    }

    public IllegalRouteException( String routeString, String message, Throwable cause )
    {
        super( routeString + "\n" + message, cause );
    }

    public String routeString()
    {
        return routeString;
    }
}
