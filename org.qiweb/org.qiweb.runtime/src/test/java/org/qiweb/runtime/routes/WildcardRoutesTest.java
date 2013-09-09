package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
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

public class WildcardRoutesTest
{

    @Test
    public void testAPI()
    {
        Application application = new ApplicationInstance( new RoutesProvider()
        {
            @Override
            public Routes routes( Application application )
            {
                return RouteBuilder.routes( route( "GET" ).on( "/test/*path/as/file" ).
                    to( FakeController.class, new MethodRecorder<FakeController>()
                {
                    @Override
                    protected void call( FakeController controller )
                    {
                        controller.wild( p( "path", String.class ) );
                    }
                } ).newInstance() );
            }
        } );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        assertWilcardRoute( application, route );
    }

    @Test
    public void testWildcardRoutes()
    {
        Application application = new ApplicationInstance( new RoutesParserProvider(
            "GET /test/*path/as/file com.acme.app.FakeController.wild( String path )" ) );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        assertWilcardRoute( application, route );
    }

    private void assertWilcardRoute( Application application, Route route )
    {
        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/as/file" ) ),
            is( false ) );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/foo/as/file" ) ),
            is( true ) );

        assertThat(
            (String) route.bindParameters( application.parameterBinders(), "/test/foo/as/file", QueryStringInstance.EMPTY ).get( "path" ),
            equalTo( "foo" ) );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/foo/bar/as/file" ) ),
            is( true ) );

        assertThat(
            (String) route.bindParameters( application.parameterBinders(), "/test/foo/bar/as/file", QueryStringInstance.EMPTY ).get( "path" ),
            equalTo( "foo/bar" ) );

        assertThat(
            route.satisfiedBy( reqHeadForGet( "/test/as/file/test/bar/as/file" ) ),
            is( true ) );

        assertThat(
            (String) route.bindParameters( application.parameterBinders(), "/test/as/file/test/bar/as/file", QueryStringInstance.EMPTY ).get( "path" ),
            equalTo( "as/file/test/bar" ) );
    }

    private RequestHeader reqHeadForGet( String path )
    {
        return new RequestHeaderInstance( "abc", "HTTP/1.1", "GET",
                                          "http://localhost" + path, path,
                                          QueryStringInstance.EMPTY, new HeadersInstance( false ), new CookiesInstance() );
    }
}
