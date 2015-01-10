/*
 * Copyright (c) 2010-2014 the original author or authors
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
package io.werval.test.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * Free port finder.
 */
public final class FreePortFinder
{
    public static boolean isFree( int port )
    {
        return isFree( null, port );
    }

    public static boolean isFreeOnLoopback( int port )
    {
        return isFree( loopback(), port );
    }

    public static boolean isFreeOnAllInterfaces( int port )
    {
        return isFree( allInterfaces(), port );
    }

    public static boolean isFree( InetAddress address, int port )
    {
        try
        {
            new ServerSocket( port, 1, address ).close();
            return true;
        }
        catch( IOException ex )
        {
            return false;
        }
    }

    public static int findRandom()
    {
        return findRandomOnInterface( null );
    }

    public static int findRandomOnLoopback()
    {
        return findRandomOnInterface( loopback() );
    }

    public static int findRandomOnAllInterfaces()
    {
        return findRandomOnInterface( allInterfaces() );
    }

    public static int findWithPreference( int prefered )
    {
        return findOnInterfaceWithPreference( null, prefered );
    }

    public static int findWithPreferenceOnLoopback( int prefered )
    {
        return findOnInterfaceWithPreference( loopback(), prefered );
    }

    public static int findWithPreferenceOnAllInterfaces( int prefered )
    {
        return findOnInterfaceWithPreference( allInterfaces(), prefered );
    }

    public static int findRandomOnInterface( InetAddress address )
    {
        return findOnInterfaceWithPreference( address, -1 );
    }

    public static int findRandomOnInterfaceByName( String host )
    {
        try
        {
            return findOnInterfaceWithPreference( InetAddress.getByName( host ), -1 );
        }
        catch( UnknownHostException ex )
        {
            throw new UncheckedIOException( "Unable to find free port: " + ex.getMessage(), ex );
        }
    }

    public static int findOnInterfaceWithPreference( InetAddress address, int prefered )
    {
        try
        {
            ServerSocket server;
            if( prefered > 0 )
            {
                server = new ServerSocket( prefered, 1, address );
            }
            else
            {
                server = new ServerSocket( 0, 1, address );
            }
            int port = server.getLocalPort();
            server.close();
            return port;
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( "Unable to find free port: " + ex.getMessage(), ex );
        }
    }

    private static InetAddress loopback()
    {
        // 127.0.0.1
        return InetAddress.getLoopbackAddress();
    }

    private static InetAddress allInterfaces()
    {
        // 0.0.0.0
        return new InetSocketAddress( 0 ).getAddress();
    }

    private FreePortFinder()
    {
    }
}
