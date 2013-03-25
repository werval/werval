package org.qiweb.runtime;

import org.qiweb.api.QiWebException;

public class QiWebRuntimeException
    extends QiWebException
{

    private static final long serialVersionUID = 1L;

    public QiWebRuntimeException( String message )
    {
        super( message );
    }

    public QiWebRuntimeException( Throwable cause )
    {
        super( cause );
    }

    public QiWebRuntimeException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
