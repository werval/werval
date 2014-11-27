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
package org.qiweb.commands;

import java.io.File;
import java.net.URL;
import org.qiweb.devshell.DevShell;
import org.qiweb.spi.dev.DevShellSPI;

/**
 * DevShell Command.
 */
public class DevShellCommand
    implements Runnable
{
    private final DevShellSPI spi;
    private final String configResource;
    private final File configFile;
    private final URL configUrl;
    private final boolean openBrowser;

    public DevShellCommand( DevShellSPI spi )
    {
        this( spi, null, null, null, true );
    }

    public DevShellCommand( DevShellSPI spi, String configResource )
    {
        this( spi, configResource, null, null, true );
    }

    public DevShellCommand( DevShellSPI spi, File configFile )
    {
        this( spi, null, configFile, null, true );
    }

    public DevShellCommand( DevShellSPI spi, URL configUrl )
    {
        this( spi, null, null, configUrl, true );
    }

    public DevShellCommand(
        DevShellSPI spi,
        String configResource, File configFile, URL configUrl,
        boolean openBrowser
    )
    {
        this.spi = spi;
        this.configResource = configResource;
        this.configFile = configFile;
        this.configUrl = configUrl;
        this.openBrowser = openBrowser;
    }

    @Override
    public void run()
    {
        DevShell devShell = new DevShell( spi, configResource, configFile, configUrl, openBrowser );
        Runtime.getRuntime().addShutdownHook( new Thread( () -> devShell.stop(), "qiweb-devshell-shutdown" ) );
        devShell.start();
    }
}
