package org.qiweb.runtime.http;

import io.netty.channel.EventExecutorGroup;
import org.qiweb.runtime.http.routes.Routes;

public interface HttpApplication
{

    ClassLoader classLoader();

    Routes routes();

    EventExecutorGroup httpExecutors();

    <T> T controllerInstance( Class<T> controllerType );
}
