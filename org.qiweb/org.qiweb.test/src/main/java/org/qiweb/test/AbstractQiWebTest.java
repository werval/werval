/**
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
package org.qiweb.test;

import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.qiweb.api.Application;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServerInstance;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;

/**
 * Base QiWeb Test.
 */
public abstract class AbstractQiWebTest
{

    private HttpServerInstance httpServer;
    private ApplicationInstance app;

    /**
     * Activate HttpServer.
     */
    @Before
    public final void beforeEachTest()
    {
        RoutesProvider routesProvider = new RoutesParserProvider( routesString() );
        app = new ApplicationInstance( routesProvider );
        httpServer = new HttpServerInstance( "qiweb-test", app );
        httpServer.activate();

        // Setup RestAssured if present
        try
        {
            Field restAssuredPortField = Class.forName( "com.jayway.restassured.RestAssured" ).getField( "port" );
            restAssuredPortField.set( null, app.config().intNumber( QIWEB_HTTP_PORT ) );
            Field restAssuredBaseURLField = Class.forName( "com.jayway.restassured.RestAssured" ).getField( "baseURL" );
            restAssuredBaseURLField.set( null, "http://" + app.config().string( QIWEB_HTTP_ADDRESS ) );
        }
        catch( ClassNotFoundException | NoSuchFieldException |
               IllegalArgumentException | IllegalAccessException noRestAssured )
        {
            // RestAssured is not present, we simply don't configure it.
        }
    }

    /**
     * Passivate HttpServer.
     */
    @After
    public final void afterEachTest()
    {
        httpServer.passivate();
        httpServer = null;
        app = null;
    }

    protected abstract String routesString();

    /**
     * @return Application
     */
    protected final Application application()
    {
        return app;
    }

    protected final String baseHttpUrl()
    {
        return "http://" + app.config().string( QIWEB_HTTP_ADDRESS ) + ":" + app.config().string( QIWEB_HTTP_PORT );
    }
}
