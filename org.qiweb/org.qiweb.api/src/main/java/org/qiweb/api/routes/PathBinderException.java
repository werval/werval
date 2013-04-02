package org.qiweb.api.routes;

import org.qiweb.api.QiWebException;

public class PathBinderException
    extends QiWebException
{

    private static final long serialVersionUID = 1L;

    public PathBinderException( String message )
    {
        super( message );
    }

    public PathBinderException( Throwable cause )
    {
        super( cause );
    }

    public PathBinderException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
