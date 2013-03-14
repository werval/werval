package org.qiweb.http.controllers;

import org.qiweb.http.routes.Route;

/**
 * Controllers are Transients or Services.
 */
public interface Controllers
{

    ControllerMethod forRoute( Class<?> controllerType, Route route );
}
