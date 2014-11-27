/*
 * Copyright (c) 2013 the original author or authors
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

import com.acme.app.CustomParam;
import org.junit.Test;
import org.qiweb.api.Application;
import org.qiweb.api.Mode;
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
        Application app = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( com.acme.app.CustomParam custom )" ) );
        app.activate();
        try
        {
            Route route = app.routes().iterator().next();
            System.out.println( route );
            assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
        }
        finally
        {
            app.passivate();
        }
    }

    @Test
    public void givenCustomParamInConfigImportedPackagesTypeWhenLookupExpectFound()
    {
        Application app = new ApplicationInstance( Mode.TEST, new RoutesParserProvider(
            "GET /:custom com.acme.app.FakeController.customParam( CustomParam custom )" ) );
        app.activate();
        try
        {
            Route route = app.routes().iterator().next();
            System.out.println( route );
            assertThat( ( (RouteInstance) route ).controllerParams().get( "custom" ).type().getName(), equalTo( CustomParam.class.getName() ) );
        }
        finally
        {
            app.passivate();
        }
    }
}
