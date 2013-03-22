package org.qiweb.runtime.routes;

import org.qiweb.api.routes.Routes;

public interface RoutesProvider
{

    Routes routes();

    Routes routes( ClassLoader loader );
}
