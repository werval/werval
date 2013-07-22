package org.qiweb.api.routes;

/**
 * Reverse Route.
 */
public interface ReverseRoute
{

    /**
     * @return HTTP Method
     */
    String method();

    /**
     * @return HTTP URI
     */
    String uri();

    /**
     * @return Absolute HTTP URL
     */
    String httpUrl();

    /**
     * @param secure is the URL secure?
     * @return Absolute HTTP(s) URL
     */
    String httpUrl( boolean secure );

    /**
     * @return Absolute WebSocket URL
     */
    String webSocketUrl();

    /**
     * @param secure is the URL secure?
     * @return Absolute WebSocket(SSL) URL
     */
    String webSocketUrl( boolean secure );
}
