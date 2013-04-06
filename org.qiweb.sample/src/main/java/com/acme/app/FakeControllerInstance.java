package com.acme.app;

import org.qiweb.api.controllers.Outcome;

import static org.qiweb.api.controllers.Controller.*;

public class FakeControllerInstance
    implements FakeController
{

    @Override
    public Outcome test()
    {
        return outcomes().ok().
            withHeader( "controller-method", "test" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "Does your test work?" ).
            build();
    }

    @Override
    public Outcome another( String id, Integer slug )
    {
        return outcomes().ok().
            withHeader( "controller-method", "another" ).
            withHeader( "controller-param-id", id ).
            withHeader( "controller-param-slug", slug.toString() ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "You sent an 'id' of " + id + " and the " + slug + " 'slug'!" ).
            build();
    }

    @Override
    public Outcome index()
    {
        response().headers().with( "X-QiWeb-HTTP-Request-Identity", request().identity() );
        return outcomes().ok().
            withHeader( "X-QiWeb-Controller-Method", "index" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "It works!"
                      + "\nApplication Mode is: " + application().mode()
                      + "\nThis request had the following ID: " + request().identity()
                      + "\n\nHeaders: " + request().headers()
                      + "\n\nCookies: " + request().cookies() ).
            build();
    }

    @Override
    public Outcome foo()
    {
        return outcomes().ok().
            withHeader( "controller-method", "foo" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "cathedral" ).
            build();
    }

    @Override
    public Outcome bar()
    {
        return outcomes().ok().
            withHeader( "controller-method", "bar" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "bazar" ).
            build();
    }
}
