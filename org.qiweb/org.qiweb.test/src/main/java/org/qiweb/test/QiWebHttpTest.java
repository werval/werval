/*
 * Copyright (c) 2013-2014 the original author or authors
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

import org.junit.After;
import org.junit.Before;
import org.qiweb.api.Mode;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.ConfigInstance;
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.server.netty.NettyServer;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.server.HttpServer;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;
import static org.qiweb.test.QiWebTestHelper.setupRestAssuredDefaults;

/**
 * Base QiWeb HTTP JUnit Test.
 * <p>
 * Activate/Passivate QiWeb Application and HTTP Server in test mode around each JUnit test method.
 * <p>
 * By default, configuration is loaded from the <code>application.conf</code> file.
 * Override the {@link #configurationResourceName()} method to provide your own test configuration.
 * <p>
 * By default, routes are loaded from the <code>routes.conf</code> file.
 * Override the {@link #routesProvider()} method to provide your own test routes.
 */
public class QiWebHttpTest
    implements QiWebHttpTestSupport
{
    private final String configurationResourceNameOverride;
    private final RoutesProvider routesProviderOverride;
    private HttpServer httpServer;
    private ApplicationSPI app;

    public QiWebHttpTest()
    {
        this( null, null );
    }

    public QiWebHttpTest( String configurationResourceNameOverride )
    {
        this( configurationResourceNameOverride, null );
    }

    public QiWebHttpTest( RoutesProvider routesProviderOverride )
    {
        this( null, routesProviderOverride );
    }

    public QiWebHttpTest( String configurationResourceNameOverride, RoutesProvider routesProviderOverride )
    {
        this.configurationResourceNameOverride = configurationResourceNameOverride;
        this.routesProviderOverride = routesProviderOverride;
    }

    /**
     * Activate HttpServer.
     */
    @Before
    public final void beforeEachQiWebTestMethod()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        String conf = configurationResourceNameOverride == null
                      ? configurationResourceName()
                      : configurationResourceNameOverride;
        ConfigInstance config = new ConfigInstance( classLoader, conf );
        RoutesProvider routesProvider = routesProviderOverride == null
                                        ? routesProvider()
                                        : routesProviderOverride;
        app = new ApplicationInstance( Mode.TEST, config, classLoader, routesProvider );
        httpServer = new NettyServer( app );
        httpServer.activate();
        setupRestAssuredDefaults( config );
    }

    /**
     * Passivate HttpServer.
     */
    @After
    public final void afterEachQiWebTestMethod()
    {
        httpServer.passivate();
        httpServer = null;
        QiWebTestHelper.printErrorsTrace( app.errors() );
        app = null;
    }

    @Override
    public final ApplicationSPI application()
    {
        return app;
    }

    @Override
    public final String httpHost()
    {
        String httpHost = app.config().string( QIWEB_HTTP_ADDRESS );
        if( "127.0.0.1".equals( httpHost ) )
        {
            httpHost = "localhost";
        }
        return httpHost;
    }

    @Override
    public final int httpPort()
    {
        return app.config().intNumber( QIWEB_HTTP_PORT );
    }

    @Override
    public final String baseHttpUrl()
    {
        return "http://" + httpHost() + ":" + httpPort();
    }

    /**
     * Override to provide your own configuration.
     *
     * @return Configuration resource name
     */
    protected String configurationResourceName()
    {
        return "application.conf";
    }

    /**
     * Override to provide your own routes.
     *
     * @return Routes provider
     */
    protected RoutesProvider routesProvider()
    {
        return new RoutesConfProvider();
    }
}
