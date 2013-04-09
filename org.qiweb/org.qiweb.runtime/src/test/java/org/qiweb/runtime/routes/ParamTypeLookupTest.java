package org.qiweb.runtime.routes;

import com.acme.app.CustomParam;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.ConfigInstance;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.qi4j.functional.Iterables.*;

/**
 * Assert that controller method parameters types lookup is working as expected.
 */
public class ParamTypeLookupTest
{

    private final Config config = new ConfigInstance();

    @Test
    public void givenFullyQualifiedCustomParamTypeWhenLookupExpectFound()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( com.acme.app.CustomParam custom )" ) );
        Route route = first( app.routes() );
        System.out.println( route );
        assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
    }

    @Test
    public void givenCustomParamInConfigImportedPackagesTypeWhenLookupExpectFound()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( CustomParam custom )" ) );
        Route route = first( app.routes() );
        System.out.println( route );
        assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
    }
}
