package com.acme.app;

import org.qiweb.api.outcomes.Outcome;

public interface FakeController
{

    Outcome test();

    Outcome another( String id, Integer slug );

    Outcome index();

    Outcome foo();

    Outcome bar();

    Outcome wild( String path );

    String noOutcome();

    Outcome customParam( CustomParam param );

}
