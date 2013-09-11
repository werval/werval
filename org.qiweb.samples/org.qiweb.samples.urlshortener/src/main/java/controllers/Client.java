package controllers;

import org.qiweb.api.controllers.Outcome;

import static org.qiweb.api.controllers.Controller.outcomes;

public class Client
{

    public Outcome index()
    {
        return outcomes().notImplemented().build();
    }
}
