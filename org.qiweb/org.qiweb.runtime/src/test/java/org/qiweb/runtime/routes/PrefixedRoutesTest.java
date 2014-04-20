/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
import org.junit.Test;
import org.qiweb.api.routes.Route;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.qiweb.api.http.Method.GET;
import static org.qiweb.api.routes.RouteBuilder.p;

/**
 * Prefixed Routes Test.
 */
public class PrefixedRoutesTest
{
    @Test
    public void prefixedRoutePath()
    {
        Route route = new RouteBuilderInstance( "pre/fix/" ).route( GET )
            .on( "/foo/:id/bar/:slug" )
            .to( FakeController.class, c -> c.another( p( "id", String.class ), p( "slug", Integer.class ) ) )
            .modifiedBy( "service", "foo" )
            .build();

        assertThat( route.path(), equalTo( "/pre/fix/foo/:id/bar/:slug" ) );
        assertThat(
            route.toString(),
            equalTo( "GET /pre/fix/foo/:id/bar/:slug com.acme.app.FakeController.another( String id, Integer slug ) service foo" )
        );
    }

    @Test
    public void prefixedRouteSatisfiedBy()
    {
        Route route = new RouteBuilderInstance( "pre/fix/" ).route( GET )
            .on( "/foo/:id/bar/:slug" )
            .to( FakeController.class, c -> c.another( p( "id", String.class ), p( "slug", Integer.class ) ) )
            .modifiedBy( "service", "foo" )
            .build();
        assertFalse( route.satisfiedBy( RoutesTest.reqHeadForGet( "/foo/testid/bar/23" ) ) );
        assertTrue( route.satisfiedBy( RoutesTest.reqHeadForGet( "/pre/fix/foo/testid/bar/23" ) ) );
    }
}