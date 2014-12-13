/*
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
package io.werval.runtime.routes;

import com.acme.app.FakeController;
import io.werval.api.Application;
import io.werval.api.Mode;
import io.werval.api.exceptions.IllegalRouteException;
import io.werval.api.routes.Route;
import io.werval.runtime.ApplicationInstance;
import io.werval.runtime.http.QueryStringInstance;
import java.util.Map;
import java.util.concurrent.CompletionException;
import org.junit.Test;

import static io.werval.api.routes.RouteBuilder.f;
import static io.werval.api.routes.RouteBuilder.p;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ParamForcedValueParsingTest
{
    @Test
    public void givenRouteBuiltFromCodeWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( Mode.TEST, app
            -> singletonList(
                new RouteBuilderInstance( app )
                .route( "GET" )
                .on( "/foo/:id/bar" )
                .to( FakeController.class, c -> c.another( p( "id", String.class ), f( "slug", Integer.class, 42 ) ) )
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
                equalTo( "GET /foo/:id/bar com.acme.app.FakeController.another( String id, Integer slug = '42' ) service foo" )
            );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/foo/bazar/bar",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
            assertThat( (Integer) boundParams.get( "slug" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void givenRouteWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.wild( String path = '/default/value' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "/default/value" ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void givenAnotherRouteWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET /*path com.acme.app.FakeControllerInstance.another( String path, Integer num = '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/cathedral",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "cathedral" ) );
            assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test( expected = IllegalRouteException.class )
    public void quoteInQuotedForcedValue()
        throws Throwable
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.another( String path = ''', Integer num = '42' )"
        ) );
        try
        {
            application.activate();
        }
        catch( CompletionException ex )
        {
            throw ex.getCause();
        }
    }

    @Test
    public void escapedQuoteInQuotedForcedValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.another( String path = '\\'', Integer num = '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "'" ) );
            assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void commaInQuotedForcedValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.another( String path = ',', Integer num = '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "," ) );
            assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void equalsInQuotedForcedValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.another( String path = '1=2', Integer num = '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "1=2" ) );
            assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void questionEqualsInQuotedForcedValue()
    {
        Application application = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.another( String path = '1?=2', Integer num = '42' )"
        ) );
        application.activate();
        try
        {
            Route route = application.routes().iterator().next();
            System.out.println( route );
            Map<String, Object> boundParams = route.bindParameters(
                application.parameterBinders(),
                "/",
                QueryStringInstance.EMPTY
            );
            assertThat( (String) boundParams.get( "path" ), equalTo( "1?=2" ) );
            assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
        }
        finally
        {
            application.passivate();
        }
    }
}
