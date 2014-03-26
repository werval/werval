/**
 * Copyright (c) 2013-2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.app;

import org.qiweb.api.outcomes.Outcome;

import static org.qiweb.api.context.CurrentContext.outcomes;

public class FakeControllerInstance
    implements FakeController
{
    @Override
    public String noOutcome()
    {
        return "No Outcome";
    }

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
    public Outcome forcedWild( String root, String path )
    {
        return outcomes().notImplemented().build();
    }

    @Override
    public Outcome customParam( CustomParam param )
    {
        return outcomes().ok().withBody( param.computedValue() ).build();
    }
}
