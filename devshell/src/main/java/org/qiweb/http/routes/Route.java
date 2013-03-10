package org.qiweb.http.routes;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
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

    String controllerMethodName();

    Iterable<Couple<String, Class<?>>> controllerParams();
}
