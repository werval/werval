package org.qiweb.runtime.http;

import org.qiweb.api.routes.Routes;

public interface HttpApplication
{

    ClassLoader classLoader();

    Routes routes();
}
