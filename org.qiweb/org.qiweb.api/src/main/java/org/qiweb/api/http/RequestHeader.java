package org.qiweb.api.http;

/**
 * Method, Path, QueryString, Headers and Cookies.
 * 
 * <p>No Entity.</p>
 * <p>Helper methods.</p>
 */
public interface RequestHeader
{

    /**
     * @return The HTTP Request ID created by the framework
     */
    String identity();

    /**
     * @return The HTTP Request Protocol Version
     */
    String version();

    /**
     * @return The HTTP Request Method
     */
    String method();

    /**
     * @return The HTTP Request URI
     */
    String uri();

    /**
     * @return The HTTP Request Path
     */
    String path();

    /**
     * @return The HTTP Request Query String
     */
    QueryString queryString();

    /**
     * @return The HTTP Request Headers
     */
    Headers headers();

    /**
     * @return The HTTP Request Cookies
     */
    Cookies cookies();

    /**
     * The HTTP Client Address.
     * <p>Computed from the X-Forwarded-For header value if this header is present and the local address is loopback.</p>
     * @return The HTTP Client Address or an empty String
     */
    String remoteAddress();

    /**
     * The HTTP Host with or without the port, or an empty String.
     * <p>HTTP Host header, mandatory since HTTP 1.1.</p>
     * @return The HTTP Host with or without the port, or an empty String
     */
    String host();

    /**
     * The HTTP Port.
     * <p>Computed from the request URI and standard defaults 80/443.</p>
     * @return The HTTP Port
     */
    int port();

    /**
     * The HTTP Domain.
     * <p>Computed from the request URI.</p>
     * @return The HTTP Domain
     */
    String domain();

    /**
     * The HTTP Request content type, or and empty String.
     * <p>Computed from the Content-Type header, charset removed.</p>
     * @return The HTTP Request content type, or and empty String
     */
    String contentType();

    /**
     * The HTTP Request Charset or an empty String.
     * <p>Computed from the Content-Type header.</p>
     * @return The HTTP Request Charset or an empty String
     */
    String charset();
}
