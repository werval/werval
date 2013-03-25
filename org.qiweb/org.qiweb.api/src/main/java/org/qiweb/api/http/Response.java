package org.qiweb.api.http;

public interface Response
{

    MutableHeaders headers();

    MutableCookies cookies();
}
