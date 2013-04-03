package org.qiweb.devshell;

import org.qiweb.api.exceptions.QiWebException;

public class QiWebDevShellException
    extends QiWebException
{

    private static final long serialVersionUID = 1L;

    public QiWebDevShellException( String message )
    {
        super( message );
    }

    public QiWebDevShellException( Throwable cause )
    {
        super( cause );
    }

    public QiWebDevShellException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
