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
package org.qiweb.spi.server;

import org.qiweb.api.util.Reflectively;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotNull;

/**
 * Base class to write HttpServer implementations.
 *
 * Primary responsibility is error handling across lifecycle.
 * <p>
 * See {@link HttpServerHelper} for composable helper methods to use in implementations.
 */
public abstract class HttpServerAdapter
    implements HttpServer
{
    private static final Logger LOG = LoggerFactory.getLogger( HttpServerAdapter.class );
    private final Thread shutdownHook;
    protected ApplicationSPI app;
    protected DevShellSPI devSpi;

    protected HttpServerAdapter()
    {
        this.shutdownHook = new Thread(
            new Runnable()
            {
                @Override
                public void run()
                {
                    passivate();
                }
            },
            "qiweb-server-shutdown"
        );
    }

    protected HttpServerAdapter( ApplicationSPI app, DevShellSPI devSpi )
    {
        this();
        setApplicationSPI( app );
        setDevShellSPI( devSpi );
    }

    @Override
    @Reflectively.Invoked( by = "DevShell" )
    public final void setApplicationSPI( ApplicationSPI application )
    {
        ensureNotNull( "ApplicationSPI", application );
        this.app = application;
    }

    @Override
    @Reflectively.Invoked( by = "DevShell" )
    public final void setDevShellSPI( DevShellSPI devSpi )
    {
        this.devSpi = devSpi;
    }

    @Override
    @Reflectively.Invoked( by = "DevShell" )
    // Fail-fast
    public final void activate()
    {
        // Activate Application
        app.activate();

        // Notify Global object that the HttpServer will start listening to network connections
        app.global().beforeHttpBind( app );

        // Activate HttpServer
        activateHttpServer();

        // Notify Global object that the HttpServer started listening to network connections
        app.global().afterHttpBind( app );
    }

    @Override
    @Reflectively.Invoked( by = "DevShell" )
    // TODO Fail-silent, log a single stacktrace with all exceptions as suppressed
    public final void passivate()
    {
        // Notify Global object that the HttpServer will stop listening to network connections
        try
        {
            app.global().beforeHttpUnbind( app );
        }
        catch( Exception ex )
        {
            LOG.error( "Exception on Global.beforeHttpUnbind(): {}", ex.getMessage(), ex );
        }

        // Passivate HttpServer
        try
        {
            passivateHttpServer();
            LOG.debug( "Http Service Passivated" );
        }
        catch( Exception ex )
        {
            LOG.error( "Exception on HttpServer.passivate(): {}", ex.getMessage(), ex );
        }

        // Notify Global object that the HttpServer stopped listening to network connections
        try
        {
            app.global().afterHttpUnbind( app );
        }
        catch( Exception ex )
        {
            LOG.error( "Exception on Global.afterHttpUnbind(): {}", ex.getMessage(), ex );
        }

        // Passivate Application
        try
        {
            app.passivate();
        }
        catch( Exception ex )
        {
            LOG.error( "Exception on Application.passivate(): {}", ex.getMessage(), ex );
        }
    }

    /**
     * Override this method and activate your HttpServer implementation in it.
     */
    protected void activateHttpServer()
    {
        throw new UnsupportedOperationException( "Override activateHttpServer()!" );
    }

    /**
     * Override this method and passivate your HttpServer implementation in it.
     */
    protected void passivateHttpServer()
    {
        throw new UnsupportedOperationException( "Override passivateHttpServer()!" );
    }

    @Override
    @Reflectively.Invoked( by = "DevShell" )
    public final void registerPassivationShutdownHook()
    {
        try
        {
            Runtime.getRuntime().addShutdownHook( shutdownHook );
        }
        catch( IllegalArgumentException ex )
        {
            throw new IllegalStateException( "HttpServer passivation hook previously registered", ex );
        }
    }
}
