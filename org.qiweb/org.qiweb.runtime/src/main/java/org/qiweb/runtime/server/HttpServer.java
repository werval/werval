package org.qiweb.runtime.server;

public interface HttpServer
{

    void activateService()
        throws Exception;

    void passivateService()
        throws Exception;
}
