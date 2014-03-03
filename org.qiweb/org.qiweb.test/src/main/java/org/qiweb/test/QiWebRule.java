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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.qiweb.api.Application;
import org.qiweb.runtime.routes.RoutesProvider;

/**
 * QiWeb JUnit Rule.
 *
 * <p>Activate/Passivate QiWeb Application in test mode around JUnit tests.</p>
 * <p>
 *     Can be used to activate/passivate around each method test as a {@literal @}{@link Rule} or around each test class
 *     as a {@literal @}{@link ClassRule}.
 * </p>
 * <p>
 *     By default, configuration is loaded from the <code>application.conf</code> file.
 *     Use {@link #QiWebRule(java.lang.String)} or
 *     {@link #QiWebRule(java.lang.String, org.qiweb.runtime.routes.RoutesProvider)} constructor to to provide your own
 *     test configuration.
 * </p>
 * <p>
 *     By default, routes are loaded from the <code>routes.conf</code> file.
 *     Use {@link #QiWebRule(org.qiweb.runtime.routes.RoutesProvider)} or
 *     {@link #QiWebRule(java.lang.String, org.qiweb.runtime.routes.RoutesProvider) } constructor to provide your own
 *     test routes.
 * </p>
 */
public class QiWebRule
    implements QiWebTestSupport, TestRule
{
    private final QiWebTest qiweb;

    public QiWebRule()
    {
        this( null, null );
    }

    public QiWebRule( String configurationResourceName )
    {

        this( configurationResourceName, null );
    }

    public QiWebRule( RoutesProvider routesProvider )
    {
        this( null, routesProvider );
    }

    public QiWebRule( String configurationResourceName, RoutesProvider routesProvider )
    {
        qiweb = new QiWebTest( configurationResourceName, routesProvider );
    }

    @Override
    public final Application application()
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
