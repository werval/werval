package org.qiweb.runtime.exceptions;

import org.qiweb.api.exceptions.QiWebException;

/**
 * Thrown when an unexpected error occurs inside the QiWeb Runtime.
 */
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
