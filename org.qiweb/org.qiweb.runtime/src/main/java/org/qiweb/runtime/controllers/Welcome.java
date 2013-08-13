package org.qiweb.runtime.controllers;

import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;

public class Welcome
{

    public Outcome welcome()
    {
        return Controller.outcomes().ok( "Welcome!" ).build();
    }
}
