package org.qiweb.runtime.routes;

import org.qiweb.api.Config;
import org.qiweb.api.routes.Routes;

public interface RoutesProvider
{

    Routes routes( Config config, ClassLoader loader );
}
