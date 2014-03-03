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
package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
import java.util.Map;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.http.CookiesInstance;
import org.qiweb.runtime.http.HeadersInstance;
import org.qiweb.runtime.http.QueryStringInstance;
import org.qiweb.runtime.http.RequestHeaderInstance;
import org.qiweb.runtime.routes.RouteBuilder.MethodRecorder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qiweb.runtime.routes.RouteBuilder.route;

/**
 * Assert wildcard in routes behaviour.
 */
public class WildcardRoutesTest
{
    @Test
    public void wildAPI()
    {
        Application application = new ApplicationInstance( new RoutesProvider()
        {
            @Override
            public Routes routes( Application application )
            {
                return RouteBuilder.routes(
                    route( "GET" ).on( "/test/*path/as/file" ).to(
                        FakeController.class,
                        new MethodRecorder<FakeController>()
                        {
                            @Override
                            protected void call( FakeController controller )
                            {
                                controller.wild( p( "path", String.class ) );
                            }
                        }
                    ).newInstance()
                );
            }
        } );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        assertWild( application, route );
    }

    @Test
    public void wildParsed()
    {
        Application application = new ApplicationInstance( new RoutesParserProvider(
            "GET /test/*path/as/file com.acme.app.FakeController.wild( String path )"
        ) );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        assertWild( application, route );
    }

    private void assertWild( Application application, Route route )
    {
        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/as/file" ) ),
            is( false )
        );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/foo/as/file" ) ),
            is( true )
        );

        assertThat(
            (String) route.bindParameters(
                application.parameterBinders(),
                "/test/foo/as/file",
                QueryStringInstance.EMPTY
            ).get( "path" ),
            equalTo( "foo" )
        );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/foo/bar/as/file" ) ),
            is( true )
        );

        assertThat(
            (String) route.bindParameters(
                application.parameterBinders(),
                "/test/foo/bar/as/file",
                QueryStringInstance.EMPTY
            ).get( "path" ),
            equalTo( "foo/bar" )
        );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/as/file/test/bar/as/file" ) ),
            is( true )
        );

        assertThat(
            (String) route.bindParameters(
                application.parameterBinders(),
                "/test/as/file/test/bar/as/file",
                QueryStringInstance.EMPTY
            ).get( "path" ),
            equalTo( "as/file/test/bar" )
        );
    }

    private RequestHeader reqHeadForGet( String path )
    {
        return new RequestHeaderInstance(
            "abc",
            "127.0.0.1",
            "HTTP/1.1",
            "GET",
            "http://localhost" + path,
            path,
            QueryStringInstance.EMPTY,
            new HeadersInstance( false ),
            new CookiesInstance()
        );
    }

    @Test
    public void forcedWild()
    {
        Application application = new ApplicationInstance( new RoutesParserProvider(
            "GET /tree/*path com.acme.app.FakeController.forcedWild( String root = 'src/test/resources', String path )"
        ) );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        Map<String, Object> boundParams = route.bindParameters(
            application.parameterBinders(),
            "/tree/staticfiles/",
            QueryStringInstance.EMPTY
        );
        assertThat( (String) boundParams.get( "root" ), equalTo( "src/test/resources" ) );
        assertThat( (String) boundParams.get( "path" ), equalTo( "staticfiles/" ) );
        boundParams = route.bindParameters(
            application.parameterBinders(),
            "/tree/staticfiles",
            QueryStringInstance.EMPTY
        );
        assertThat( (String) boundParams.get( "root" ), equalTo( "src/test/resources" ) );
        assertThat( (String) boundParams.get( "path" ), equalTo( "staticfiles" ) );
    }
}
