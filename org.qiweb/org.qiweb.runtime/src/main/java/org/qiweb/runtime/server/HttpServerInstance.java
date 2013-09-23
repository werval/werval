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
package org.qiweb.runtime.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.TimeUnit;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;
import org.qiweb.spi.dev.DevShellSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ACCEPTORS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_IOTHREADS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_QUIETPERIOD;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_TIMEOUT;

public class HttpServerInstance
    implements HttpServer
{

    private static final class ShutdownHook
        implements Runnable
    {

        private final HttpServer server;

        private ShutdownHook( HttpServer server )
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            server.passivate();
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger( HttpServerInstance.class );
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
    private final String identity;
    private final ApplicationInstance app;
    private final DevShellSPI devSpi;
    private final ChannelGroup allChannels;
    private final Thread shutdownHook;
    private ServerBootstrap bootstrap;

    public HttpServerInstance( String identity, ApplicationInstance app )
    {
        this( identity, app, null );
    }

    public HttpServerInstance( String identity, ApplicationInstance app, DevShellSPI devSpi )
    {
        this.identity = identity;
        this.app = app;
        this.devSpi = devSpi;
        this.allChannels = new DefaultChannelGroup( identity, null );
        this.shutdownHook = new Thread( new ShutdownHook( this ), "qiweb-shutdown" );
    }

    @Override
    public void activate()
    {
        app.global().beforeHttpBind( app );

        // Netty Bootstrap
        bootstrap = new ServerBootstrap();

        // I/O Event Loops.
        // The first is used to handle the accept of new connections and the second will serve the IO of them.
        int acceptors = app.config().has( QIWEB_HTTP_ACCEPTORS )
                        ? app.config().intNumber( QIWEB_HTTP_ACCEPTORS )
                        : DEFAULT_POOL_SIZE;
        int iothreads = app.config().has( QIWEB_HTTP_IOTHREADS )
                        ? app.config().intNumber( QIWEB_HTTP_IOTHREADS )
                        : DEFAULT_POOL_SIZE;
        bootstrap.group( new NioEventLoopGroup( devSpi == null ? acceptors : 1 ),
                         new NioEventLoopGroup( devSpi == null ? iothreads : 1 ) );

        // Server Channel
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.childHandler( new HttpServerChannelInitializer( allChannels, app, devSpi ) );

        // See http://www.unixguide.net/network/socketfaq/2.16.shtml
        bootstrap.option( TCP_NODELAY, true );
        // See http://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/
        bootstrap.option( SO_KEEPALIVE, true );

        // Bind
        String address = app.config().string( QIWEB_HTTP_ADDRESS );
        int port = app.config().intNumber( QIWEB_HTTP_PORT );
        try
        {
            bootstrap.localAddress( address, port );
            allChannels.add( bootstrap.bind().sync().channel() );
        }
        catch( InterruptedException ex )
        {
            throw new QiWebRuntimeException( "Unable to bind to http://" + address + ":" + port + "/ "
                                             + "Port already in use?", ex );
        }

        LOG.debug( "[{}] Http Service Listening on http(s)://{}:{}", identity, address, port );

        app.global().afterHttpBind( app );
    }

    @Override
    public void passivate()
    {
        app.global().beforeHttpUnbind( app );

        Future<?> shutdownFuture = bootstrap.group().shutdownGracefully(
            app.config().milliseconds( QIWEB_SHUTDOWN_QUIETPERIOD ),
            app.config().milliseconds( QIWEB_SHUTDOWN_TIMEOUT ),
            TimeUnit.MILLISECONDS );
        shutdownFuture.addListener( new GenericFutureListener<Future<Object>>()
        {
            @Override
            public void operationComplete( Future<Object> future )
                throws Exception
            {
                allChannels.clear();

                LOG.debug( "[{}] Http Service Passivated", identity );

                app.global().afterHttpUnbind( app );
                app.global().onStop( app );
            }
        } );
        shutdownFuture.awaitUninterruptibly();
    }

    @Override
    public void registerPassivationShutdownHook()
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
