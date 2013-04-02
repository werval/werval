package org.qiweb.api.routes;

/**
 * (De)Serialize path parameters.
 * 
 * @param <T> Path parameter type
 */
public interface PathBinder<T>
{

    /**
     * @param type Parameter type
     * @return TRUE if this PathBinder accept the given type, otherwise return FALSE
     */
    boolean accept( Class<?> type );

    T bind( java.lang.String name, java.lang.String value );

    java.lang.String unbind( java.lang.String name, T value );
}
