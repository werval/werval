package org.qiweb.runtime.routes;

import org.qiweb.api.Application;
import org.qiweb.api.routes.Routes;

public interface RoutesProvider
{

    Routes routes( Application application );
}
