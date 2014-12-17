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
package io.werval.server.bootstrap;

import io.werval.spi.ApplicationSPI;
import io.werval.spi.server.HttpServer;
import java.util.List;
import java.util.ServiceLoader;

import static io.werval.util.Iterables.toList;

/**
 * Werval HTTP Server Bootstrap Main Class.
 * <p>
 * Use {@link ServiceLoader} to load {@link ApplicationSPI} and {@link HttpServer} implementations.
 */
public final class Main
{
    public static void main( String[] args )
        throws Exception
    {
        ApplicationSPI app = load( ApplicationSPI.class );
        HttpServer server = load( HttpServer.class );
        server.setApplicationSPI( app );
        server.registerPassivationShutdownHook();
        server.activate();
    }

    private static <T> T load( Class<T> type )
    {
        List<T> impls = toList( ServiceLoader.load( type, Main.class.getClassLoader() ) );
        if( impls.isEmpty() )
        {
            throw new IllegalStateException(
                String.format( "No %s implementations found on the classpath.", type )
            );
        }
        if( impls.size() > 1 )
        {
            throw new IllegalStateException(
                String.format( "Multiple %s implementations found on the classpath: %s", type, impls )
            );
        }
        return impls.get( 0 );
    }

    private Main()
    {
    }
}
