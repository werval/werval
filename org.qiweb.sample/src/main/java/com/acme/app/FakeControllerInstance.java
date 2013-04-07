package com.acme.app;

import java.util.List;
import java.util.Map.Entry;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.http.Cookies.Cookie;

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
        StringBuilder sb = new StringBuilder( "It works!\n" );
        sb.append( "Application Mode is: " ).append( application().mode() ).append( "\n" );
        sb.append( "Request identity is: " ).append( request().identity() ).append( "\n" );
        sb.append( "Request path is: " ).append( request().path() ).append( "\n" );
        sb.append( "\nRequest Headers:\n" );
        for( Entry<String, List<String>> header : request().headers().asMapAll().entrySet() )
        {
            sb.append( "\t" ).append( header.getKey() ).append( ": " ).append( header.getValue() ).append( ",\n" );
        }
        sb.append( "\nRequest Cookies:\n" );
        for( Cookie cookie : request().cookies() )
        {
            sb.append( "\t" ).append( cookie.name() ).append( ": " ).append( cookie ).append( ",\n" );
        }
        return outcomes().ok().
            as( "text/plain; charset=UTF-8" ).
            withBody( sb.toString() ).
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
