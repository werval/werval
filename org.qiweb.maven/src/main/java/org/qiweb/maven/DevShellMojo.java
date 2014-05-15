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
package org.qiweb.maven;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.qiweb.commands.DevShellCommand;
import org.qiweb.devshell.JavaWatcher;
import org.qiweb.spi.dev.DevShellSPI;

import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

/**
 * Development Shell Mojo.
 */
@Mojo( name = "devshell", requiresDependencyResolution = RUNTIME, threadSafe = true )
@Execute( phase = COMPILE )
public class DevShellMojo
    extends AbstractQiWebMojo
{
    @Parameter( defaultValue = "compile" )
    private String rebuildPhase;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> QiWeb DevShell for " + project.getArtifactId() + " starting..." );

        try
        {
            // Classpath
            URL[] runtimeClassPath = runtimeClassPath();

            // Sources
            Set<File> sources = new LinkedHashSet<>();
            for( String sourceRoot : project.getCompileSourceRoots() )
            {
                sources.add( new File( sourceRoot ) );
            }

            File rootDir = project.getBasedir();
            URL[] applicationClasspath = new URL[]
            {
                new File( rootDir, "target/classes" ).toURI().toURL()
            };

            // Start DevShell
            DevShellSPI devShellSPI = new MavenDevShellSPI(
                applicationClasspath, runtimeClassPath,
                sources,
                new JavaWatcher(),
                rootDir,
                rebuildPhase
            );
            new DevShellCommand( devShellSPI ).run();
        }
        catch( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
