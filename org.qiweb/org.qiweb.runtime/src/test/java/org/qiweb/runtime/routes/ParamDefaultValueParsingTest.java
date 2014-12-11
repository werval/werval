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
import io.werval.api.Application;
import io.werval.api.Mode;
import io.werval.api.routes.Route;
import java.util.Map;
import org.junit.Test;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.http.QueryStringInstance;

import static io.werval.api.routes.RouteBuilder.d;
import static io.werval.api.routes.RouteBuilder.p;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ParamDefaultValueParsingTest
{
    @Test
    public void apiRouteWithParamWithDefaultValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, app
            -> singletonList(
                new RouteBuilderInstance( app )
                .route( "GET" )
                .on( "/foo/:id/bar" )
                .to( FakeController.class, c -> c.another( p( "id", String.class ), d( "slug", Integer.class, 42 ) ) )
                .modifiedBy( "service", "foo" )
                .build()
            )
        );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            assertThat(
                route.toString(),
                equalTo( "GET /foo/:id/bar com.acme.app.FakeController.another( String id, Integer slug ?= '42' ) service foo" )
            );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/foo/bazar/bar",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
            assertThat( (Integer) boundParams.get( "slug" ), equalTo( 42 ) );
            boundParams = route.bindParameters(
                application.parameterBinders(),
                "/foo/bazar/bar",
                new QueryStringInstance( singletonMap( "slug", singletonList( "23" ) ) )
            );
            assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
            assertThat( (Integer) boundParams.get( "slug" ), equalTo( 23 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void parsedRouteWithParamWithDefaultValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET /foo/:id/bar com.acme.app.FakeControllerInstance.another( String id, Integer slug ?= '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            assertThat(
                route.toString(),
                equalTo( "GET /foo/:id/bar com.acme.app.FakeControllerInstance.another( String id, Integer slug ?= '42' )" )
            );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/foo/bazar/bar",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
            assertThat( (Integer) boundParams.get( "slug" ), equalTo( 42 ) );
            boundParams = route.bindParameters(
                application.parameterBinders(),
                "/foo/bazar/bar",
                new QueryStringInstance( singletonMap( "slug", singletonList( "23" ) ) )
            );
            assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
            assertThat( (Integer) boundParams.get( "slug" ), equalTo( 23 ) );
        }
        finally
        {
            application.passivate();
        }
    }
}
