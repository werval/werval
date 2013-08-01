package org.qiweb.api.http;

import java.util.Map;

/**
 * A Request is a RequestHeader plus a RequestBody.
 */
public interface Request
    extends RequestHeader
{

    Map<String, Object> parameters();

    RequestBody body();
}
