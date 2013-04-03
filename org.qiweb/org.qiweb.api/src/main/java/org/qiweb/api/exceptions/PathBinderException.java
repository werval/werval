package org.qiweb.api.exceptions;

/**
 * Thrown when something goes wrong when using PathBinders.
 */
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
