package org.qiweb.http.server;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;

@Mixins( HttpServerInstance.class )
public interface HttpServer
    extends ServiceActivation
{
}
