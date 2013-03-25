package com.acme.app;

import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.runtime.controllers.Outcomes;

public class FakeControllerInstance
    implements FakeController
{

    @Override
    public Outcome test()
    {
        return Outcomes.ok().
            withHeader( "controller-method", "test" ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "Does your test work?" );
    }

    @Override
    public Outcome another( String id, Integer slug )
    {
        return Outcomes.ok().
            withHeader( "controller-method", "another" ).
            withHeader( "controller-param-id", id ).
            withHeader( "controller-param-slug", slug.toString() ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "You sent an 'id' of " + id + " and the " + slug + " 'slug'!" );
    }

    @Override
    public Outcome index()
    {
        Controller.response().headers().with( "X-QiWeb-HTTP-Request-Identity",
                                              Controller.request().header().identity() );
        return Outcomes.ok().
            withHeader( "X-QiWeb-Controller-Method", "index" ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "It works!\nThis request had the following ID: " + Controller.request().header().identity()
                        + "\n\nHeaders: " + Controller.request().header().headers()
                        + "\n\nCookies: " + Controller.request().header().cookies() );
    }

    @Override
    public Outcome foo()
    {
        return Outcomes.ok().
            withHeader( "controller-method", "foo" ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "cathedral" );
    }

    @Override
    public Outcome bar()
    {
        return Outcomes.ok().
            withHeader( "controller-method", "bar" ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "bazar" );
    }
}
