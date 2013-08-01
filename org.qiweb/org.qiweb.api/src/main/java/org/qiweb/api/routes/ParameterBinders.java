package org.qiweb.api.routes;

public interface ParameterBinders
{

    <T> T bind( Class<T> type, String paramName, String paramValue );

    <T> String unbind( Class<T> type, String paramName, T paramValue );
}
