package org.qiweb.api.controllers;

import org.qiweb.api.http.Headers;

/**
 * Outcome of a HTTP Request processing.
 */
public interface Outcome
{

    int status();

    Headers headers();
}
