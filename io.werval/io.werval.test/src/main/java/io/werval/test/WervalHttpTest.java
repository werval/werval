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
package io.werval.test;

import java.util.LinkedHashMap;
import java.util.Map;

import io.werval.api.Mode;
import io.werval.runtime.ApplicationInstance;
import io.werval.runtime.ConfigInstance;
import io.werval.runtime.ConfigKeys;
import io.werval.runtime.CryptoInstance;
import io.werval.runtime.routes.RoutesConfProvider;
import io.werval.runtime.routes.RoutesProvider;
import io.werval.server.netty.NettyServer;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.server.HttpServer;
import io.werval.test.util.FreePortFinder;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ADDRESS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_PORT;
import static io.werval.test.WervalTestHelper.setupRestAssuredDefaults;

/**
 * Base Werval HTTP JUnit Test.
 * <p>
 * Activate/Passivate Werval Application and HTTP Server in test mode around each JUnit test method.
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
public class WervalHttpTest
    implements WervalHttpTestSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( WervalHttpTest.class );
    private final String configurationResourceNameOverride;
    private final RoutesProvider routesProviderOverride;
    private HttpServer httpServer;
    private ApplicationSPI app;

    public WervalHttpTest()
    {
        this( null, null );
    }

    public WervalHttpTest( String configurationResourceNameOverride )
    {
        this( configurationResourceNameOverride, null );
    }

    public WervalHttpTest( RoutesProvider routesProviderOverride )
    {
        this( null, routesProviderOverride );
    }

    public WervalHttpTest( String configurationResourceNameOverride, RoutesProvider routesProviderOverride )
    {
        this.configurationResourceNameOverride = configurationResourceNameOverride;
        this.routesProviderOverride = routesProviderOverride;
    }

    /**
     * Activate HttpServer.
     */
    @Before
    public final void beforeEachTestMethod()
    {
        // Classloader
        ClassLoader classLoader = getClass().getClassLoader();

        // Configuration
        String conf = configurationResourceNameOverride == null
                      ? configurationResourceName()
                      : configurationResourceNameOverride;
        ConfigInstance config = new ConfigInstance( classLoader, conf );

        // Configuration overrides
        Map<String, Object> overrides = new LinkedHashMap<>( 2 );
        String address = config.string( WERVAL_HTTP_ADDRESS );
        int freePort = FreePortFinder.findRandomOnInterfaceByName( address );
        LOG.info( "Application will forcibly listen on {}:{} for test mode", address, freePort );
        overrides.put( WERVAL_HTTP_PORT, freePort );
        try
        {
            config.string( ConfigKeys.APP_SECRET );
        }
        catch( com.typesafe.config.ConfigException.Missing noAppSecret )
        {
            String secret = CryptoInstance.newWeaklyRandomSecret256BitsHex();
            LOG.info( "Application has no 'app.secret', using a weakly random one for test mode: {}", secret );
            overrides.put( "app.secret", secret );
        }
        config = new ConfigInstance( classLoader, conf, null, null, overrides );

        // Routes
        RoutesProvider routesProvider = routesProviderOverride == null
                                        ? routesProvider()
                                        : routesProviderOverride;

        // Application
        app = new ApplicationInstance( Mode.TEST, config, classLoader, routesProvider );

        // Server
        httpServer = new NettyServer( app );
        httpServer.activate();

        // Final setups
        setupRestAssuredDefaults( config );
    }

    /**
     * Passivate HttpServer.
     */
    @After
    public final void afterEachTestMethod()
    {
        httpServer.passivate();
        httpServer = null;
        WervalTestHelper.printErrorsTrace( app.errors() );
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
