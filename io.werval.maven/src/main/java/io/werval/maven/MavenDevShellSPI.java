/*
 * Copyright (c) 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.werval.maven;

import io.werval.spi.dev.DevShellRebuildException;
import io.werval.spi.dev.DevShellSPI.SourceWatcher;
import io.werval.spi.dev.DevShellSPIAdapter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Set;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.MavenExecutionException;

/**
 * Maven DevShellSPI implementation.
 */
public class MavenDevShellSPI
    extends DevShellSPIAdapter
{
    private final File pom;
    private final CommandLine cmdLine;

    public MavenDevShellSPI(
        URL[] applicationSources,
        URL[] applicationClassPath,
        URL[] runtimeClassPath,
        Set<File> sources,
        SourceWatcher watcher,
        File rootDir,
        String rebuildPhase
    )
    {
        super( applicationSources, applicationClassPath, runtimeClassPath, sources, watcher, false );
        pom = new File( rootDir, "pom.xml" );
        cmdLine = new CommandLine( "mvn" );
        cmdLine.addArgument( "-f" );
        cmdLine.addArgument( pom.getAbsolutePath() );
        cmdLine.addArgument( rebuildPhase );
    }

    @Override
    protected void doRebuild()
        throws DevShellRebuildException
    {
        System.out.println( "Reload!" );
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        executor.setStreamHandler( new PumpStreamHandler( output ) );
        try
        {
            int exitValue = executor.execute( cmdLine );
            if( exitValue != 0 )
            {
                throw new DevShellRebuildException(
                    new MavenExecutionException( "Maven exited with a non-zero status: " + exitValue, pom )
                );
            }
        }
        catch( Exception ex )
        {
            throw new DevShellRebuildException( ex, output.toString() );
        }
    }
}
