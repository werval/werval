package org.qiweb.runtime.routes;

import org.junit.Test;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.http.CookiesInstance;
import org.qiweb.runtime.http.HeadersInstance;
import org.qiweb.runtime.http.QueryStringInstance;
import org.qiweb.runtime.http.RequestHeaderInstance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WildcardRoutesTest
{

    @Test
    public void testWildcardRoutes()
    {
        Route route = RouteBuilder.parseRoute(
            "GET /test/*path/as/file com.acme.app.FakeControllerInstance.wild( String path )" );

        System.out.println( route );

        assertThat( route.satisfiedBy( reqHeadFor( "/test/as/file" ) ), is( false ) );
        assertThat( route.satisfiedBy( reqHeadFor( "/test/foo/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/foo/as/file" ), equalTo( "foo" ) );
        assertThat( route.satisfiedBy( reqHeadFor( "/test/foo/bar/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/foo/bar/as/file" ), equalTo( "foo/bar" ) );
        assertThat( route.satisfiedBy( reqHeadFor( "/test/as/file/test/bar/as/file" ) ), is( true ) );
        assertThat( route.controllerParamPathValue( "path", "/test/as/file/test/bar/as/file" ), equalTo( "as/file/test/bar" ) );
    }

    private RequestHeader reqHeadFor( String path )
    {
        return new RequestHeaderInstance( "abc", "1.1", "GET",
                                          "http://localhost" + path, path,
                                          new QueryStringInstance(), new HeadersInstance(), new CookiesInstance() );
    }
}
