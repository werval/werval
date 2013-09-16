package org.qiweb.runtime.routes;

import com.acme.app.CustomParam;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.ApplicationInstance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that controller method parameters types lookup is working as expected.
 */
public class ParamTypeLookupTest
{

    @Test
    public void givenFullyQualifiedCustomParamTypeWhenLookupExpectFound()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( com.acme.app.CustomParam custom )" ) );
        Route route = app.routes().iterator().next();
        System.out.println( route );
        assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
    }

    @Test
    public void givenCustomParamInConfigImportedPackagesTypeWhenLookupExpectFound()
    {
        Application app = new ApplicationInstance( new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( CustomParam custom )" ) );
        Route route = app.routes().iterator().next();
        System.out.println( route );
        assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
    }
}
