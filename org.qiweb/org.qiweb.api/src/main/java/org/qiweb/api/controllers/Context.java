package org.qiweb.api.controllers;

import org.qiweb.api.http.Flash;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.Session;

public interface Context
{

    Request request();

    Flash flash();

    Session session();
}
