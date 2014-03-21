/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.runtime.plugins;

import java.util.Iterator;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.Route;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.context.CurrentContext.outcomes;

/**
 * Plugin Routes Test.
 */
public class PluginRoutesTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule(
        "plugin-routes-test.conf",
        new RoutesParserProvider( "GET / org.qiweb.runtime.plugins.PluginRoutesTest$ApplicationController.index" )
    );

    public static class ApplicationController
    {
        public Outcome index()
        {
            return outcomes().ok( "index" ).build();
        }
    }

    @Test
    public void pluginRoutesAreReachable()
    {
        expect()
            .statusCode( 200 )
            .body( equalTo( "prepended foo" ) )
            .when()
            .get( "/prepended/foo" );
        expect()
            .statusCode( 200 )
            .body( equalTo( "index" ) )
            .when()
            .get( "/" );
        expect()
            .statusCode( 200 )
            .body( equalTo( "appended bar" ) )
            .when()
            .get( "/appended/bar" );
    }

    @Test
    public void pluginRoutesAreInCorrectOrder()
    {
        Iterator<Route> routes = QIWEB.application().routes().iterator();
        assertThat( routes.next().controllerMethodName(), equalTo( "prepended" ) );
        assertThat( routes.next().controllerMethodName(), equalTo( "index" ) );
        assertThat( routes.next().controllerMethodName(), equalTo( "appended" ) );
        assertFalse( routes.hasNext() );
    }
}
