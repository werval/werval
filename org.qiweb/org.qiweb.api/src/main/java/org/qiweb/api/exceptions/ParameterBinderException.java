package org.qiweb.api.exceptions;

/**
 * Thrown when something goes wrong when using ParameterBinders.
 */
public class ParameterBinderException
    extends QiWebException
{

    private static final long serialVersionUID = 1L;

    public ParameterBinderException( String message )
    {
        super( message );
    }

    public ParameterBinderException( Throwable cause )
    {
        super( cause );
    }

    public ParameterBinderException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
