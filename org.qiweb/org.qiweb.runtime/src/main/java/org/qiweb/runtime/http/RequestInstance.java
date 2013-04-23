package org.qiweb.runtime.http;

import java.util.Collections;
import java.util.Map;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;

public class RequestInstance
    implements Request
{

    private final RequestHeader header;
    private final Map<String, Object> pathParams;
    private final RequestBody body;

    public RequestInstance( RequestHeader header, Map<String, Object> pathParams, RequestBody body )
    {
        this.header = header;
        this.pathParams = pathParams;
        this.body = body;
    }

    @Override
    public String identity()
    {
        return header.identity();
    }

    @Override
    public String version()
    {
        return header.version();
    }

    @Override
    public String method()
    {
        return header.method();
    }

    @Override
    public String uri()
    {
        return header.uri();
    }

    @Override
    public String path()
    {
        return header.path();
    }

    @Override
    public QueryString queryString()
    {
        return header.queryString();
    }

    @Override
    public String remoteAddress()
    {
        return header.remoteAddress();
    }

    @Override
    public String host()
    {
        return header.host();
    }

    @Override
    public int port()
    {
        return header.port();
    }

    @Override
    public String domain()
    {
        return header.domain();
    }

    @Override
    public String contentType()
    {
        return header.contentType();
    }

    @Override
    public String charset()
    {
        return header.charset();
    }

    @Override
    public Headers headers()
    {
        return header.headers();
    }

    @Override
    public Cookies cookies()
    {
        return header.cookies();
    }

    @Override
    public Map<String, Object> pathParams()
    {
        return Collections.unmodifiableMap( pathParams );
    }

    @Override
    public RequestBody body()
    {
        return body;
    }
}
