package org.qiweb.api.routes;

import java.lang.reflect.Method;
import java.util.Map;
import org.qiweb.api.http.RequestHeader;

/**
 * Route. HttpRequest satisfiedBy.
 */
public interface Route
{

    String httpMethod();

    String path();

    Class<?> controllerType();

    Method controllerMethod();

    String controllerMethodName();

    Map<String, Class<?>> controllerParams();

    String controllerParamPathValue( String paramName, String path );

    boolean satisfiedBy( RequestHeader requestHeader );
}
