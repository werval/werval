package org.qiweb.api;

import org.qiweb.api.routes.Routes;

public interface QiWebApplication
{

    ClassLoader classLoader();

    Routes routes();
}
