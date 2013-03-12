package com.acme.app;

import org.qiweb.http.controllers.Result;
import org.qiweb.http.controllers.Results;

public class FakeControllerInstance
    implements FakeController
{

    @Override
    public Result test()
    {
        return Results.ok().withHeader( "controller-method", "test" ).as( "text/plain; charset=UTF-8" ).withBody( "Does your test work?" );
    }

    @Override
    public Result another( String id, Integer slug )
    {
        return Results.ok().withHeader( "controller-method", "another" ).withHeader( "controller-param-id", id ).withHeader( "controller-param-slug", slug.toString() ).as( "text/plain; charset=UTF-8" ).withBody( "You sent an 'id' of " + id + " and the " + slug + " 'slug'!" );
    }

    @Override
    public Result index()
    {
        return Results.ok().withHeader( "controller-method", "index" ).as( "text/plain; charset=UTF-8" ).withBody( "It works!" );
    }

    @Override
    public Result foo()
    {
        return Results.ok().withHeader( "controller-method", "foo" ).as( "text/plain; charset=UTF-8" ).withBody( "cathedral" );
    }

    @Override
    public Result bar()
    {
        return Results.ok().withHeader( "controller-method", "bar" ).as( "text/plain; charset=UTF-8" ).withBody( "bazar" );
    }
}
