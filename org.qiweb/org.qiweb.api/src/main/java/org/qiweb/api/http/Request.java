package org.qiweb.api.http;

/**
 * A Request is a RequestHeader plus a RequestBody.
 */
public interface Request
    extends RequestHeader
{

    RequestBody body();
}
