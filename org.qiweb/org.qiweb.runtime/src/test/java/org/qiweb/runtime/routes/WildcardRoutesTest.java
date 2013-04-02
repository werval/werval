package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
import org.junit.Test;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.routes.Route;
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
        Route route = route( "GET" ).on( "/test/*path/as/file" ).
            to( FakeController.class, new MethodRecorder<FakeController>()
        {
            @Override
            protected void call( FakeController controller )
            {
                controller.wild( p( "path", String.class ) );
            }
        } ).newInstance();
        System.out.println( route );
        assertWilcardRoute( route );
    }

    @Test
    public void testWildcardRoutes()
    {
        Route route = RouteBuilder.parseRoute(
            "GET /test/*path/as/file com.acme.app.FakeController.wild( String path )" );
        System.out.println( route );
        assertWilcardRoute( route );
    }

    private void assertWilcardRoute( Route route )
    {
        assertThat( route.satisfiedBy( reqHeadForGet( "/test/as/file" ) ), is( false ) );
        assertThat( route.satisfiedBy( reqHeadForGet( "/test/foo/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/foo/as/file" ), equalTo( "foo" ) );
        assertThat( route.satisfiedBy( reqHeadForGet( "/test/foo/bar/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/foo/bar/as/file" ), equalTo( "foo/bar" ) );
        assertThat( route.satisfiedBy( reqHeadForGet( "/test/as/file/test/bar/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/as/file/test/bar/as/file" ), equalTo( "as/file/test/bar" ) );
    }

    private RequestHeader reqHeadForGet( String path )
    {
        return new RequestHeaderInstance( "abc", "HTTP/1.1", "GET",
                                          "http://localhost" + path, path,
                                          new QueryStringInstance(), new HeadersInstance(), new CookiesInstance() );
    }
}
