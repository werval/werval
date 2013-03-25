package org.qiweb.runtime.http;

import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;

public class RequestInstance
    implements Request
{

    private final RequestHeader header;
    private final RequestBody body;

    public RequestInstance( RequestHeader header, RequestBody body )
    {
        this.header = header;
        this.body = body;
    }

    @Override
    public RequestHeader header()
    {
        return header;
    }

    @Override
    public RequestBody body()
    {
        return body;
    }
}
