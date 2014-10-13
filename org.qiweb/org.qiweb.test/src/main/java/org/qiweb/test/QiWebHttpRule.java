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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.spi.ApplicationSPI;

/**
 * QiWeb HTTP JUnit Rule.
 * <p>
 * Activate/Passivate QiWeb Application and HTTP Server in test mode around JUnit tests.
 * <p>
 * Can be used to activate/passivate around each method test as a {@literal @}{@link Rule} or around each test class
 * as a {@literal @}{@link ClassRule}.
 * <p>
 * By default, configuration is loaded from the <code>application.conf</code> file and routes are loaded from the
 * <code>routes.conf</code> file.
 * <p>
 * Use the various constructors to to provide your own test configuration and routes.
 */
public class QiWebHttpRule
    implements QiWebHttpTestSupport, TestRule
{
    private final QiWebHttpTest qiweb;

    public QiWebHttpRule()
    {
        this( null, null );
    }

    public QiWebHttpRule( String configurationResourceName )
    {

        this( configurationResourceName, null );
    }

    public QiWebHttpRule( RoutesProvider routesProvider )
    {
        this( null, routesProvider );
    }

    public QiWebHttpRule( String configurationResourceName, RoutesProvider routesProvider )
    {
        qiweb = new QiWebHttpTest( configurationResourceName, routesProvider );
    }

    @Override
    public final ApplicationSPI application()
    {
        return qiweb.application();
    }

    @Override
    public final String httpHost()
    {
        return qiweb.httpHost();
    }

    @Override
    public final int httpPort()
    {
        return qiweb.httpPort();
    }

    @Override
    public final String baseHttpUrl()
    {
        return qiweb.baseHttpUrl();
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
                qiweb.beforeEachQiWebTestMethod();
                try
                {
                    statement.evaluate();
                }
                finally
                {
                    qiweb.afterEachQiWebTestMethod();
                }
            }
        };
    }
}
