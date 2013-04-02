package org.qiweb.api.routes;

public interface PathBinders
{

    <T> T bind( Class<T> type, String pathParamName, String pathParamValue );

    <T> String unbind( Class<T> type, String pathParamName, T pathParamValue );
}
