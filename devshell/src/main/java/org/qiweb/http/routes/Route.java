package org.qiweb.http.routes;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import java.lang.reflect.Method;
import org.codeartisans.java.toolbox.Couple;
import org.qi4j.functional.Specification;

/**
 * Route. HttpRequest satisfiedBy.
 */
public interface Route
    extends Specification<HttpRequest>
{

    HttpMethod httpMethod();

    String path();

    Class<?> controllerType();

    Method controllerMethod();

    String controllerMethodName();

    Iterable<Couple<String, Class<?>>> controllerParams();

    String controllerParamPathValue( String paramName, String path );
}
