/*
 * Copyright 2013 Paul Merlin.
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
import org.qiweb.spi.dev.DevShellSPIAdapter;
import org.qiweb.spi.dev.Watcher;

public class MavenDevShellSPI
    extends DevShellSPIAdapter
{

    public MavenDevShellSPI(
        String name, File rootDir, File buildDir,
        Set<File> mainSources, File mainOutput, URL[] mainClassPath,
        Set<File> testSources, File testOutput, URL[] testClassPath,
        Watcher watcher )
    {
        super( name, rootDir, buildDir,
               mainSources, mainOutput, mainClassPath,
               testSources, testOutput, testClassPath,
               watcher );
    }

    @Override
    protected void doRebuildMain()
    {
        System.out.println( "-------------------------------------------------------" );
        System.out.println( "REBUILD MAIN" );
        try
        {
            CommandLine cmdLine = new CommandLine( "mvn" );
            cmdLine.addArgument( "-f" );
            cmdLine.addArgument( new File( rootDir(), "pom.xml" ).getAbsolutePath() );
            // cmdLine.addArgument( "clean");
            cmdLine.addArgument( "compile" );
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute( cmdLine );
        }
        catch( Exception ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }

        System.out.println( "-------------------------------------------------------" );
    }
}
