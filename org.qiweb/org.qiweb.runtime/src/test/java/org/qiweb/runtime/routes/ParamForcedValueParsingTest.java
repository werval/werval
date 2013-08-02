package org.qiweb.runtime.routes;

import com.acme.app.FakeController;
import java.util.Map;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.http.QueryStringInstance;
import org.qiweb.runtime.routes.RouteBuilder.MethodRecorder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

// TODO Write tests with weird forced values taking escaping and special chars into account
public class ParamForcedValueParsingTest
{

    @Test
    public void givenRouteBuiltFromCodeWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( new RoutesProvider()
        {
            @Override
            public Routes routes( Application application )
            {
                return RouteBuilder.routes( RouteBuilder.route( "GET" ).on( "/foo/:id/bar" ).
                    to( FakeController.class, new MethodRecorder<FakeController>()
                {
                    @Override
                    protected void call( FakeController controller )
                    {
                        controller.another( p( "id", String.class ), p( "slug", Integer.class, 42 ) );
                    }
                } ).modifiedBy( "service", "foo" ).newInstance() );
            }
        } );
        Route route = application.routes().iterator().next();
        assertThat( route.toString(), equalTo( "GET /foo/:id/bar com.acme.app.FakeController.another( String id, Integer slug = '42' ) service foo" ) );
        Map<String, Object> boundParams = route.bindParameters( application.parameterBinders(), "/foo/bazar/bar", QueryStringInstance.EMPTY );
        assertThat( (String) boundParams.get( "id" ), equalTo( "bazar" ) );
        assertThat( (Integer) boundParams.get( "slug" ), equalTo( 42 ) );
    }

    @Test
    public void givenRouteWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( new RoutesParserProvider(
            "GET / com.acme.app.FakeControllerInstance.wild( String path = '/default/value' )" ) );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        Map<String, Object> boundParams = route.bindParameters( application.parameterBinders(), "/", QueryStringInstance.EMPTY );
        assertThat( (String) boundParams.get( "path" ), equalTo( "/default/value" ) );
    }

    @Test
    public void givenAnotherRouteWithParamForcedValueWhenBindingExpectCorrectParams()
    {
        Application application = new ApplicationInstance( new RoutesParserProvider(
            "GET /*path com.acme.app.FakeControllerInstance.another( String path, Integer num = '42' )" ) );
        Route route = application.routes().iterator().next();
        System.out.println( route );
        Map<String, Object> boundParams = route.bindParameters( application.parameterBinders(), "/cathedral", QueryStringInstance.EMPTY );
        assertThat( (String) boundParams.get( "path" ), equalTo( "cathedral" ) );
        assertThat( (Integer) boundParams.get( "num" ), equalTo( 42 ) );
    }
}
