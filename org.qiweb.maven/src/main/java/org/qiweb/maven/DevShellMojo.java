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
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.qiweb.devshell.DevShell;
import org.qiweb.devshell.JavaWatcher;

/**
 * @goal devshell
 * @requiresDependencyResolution runtime
 */
public class DevShellMojo
    extends AbstractMojo
{

    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * @parameter default-value="compile"
     */
    private String rebuildPhase;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> QiWeb DevShell for " + project.getArtifactId() + " starting..." );

        try
        {
            File rootDir = project.getBasedir();

            // Classpath
            Set<URL> classPathSet = new LinkedHashSet<URL>();
            for( String runtimeClassPathElement : project.getRuntimeClasspathElements() )
            {
                classPathSet.add( new URL( "file://" + runtimeClassPathElement ) );
            }
            URL[] runtimeClassPath = classPathSet.toArray( new URL[ classPathSet.size() ] );

            // Sources
            Set<File> sources = new LinkedHashSet<File>();
            for( String sourceRoot : project.getCompileSourceRoots() )
            {
                sources.add( new File( sourceRoot ) );
            }

            // Run DevShell
            URL[] applicationClasspath = new URL[]
            {
                new File( rootDir, "target/classes" ).toURI().toURL()
            };
            final DevShell devShell = new DevShell( new MavenDevShellSPI( applicationClasspath, runtimeClassPath,
                                                                          sources, new JavaWatcher(),
                                                                          rootDir, rebuildPhase ) );

            Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    devShell.stop();
                }
            }, "qiweb-devshell-shutdown" ) );

            devShell.start();
        }
        catch( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
