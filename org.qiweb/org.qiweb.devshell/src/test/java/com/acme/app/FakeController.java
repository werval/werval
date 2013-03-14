package com.acme.app;

import org.qi4j.api.mixin.Mixins;
import org.qiweb.http.controllers.Result;

@Mixins( FakeControllerInstance.class )
public interface FakeController
{

    Result test();

    Result another( String id, Integer slug );

    Result index();

    Result foo();

    Result bar();
}
