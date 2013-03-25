package org.qiweb.api.http;

import org.qiweb.api.controllers.Outcome;

public interface Response
{

    void apply( Outcome result );
}
