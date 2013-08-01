package org.qiweb.api.routes;

/**
 * (De)Serialize parameters.
 * 
 * @param <T> Parameter type
 */
public interface ParameterBinder<T>
{

    /**
     * @param type Parameter type
     * @return TRUE if this ParameterBinder accept the given type, otherwise return FALSE
     */
    boolean accept( Class<?> type );

    T bind( String name, String value );

    String unbind( String name, T value );
}
