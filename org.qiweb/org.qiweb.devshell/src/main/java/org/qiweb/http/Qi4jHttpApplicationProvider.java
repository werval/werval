package org.qiweb.http;

public class Qi4jHttpApplicationProvider
    implements HttpApplicationProvider
{

    private final String applicationAssemblerName;
    private final String httpLayerName;
    private final String httpModuleName;

    public Qi4jHttpApplicationProvider( String applicationAssemblerName, String httpLayerName, String httpModuleName )
    {
        this.applicationAssemblerName = applicationAssemblerName;
        this.httpLayerName = httpLayerName;
        this.httpModuleName = httpModuleName;
    }

    @Override
    public HttpApplication httpApplication()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
