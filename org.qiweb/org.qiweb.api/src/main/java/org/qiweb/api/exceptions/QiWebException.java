package org.qiweb.api.exceptions;

/**
 * Base exception for all QiWeb errors.
 */
public class QiWebException
    extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public QiWebException( String message )
    {
        super( message );
    }

    public QiWebException( Throwable cause )
    {
        super( cause.getMessage(), cause );
    }

    public QiWebException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
