package org.qiweb.api.routes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.qiweb.api.http.RequestHeader;

/**
 * Route.
 * <p>HTTP RequestHeader satisfiedBy.</p>
 */
public interface Route
{

    String httpMethod();

    String path();

    Class<?> controllerType();

    Method controllerMethod();

    String controllerMethodName();

    Map<String, Class<?>> controllerParams();

    boolean satisfiedBy( RequestHeader requestHeader );

    Set<String> modifiers();

    String controllerParamPathValue( String paramName, String path );
}
