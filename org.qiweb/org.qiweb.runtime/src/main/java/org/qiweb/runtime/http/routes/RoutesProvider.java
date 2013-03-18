package org.qiweb.runtime.http.routes;

public interface RoutesProvider
{

    Routes routes();

    Routes routes( ClassLoader loader );
}
