/*
 * Copyright (c) 2013 the original author or authors.
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
package org.qiweb.maven;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.spi.dev.DevShellSPIAdapter;

public class MavenDevShellSPI
    extends DevShellSPIAdapter
{
    private final DefaultExecutor executor;
    private final CommandLine cmdLine;

    public MavenDevShellSPI(
        URL[] applicationClassPath,
        URL[] runtimeClassPath,
        Set<File> sources,
        SourceWatcher watcher,
        File rootDir,
        String rebuildPhase
    )
    {
        super( applicationClassPath, runtimeClassPath, sources, watcher, false );
        executor = new DefaultExecutor();
        cmdLine = new CommandLine( "mvn" );
        cmdLine.addArgument( "-f" );
        cmdLine.addArgument( new File( rootDir, "pom.xml" ).getAbsolutePath() );
        cmdLine.addArgument( rebuildPhase );
    }

    @Override
    protected void doRebuild()
    {
        try
        {
            int exitValue = executor.execute( cmdLine );
            if( exitValue != 0 )
            {
                System.out.println( "Maven Rebuild exited with a non-zero status: " + exitValue );
            }
        }
        catch( Exception ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
    }
}
