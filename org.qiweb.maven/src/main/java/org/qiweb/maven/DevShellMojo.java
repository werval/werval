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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.qiweb.commands.DevShellCommand;
import org.qiweb.devshell.JavaWatcher;
import org.qiweb.spi.dev.DevShellSPI;

import static java.util.Collections.EMPTY_SET;
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

    @Parameter( property = "qiwebdev.extraWatch" )
    private File[] extraWatch;

    @Parameter( property = "plugin.artifacts", required = true, readonly = true )
    private List<Artifact> pluginArtifacts;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> QiWeb DevShell for " + project.getArtifactId() + " starting..." );

        try
        {
            // Application Sources
            URL[] applicationSources = applicationSources();

            // Application Classpath
            Set<URL> appCP = qiwebDocArtifacts();
            appCP.add( new File( project.getBasedir(), "target/classes" ).toURI().toURL() );
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
                toWatch.addAll( Arrays.asList( extraWatch ) );
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
            new DevShellCommand( devShellSPI ).run();
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
        project.getCompileSourceRoots();
        return sourcesSet.toArray( new URL[ sourcesSet.size() ] );
    }

    protected Set<URL> qiwebDocArtifacts()
        throws MalformedURLException
    {
        Artifact qiwebDocArtifact = null;
        Artifact sitemeshArtifact = null;
        for( Artifact pluginArtifact : pluginArtifacts )
        {
            if( "org.qiweb".equals( pluginArtifact.getGroupId() )
                && "org.qiweb.doc".equals( pluginArtifact.getArtifactId() ) )
            {
                qiwebDocArtifact = pluginArtifact;
            }
            else if( "org.sitemesh".equals( pluginArtifact.getGroupId() )
                     && "sitemesh".equals( pluginArtifact.getArtifactId() ) )
            {
                sitemeshArtifact = pluginArtifact;
            }
        }

        if( qiwebDocArtifact == null || sitemeshArtifact == null )
        {
            getLog().warn(
                "QiWeb Documentation not in the Maven Plugin Classpath, please report the issue: "
                + "https://scm.codeartisans.org/qiweb/qiweb/issues/new"
            );
            return EMPTY_SET;
        }

        Set<URL> result = new LinkedHashSet<>();
        result.add( qiwebDocArtifact.getFile().toURI().toURL() );
        result.add( sitemeshArtifact.getFile().toURI().toURL() );
        return result;
    }
}
