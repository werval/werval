package com.acme.app;

import org.qi4j.api.mixin.Mixins;
import org.qiweb.api.controllers.Outcome;

@Mixins( FakeControllerInstance.class )
public interface FakeController
{

    Outcome test();

    Outcome another( String id, Integer slug );

    Outcome index();

    Outcome foo();

    Outcome bar();

    Outcome wild( String path );
}
