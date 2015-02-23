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
package io.werval.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.werval.api.exceptions.PassivationException;
import io.werval.api.exceptions.WervalException;
import io.werval.runtime.exceptions.WervalRuntimeException;
import io.werval.runtime.util.NamedThreadFactory;
import io.werval.spi.ApplicationSPI;
import io.werval.spi.dev.DevShellSPI;
import io.werval.spi.server.HttpServerAdapter;
import io.werval.util.Reflectively;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.werval.api.Mode.PROD;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ACCEPTORS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ADDRESS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_IOTHREADS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_PORT;
import static io.werval.runtime.ConfigKeys.WERVAL_SHUTDOWN_QUIETPERIOD;
import static io.werval.runtime.ConfigKeys.WERVAL_SHUTDOWN_TIMEOUT;

/**
 * Netty HTTP Server.
 */
@Reflectively.Loaded( by = "DevShell" )
public class NettyServer
    extends HttpServerAdapter
{
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    @Reflectively.Invoked( by = "DevShell" )
    public NettyServer()
    {
        super();
        this.allChannels = new DefaultChannelGroup( "werval-netty-server", null );
    }

    public NettyServer( ApplicationSPI app )
    {
        this( app, null );
    }

    public NettyServer( ApplicationSPI app, DevShellSPI devSpi )
    {
        this();
        setApplicationSPI( app );
        setDevShellSPI( devSpi );
    }

    @Override
    protected void activateHttpServer()
    {
        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them.
        int acceptors = app.config().intOptional( WERVAL_HTTP_ACCEPTORS ).orElse( DEFAULT_POOL_SIZE );
        int iothreads = app.config().intOptional( WERVAL_HTTP_IOTHREADS ).orElse( DEFAULT_POOL_SIZE );
        bootstrap.group(
            new NioEventLoopGroup( app.mode() == PROD ? acceptors : 1, new NamedThreadFactory( "werval-acceptor" ) ),
            new NioEventLoopGroup( app.mode() == PROD ? iothreads : 1, new NamedThreadFactory( "werval-io" ) )
        );
        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, app, devSpi ) );

        // See http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( TCP_NODELAY, true );
        // See http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/
        bootstrap.option( SO_KEEPALIVE, true );

        // Bind
        String address = app.config().string( WERVAL_HTTP_ADDRESS );
        int port = app.config().intNumber( WERVAL_HTTP_PORT );
        try
        {
            bootstrap.localAddress( address, port );
            allChannels.add( bootstrap.bind().sync().channel() );
        }
        catch( InterruptedException ex )
        {
            throw new WervalRuntimeException( "Unable to bind to http(s)://" + address + ":" + port + "/ "
                                              + "Port already in use?", ex );
        }
    }

    @Override
    protected void passivateHttpServer()
    {
        if( bootstrap != null )
        {
            // app.config() can be null if activation failed, allow gracefull shutdown
            long shutdownQuietPeriod = app.config() == null
                                       ? 1000
                                       : app.config().milliseconds( WERVAL_SHUTDOWN_QUIETPERIOD );
            long shutdownTimeout = app.config() == null
                                   ? 5000
                                   : app.config().milliseconds( WERVAL_SHUTDOWN_TIMEOUT );

            // Record all passivation errors here to report them at once at the end
            List<Exception> passivationErrors = new ArrayList<>();

            // Shutdown IO Threads
            try
            {
                bootstrap.childGroup().shutdownGracefully(
                    shutdownQuietPeriod,
                    shutdownTimeout,
                    TimeUnit.MILLISECONDS
                ).syncUninterruptibly();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new WervalException( "Error while shutting down IO Threads: " + ex.getMessage(), ex )
                );
            }

            // Shutdown Accept Threads
            try
            {
                bootstrap.group().shutdownGracefully(
                    shutdownQuietPeriod,
                    shutdownTimeout,
                    TimeUnit.MILLISECONDS
                ).syncUninterruptibly();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new WervalException( "Error while shutting down Accept Threads: " + ex.getMessage(), ex )
                );
            }

            // Force close all channels
            try
            {
                if( !allChannels.isEmpty() )
                {
                    allChannels.close();
                }
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new WervalException( "Error while force-closing remaining open channels: " + ex.getMessage(), ex )
                );
            }
            finally
            {
                allChannels.clear();
            }

            // Report errors if any
            if( !passivationErrors.isEmpty() )
            {
                PassivationException ex = new PassivationException( "Errors during NettyServer passivation" );
                for( Exception passivationError : passivationErrors )
                {
                    ex.addSuppressed( passivationError );
                }
                throw ex;
            }
        }
    }
}
