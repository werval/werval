package org.qiweb.api.http;

/**
 * A request.
 */
public interface Request
{

    RequestHeader header();

    RequestBody body();
}
