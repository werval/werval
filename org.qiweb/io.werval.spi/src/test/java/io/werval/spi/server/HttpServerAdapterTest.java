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
package io.werval.spi.server;

import io.werval.api.Mode;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.test.util.Slf4jRule;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Assert HttpServerAdapter error handling.
 */
public class HttpServerAdapterTest
{
    static class ActivationErrorHttpServer
        extends HttpServerAdapter
    {
        @Override
        protected void activateHttpServer()
        {
            throw new RuntimeException( "activateHttpServer" );
        }

        @Override
        protected void passivateHttpServer()
        {
        }
    }

    static class PassivationErrorHttpServer
        extends HttpServerAdapter
    {
        @Override
        protected void activateHttpServer()
        {
        }

        @Override
        protected void passivateHttpServer()
        {
            throw new RuntimeException( "passivateHttpServer" );
        }
    }

    @Rule
    public Slf4jRule slf4j = new Slf4jRule()
    {
        {
            record( Slf4jRule.Level.ERROR );
            recordForType( HttpServerAdapter.class );
        }
    };

    @Test
    public void testActivationError()
    {
        try
        {
            HttpServer server = new ActivationErrorHttpServer()
            {
                {
                    setApplicationSPI( new ApplicationInstance( Mode.TEST ) );
                }
            };
            server.activate();
            fail( "HttpServer should have failed" );
        }
        catch( Exception ex )
        {
            assertThat( ex, instanceOf( RuntimeException.class ) );
            assertThat( ex.getMessage(), containsString( "activateHttpServer" ) );
        }
    }

    @Test
    public void testPassivationError()
    {
        HttpServer server = new PassivationErrorHttpServer()
        {
            {
                setApplicationSPI( new ApplicationInstance( Mode.TEST ) );
            }
        };
        server.activate();
        server.passivate();
        assertThat( slf4j.size(), greaterThan( 0 ) );
        assertTrue( "Passivation error logged", slf4j.contains( "There were errors during passivation" ) );
        assertTrue( "Specific exception in stacktrace", slf4j.containsExMessage( "passivateHttpServer" ) );
    }
}
