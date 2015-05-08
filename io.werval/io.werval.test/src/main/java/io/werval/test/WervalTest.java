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
package io.werval.test;

import io.werval.api.Mode;
import io.werval.runtime.ApplicationInstance;
import io.werval.runtime.ConfigInstance;
import io.werval.runtime.ConfigKeys;
import io.werval.runtime.CryptoInstance;
import io.werval.runtime.routes.RoutesConfProvider;
import io.werval.runtime.routes.RoutesProvider;
import io.werval.spi.ApplicationSPI;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonMap;

/**
 * Base Werval JUnit Test.
 * <p>
 * Activate/Passivate Werval Application in test mode around each JUnit test method.
 * <p>
 * By default, configuration is loaded from the <code>application.conf</code> file.
 * Override the {@link #configurationResourceName()} method to provide your own test configuration.
 * <p>
 * By default, routes are loaded from the <code>routes.conf</code> file.
 * Override the {@link #routesProvider()} method to provide your own test routes.
 *
 * @navcomposed 1 - 1 ApplicationSPI
 */
public class WervalTest
    implements WervalTestSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( WervalTest.class );
    private final String configurationResourceNameOverride;
    private final RoutesProvider routesProviderOverride;
    private ApplicationSPI app;

    public WervalTest()
    {
        this( null, null );
    }

    /* package */ WervalTest( String configurationResourceNameOverride, RoutesProvider routesProviderOverride )
    {
        this.configurationResourceNameOverride = configurationResourceNameOverride;
        this.routesProviderOverride = routesProviderOverride;
    }

    /**
     * Activate Application.
     */
    @Before
    public final void beforeEachTestMethod()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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
        app.activate();
    }

    /**
     * Passivate Application.
     */
    @After
    public final void afterEachTestMethod()
    {
        WervalTestHelper.printErrorsTrace( app.errors() );
        app.passivate();
        app = null;
    }

    @Override
    public final ApplicationSPI application()
    {
        return app;
    }

    @Override
    public RequestBuilder newRequestBuilder()
    {
        return app.httpBuilders().newRequestBuilder();
    }

    @Override
    public CookieBuilder newCookieBuilder()
    {
        return app.httpBuilders().newCookieBuilder();
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
