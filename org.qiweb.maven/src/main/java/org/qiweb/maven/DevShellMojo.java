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
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.qiweb.devshell.DevShell;
import org.qiweb.devshell.JNotifyWatcher;
import org.qiweb.spi.dev.DevShellSPI;

/**
 * @goal devshell
 * @requiresDependencyResolution runtime
 */
public class DevShellMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> QiWeb DevShell for " + project.getArtifactId() + " starting..." );

        try
        {
            String projectName = project.getArtifactId();
            File rootDir = project.getBasedir();
            File buildDir = new File( rootDir, "target" );

            Set<File> mainSources = new LinkedHashSet<File>();
            for( String compileSourceRoot : project.getCompileSourceRoots() )
            {
                mainSources.add( new File( compileSourceRoot ) );
            }
            File mainOutput = new File( buildDir, "classes" );
            Set<URL> mainClassPathSet = new LinkedHashSet<URL>();
            mainClassPathSet.add( mainOutput.toURI().toURL() );
            for( String compileClassPathElement : project.getCompileClasspathElements() )
            {
                mainClassPathSet.add( new URL( "file://" + compileClassPathElement ) );
            }
            URL[] mainClassPath = mainClassPathSet.toArray( new URL[ mainClassPathSet.size() ] );

            Set<File> testSources = new LinkedHashSet<File>();
            for( String compileTestSourceRoot : project.getTestCompileSourceRoots() )
            {
                testSources.add( new File( compileTestSourceRoot ) );
            }
            File testOutput = new File( buildDir, "test/classes" );
            Set<URL> testClassPathSet = new LinkedHashSet<URL>();
            testClassPathSet.add( testOutput.toURI().toURL() );
            for( String testClassPathElement : project.getTestClasspathElements() )
            {
                testClassPathSet.add( new URL( "file://" + testClassPathElement ) );
            }
            URL[] testClassPath = testClassPathSet.toArray( new URL[ testClassPathSet.size() ] );

            // Deploy JNotify            
            JNotifyWatcher.deployNativeLibraries( buildDir );

            DevShellSPI devSPI = new MavenDevShellSPI( projectName, rootDir, buildDir,
                                                       mainSources, mainOutput, mainClassPath,
                                                       testSources, testOutput, testClassPath,
                                                       new JNotifyWatcher() );

            final DevShell devShell = new DevShell( devSPI );

            Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    devShell.stop();
                }
            }, "Maven DevShell Shutdown Hook Thread" ) );

            devShell.start();
        }
        catch( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
