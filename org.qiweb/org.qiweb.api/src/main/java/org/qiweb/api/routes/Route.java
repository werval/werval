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

    boolean satisfiedBy( RequestHeader requestHeader );

    Set<String> modifiers();

    Map<String, Object> bindPath( PathBinders pathBinders, String path );
}
