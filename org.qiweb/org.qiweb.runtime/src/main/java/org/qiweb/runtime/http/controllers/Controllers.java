package org.qiweb.runtime.http.controllers;

import org.qiweb.runtime.http.routes.Route;

/**
 * Controllers are Transients or Services.
 */
public interface Controllers
{

    ControllerMethod forRoute( Class<?> controllerType, Route route );
}
