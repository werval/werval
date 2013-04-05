package com.acme.app;

import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Cookies;

import static org.qiweb.api.controllers.Controller.*;

public class FakeControllerInstance
    implements FakeController
{

    @Override
    public Outcome test()
    {
        return outcomes().
            ok().
            withHeader( "controller-method", "test" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "Does your test work?" ).
            build();
    }

    @Override
    public Outcome another( String id, Integer slug )
    {
        return outcomes().
            ok().
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
        return outcomes().ok().
            withHeader( "controller-method", "index" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "It works!" ).
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
        return outcomes().
            ok().
            withHeader( "controller-method", "bar" ).
            as( "text/plain; charset=UTF-8" ).
            withBody( "bazar" ).
            build();
    }

    @Override
    public Outcome wild( String path )
    {
        return outcomes().notImplemented().build();
    }

    @Override
    public String noOutcome()
    {
        return "No Outcome";
    }

    @Override
    public Outcome customParam( CustomParam param )
    {
        Cookies requestCookies = request().cookies();
        Cookies responseCookies = response().cookies();
        return outcomes().ok().withBody( param.computedValue() ).build();
    }
}
