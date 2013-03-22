package org.qiweb.runtime.http.routes;

import java.lang.reflect.Method;
import org.codeartisans.java.toolbox.Couple;
import org.qi4j.functional.Specification;
import org.qiweb.api.http.HttpRequestHeader;

/**
 * Route. HttpRequest satisfiedBy.
 */
public interface Route
    extends Specification<HttpRequestHeader>
{

    String httpMethod();

    String path();

    Class<?> controllerType();

    Method controllerMethod();

    String controllerMethodName();

    Iterable<Couple<String, Class<?>>> controllerParams();

    String controllerParamPathValue( String paramName, String path );
}
