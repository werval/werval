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

import io.werval.runtime.routes.RoutesProvider;
import io.werval.spi.ApplicationSPI;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Werval JUnit Rule.
 * <p>
 * Activate/Passivate Werval Application in test mode around JUnit tests.
 * <p>
 * Can be used to activate/passivate around each method test as a {@literal @}{@link org.junit.Rule}
 * or around each test class as a {@literal @}{@link org.junit.ClassRule}.
 * <p>
 * By default, configuration is loaded from the <code>application.conf</code> file and routes are loaded from the
 * <code>routes.conf</code> file.
 * <p>
 * Use the various constructors to to provide your own test configuration and routes.
 *
 * @composed 1 - 1 WervalTest
 */
public class WervalRule
    implements WervalTestSupport, TestRule
{
    private final WervalTest werval;

    public WervalRule()
    {
        this( null, null );
    }

    public WervalRule( String configurationResourceName )
    {
        this( configurationResourceName, null );
    }

    public WervalRule( RoutesProvider routesProvider )
    {
        this( null, routesProvider );
    }

    public WervalRule( String configurationResourceName, RoutesProvider routesProvider )
    {
        werval = new WervalTest( configurationResourceName, routesProvider );
    }

    @Override
    public Statement apply( final Statement statement, Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate()
                throws Throwable
            {
                werval.beforeEachTestMethod();
                try
                {
                    statement.evaluate();
                }
                finally
                {
                    werval.afterEachTestMethod();
                }
            }
        };
    }

    @Override
    public final ApplicationSPI application()
    {
        return werval.application();
    }

    @Override
    public RequestBuilder newRequestBuilder()
    {
        return werval.newRequestBuilder();
    }

    @Override
    public CookieBuilder newCookieBuilder()
    {
        return werval.newCookieBuilder();
    }
}
