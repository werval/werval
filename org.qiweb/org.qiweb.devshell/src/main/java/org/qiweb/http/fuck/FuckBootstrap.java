package org.qiweb.http.fuck;

import org.qiweb.http.HttpApplication;
import org.qiweb.http.HttpApplicationProvider;

public class FuckBootstrap
    implements HttpApplicationProvider
{

    @Override
    public HttpApplication httpApplication()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
