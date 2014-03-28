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
package org.qiweb.test;

import org.junit.After;
import org.junit.Before;
import org.qiweb.api.Config;
import org.qiweb.api.Mode;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.ConfigInstance;
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.spi.ApplicationSPI;

/**
 * Base QiWeb JUnit Test.
 *
 * Activate/Passivate QiWeb Application in test mode around each JUnit test method.
 * <p>
 * By default, configuration is loaded from the <code>application.conf</code> file.
 * Override the {@link #configurationResourceName()} method to provide your own test configuration.
 * <p>
 * By default, routes are loaded from the <code>routes.conf</code> file.
 * Override the {@link #routesProvider()} method to provide your own test routes.
 */
public class QiWebTest
    implements QiWebTestSupport
{
    private final String configurationResourceNameOverride;
    private final RoutesProvider routesProviderOverride;
    private ApplicationSPI app;

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
     * Activate Application.
     */
    @Before
    public final void beforeEachQiWebTestMethod()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        String conf = configurationResourceNameOverride == null
                      ? configurationResourceName()
                      : configurationResourceNameOverride;
        Config config = new ConfigInstance( classLoader, conf );
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
    public final void afterEachQiWebTestMethod()
    {
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
