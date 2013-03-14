package org.qiweb.http;

import io.netty.channel.EventExecutorGroup;
import org.qiweb.http.routes.Routes;

public interface HttpApplication
{

    ClassLoader classLoader();

    Routes routes();

    EventExecutorGroup httpExecutors();

    <T> T controllerInstance( Class<T> controllerType );
}
