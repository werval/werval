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
import org.qiweb.api.Application.Mode;
import org.qiweb.api.Config;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.ConfigInstance;
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.server.HttpServerInstance;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;

/**
 * Base QiWeb JUnit Test.
 * 
 * <p>Activate/Passivate QiWeb Application in test mode around each JUnit test.</p>
 * <p>
 *     By default, configuration is loaded from the <code>application.conf</conf> file.
 *     Override the {@link #configurationResourceName()} method to provide your own test configuration.
 * </p>
 * <p>
 *     By default, routes are loaded from the <code>routes.conf</code> file.
 *     Override the {@link #routesProvider()} method to provide your own test routes.
 * </p>
 */
public class QiWebTest
{

    private final String configurationResourceNameOverride;
    private final RoutesProvider routesProviderOverride;
    private HttpServerInstance httpServer;
    private ApplicationInstance app;

    public QiWebTest()
    {
        this( null, null );
    }

    /* package */ QiWebTest( String configurationResourceNameOverride, RoutesProvider routesProviderOverride )
    {
        this.configurationResourceNameOverride = configurationResourceNameOverride;
        this.routesProviderOverride = routesProviderOverride;
    }

    /**
     * Activate HttpServer.
     */
    @Before
    public final void beforeEachTest()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        Config config = new ConfigInstance( classLoader, configurationResourceNameOverride == null
                                                         ? configurationResourceName()
                                                         : configurationResourceNameOverride );
        RoutesProvider routesProvider = routesProviderOverride == null
                                        ? routesProvider()
                                        : routesProviderOverride;
        app = new ApplicationInstance( Mode.TEST, config, classLoader, routesProvider );
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

    /**
     * @return Application
     */
    protected final Application application()
    {
        return app;
    }

    /**
     * @return Base HTTP URL based on QiWeb listening address and port Configuration.
     */
    protected final String baseHttpUrl()
    {
        String httpAddress = app.config().string( QIWEB_HTTP_ADDRESS );
        if( "127.0.0.1".equals( httpAddress ) )
        {
            httpAddress = "localhost";
        }
        return "http://" + httpAddress + ":" + app.config().string( QIWEB_HTTP_PORT );
    }

    /**
     * Override to provide your own configuration.
     */
    protected String configurationResourceName()
    {
        return "application.conf";
    }

    /**
     * Override to provide your own routes.
     */
    protected RoutesProvider routesProvider()
    {
        return new RoutesConfProvider();
    }
}
