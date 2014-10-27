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
package org.qiweb.server.bootstrap;

import org.qiweb.api.Mode;
import org.qiweb.runtime.ApplicationInstance;
import org.qiweb.server.netty.NettyServer;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.server.HttpServer;

/**
 * QiWeb HTTP Server Bootstrap Main Class.
 */
public final class Main
{
    public static void main( String[] args )
        throws Exception
    {
        ApplicationSPI app = new ApplicationInstance( Mode.PROD );
        HttpServer server = new NettyServer( app );
        server.registerPassivationShutdownHook();
        server.activate();
    }

    private Main()
    {
    }
}
