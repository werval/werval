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

import io.werval.api.Mode;
import io.werval.runtime.ApplicationInstance;
import io.werval.runtime.ConfigInstance;
import io.werval.runtime.ConfigKeys;
import io.werval.runtime.CryptoInstance;
import io.werval.runtime.routes.RoutesConfProvider;
import io.werval.runtime.routes.RoutesProvider;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.qiweb.server.netty.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonMap;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ADDRESS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_PORT;
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
 *
 * @navcomposed 1 - 1 ApplicationSPI
 * @navcomposed 1 - 1 HttpServer
 */
public class QiWebHttpTest
    implements QiWebHttpTestSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( QiWebHttpTest.class );
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
        try
        {
            config.string( ConfigKeys.APP_SECRET );
        }
        catch( com.typesafe.config.ConfigException.Missing noAppSecret )
        {
            String secret = CryptoInstance.newRandomSecret256BitsHex();
            LOG.info( "Application has no 'app.secret', using a random one for test mode: {}", secret );
            config = new ConfigInstance( classLoader, conf, null, null, singletonMap( "app.secret", secret ) );
        }
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
        String httpHost = app.config().string( WERVAL_HTTP_ADDRESS );
        if( "127.0.0.1".equals( httpHost ) )
        {
            httpHost = "localhost";
        }
        return httpHost;
    }

    @Override
    public final int httpPort()
    {
        return app.config().intNumber( WERVAL_HTTP_PORT );
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
        return null;
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
