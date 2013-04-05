package org.qiweb.runtime.http;

import org.qiweb.api.http.MutableCookies;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.http.Response;

public class ResponseInstance
    implements Response
{

    private final MutableHeaders headers;
    private final MutableCookies cookies;

    public ResponseInstance()
    {
        this.headers = new HeadersInstance();
        this.cookies = new CookiesInstance();
    }

    @Override
    public MutableHeaders headers()
    {
        return headers;
    }

    @Override
    public MutableCookies cookies()
    {
        return cookies;
    }
}
