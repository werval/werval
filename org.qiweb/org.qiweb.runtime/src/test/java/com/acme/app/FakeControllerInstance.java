package com.acme.app;

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
        return Outcomes.ok().
            withHeader( "controller-method", "index" ).
            as( "text/plain; charset=UTF-8" ).
            withEntity( "It works!" );
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

    @Override
    public Outcome wild( String path )
    {
        return Outcomes.notImplemented();
    }

    @Override
    public String noOutcome()
    {
        return "No Outcome";
    }

    @Override
    public Outcome customParam( CustomParam param )
    {
        return Outcomes.ok().withEntity( param.computedValue() );
    }
}
