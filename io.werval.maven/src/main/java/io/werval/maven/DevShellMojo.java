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

import io.werval.commands.DevShellCommand;
import io.werval.devshell.JavaWatcher;
import io.werval.spi.dev.DevShellSPI;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static java.util.Collections.EMPTY_SET;
import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

/**
 * Run the application in development mode.
 */
@Mojo( name = "devshell", requiresDependencyResolution = RUNTIME, threadSafe = true )
@Execute( phase = COMPILE )
public class DevShellMojo
    extends AbstractRunGoal
{
    /**
     * Rebuild phase.
     */
    @Parameter( defaultValue = "compile" )
    private String rebuildPhase;

    /**
     * Extra files or directories paths to watch for changes, relative to the project base directory.
     */
    @Parameter( property = "wervaldev.extraWatch" )
    private String[] extraWatch;

    /**
     * Open default browser on start.
     */
    @Parameter( property = "wervaldev.openBrowser", defaultValue = "true" )
    private boolean openBrowser;

    @Parameter( property = "plugin.artifacts", required = true, readonly = true )
    private List<Artifact> pluginArtifacts;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> Werval DevShell for " + project.getArtifactId() + " starting..." );

        try
        {
            // Application Sources
            URL[] applicationSources = applicationSources();

            // Application Classpath
            Set<URL> appCP = wervalDocArtifacts();
            appCP.add( new File( project.getBasedir(), "target/classes" ).toURI().toURL() );
            if( extraClassPath != null )
            {
                for( String extraCP : extraClassPath )
                {
                    appCP.add( new File( project.getBasedir(), extraCP ).toURI().toURL() );
                }
            }
            URL[] applicationClasspath = appCP.toArray( new URL[ appCP.size() ] );

            // Runtime Classpath
            URL[] runtimeClassPath = runtimeClassPath();

            // To watch for changes
            Set<File> toWatch = new LinkedHashSet<>();
            for( String eachToWatch : project.getCompileSourceRoots() )
            {
                toWatch.add( new File( eachToWatch ) );
            }
            if( extraWatch != null )
            {
                for( String extraW : extraWatch )
                {
                    toWatch.add( new File( project.getBasedir(), extraW ) );
                }
            }

            // Start DevShell
            DevShellSPI devShellSPI = new MavenDevShellSPI(
                applicationSources,
                applicationClasspath, runtimeClassPath,
                toWatch,
                new JavaWatcher(),
                project.getBasedir(),
                rebuildPhase
            );
            new DevShellCommand( devShellSPI, configResource, configFile, configUrl, openBrowser ).run();
        }
        catch( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }

    protected URL[] applicationSources()
        throws MalformedURLException
    {
        Set<URL> sourcesSet = new LinkedHashSet<>();
        for( String applicationSourcesElement : project.getCompileSourceRoots() )
        {
            sourcesSet.add( new File( applicationSourcesElement ).toURI().toURL() );
        }
        return sourcesSet.toArray( new URL[ sourcesSet.size() ] );
    }

    protected Set<URL> wervalDocArtifacts()
        throws MalformedURLException
    {
        Artifact wervalDocArtifact = null;
        Artifact sitemeshArtifact = null;
        for( Artifact pluginArtifact : pluginArtifacts )
        {
            if( "io.werval".equals( pluginArtifact.getGroupId() )
                && "io.werval.doc".equals( pluginArtifact.getArtifactId() ) )
            {
                wervalDocArtifact = pluginArtifact;
            }
            else if( "org.sitemesh".equals( pluginArtifact.getGroupId() )
                     && "sitemesh".equals( pluginArtifact.getArtifactId() ) )
            {
                sitemeshArtifact = pluginArtifact;
            }
        }

        if( wervalDocArtifact == null || sitemeshArtifact == null )
        {
            getLog().warn(
                "Werval Documentation not in the Maven Plugin Classpath, please report the issue: "
                + "https://github.com/werval/werval/issues/new"
            );
            return EMPTY_SET;
        }

        Set<URL> result = new LinkedHashSet<>();
        result.add( wervalDocArtifact.getFile().toURI().toURL() );
        result.add( sitemeshArtifact.getFile().toURI().toURL() );
        return result;
    }
}
