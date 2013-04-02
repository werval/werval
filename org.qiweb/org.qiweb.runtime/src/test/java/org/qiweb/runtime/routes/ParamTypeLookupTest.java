package org.qiweb.runtime.routes;

import com.acme.app.CustomParam;
import org.junit.Ignore;
import org.junit.Test;
import org.qiweb.api.Config;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.ConfigInstance;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Assert that controller method parameters types lookup is working as expected.
 */
public class ParamTypeLookupTest
{

    private final Config config = new ConfigInstance();

    @Test
    public void givenFullyQualifiedCustomParamTypeWhenLookupExpectFound()
    {
        Route route = RouteBuilder.parseRoute(
            config,
            "GET /:custom com.acme.app.FakeController.customParam( com.acme.app.CustomParam custom )" );
        System.out.println( route );
        assertThat( route.controllerParams().get( "custom" ).getName(), equalTo( CustomParam.class.getName() ) );
    }

    @Test
    public void givenCustomParamInConfigImportedPackagesTypeWhenLookupExpectFound()
    {
        Route route = RouteBuilder.parseRoute(
            config,
            "GET /:custom com.acme.app.FakeController.customParam( CustomParam custom )" );
        System.out.println( route );
        assertThat( route.controllerParams().get( "custom" ).getName(), equalTo( CustomParam.class.getName() ) );
    }
}
