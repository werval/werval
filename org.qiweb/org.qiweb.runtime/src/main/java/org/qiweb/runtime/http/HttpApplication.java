package org.qiweb.runtime.http;

import org.qiweb.runtime.http.routes.Routes;

public interface HttpApplication
{

    ClassLoader classLoader();

    Routes routes();
}
