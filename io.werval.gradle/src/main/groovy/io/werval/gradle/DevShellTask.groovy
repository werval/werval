/**
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
package io.werval.gradle

import io.werval.commands.DevShellCommand
import io.werval.devshell.JavaWatcher
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

class DevShellTask extends DefaultTask
{
    /**
     * Application source sets, default to {@literal main} and {@literal dev}.
     */
    Set<SourceSet> sourceSets = new LinkedHashSet<>( [ project.sourceSets.main, project.sourceSets.dev ] )

    /**
     * Configuration resource name.
     * <p>
     * Loaded from the application classpath.
     */
    String configResource

    /**
     * Configuration file.
     */
    File configFile

    /**
     * Configuration URL.
     */
    URL configUrl

    /**
     * Paths of extra files or directories to watch for changes.
     */
    Set<String> extraWatch = new LinkedHashSet<>()

    /**
     * Open default browser on start.
     */
    boolean openBrowser = true;

    @TaskAction
    void runDevShell()
    {
        project.logger.lifecycle ">> Werval DevShell for " + project.getName() + " starting..."

        // Default behaviour
        if( extraWatch == null )
        {
            extraWatch = new LinkedHashSet<>()
        }
        if( sourceSets == null )
        {
            sourceSets = new LinkedHashSet<>( [ project.sourceSets.main ] )
        }

        // == Gather build info
        def applicationSources = sourceSets.collect { sourceSet ->
            sourceSet.allSource.srcDirs.collect { f -> f.toURI().toURL() }
        }.flatten()
        def applicationClasspath = sourceSets.collect { sourceSet ->
            (
                [ sourceSet.output.classesDir.toURI().toURL(), sourceSet.output.resourcesDir.toURI().toURL() ]
                + sourceSet.output.dirs.collect { d -> d.toURI().toURL() }
            ).flatten()
        }.flatten()
        def runtimeClasspath = project.configurations.devshell.files.collect { f ->
            f.toURI().toURL()
        }
        runtimeClasspath += sourceSets.collect { sourceSet ->
            project.configurations[sourceSet.runtimeConfigurationName].files.collect { f -> f.toURI().toURL() }
        }
        runtimeClasspath = runtimeClasspath.flatten()
        
        def toWatch = sourceSets*.allSource*.srcDirs[0]
        toWatch += extraWatch.collect { s -> project.file( s ) }
        if( configFile != null ) toWatch += configFile

        project.logger.debug "====================================================================================="
        project.logger.debug "APPLICATION SOURCES"
        project.logger.debug applicationSources.toString()
        project.logger.debug "APPLICATION CLASSPATH"
        project.logger.debug applicationClasspath.toString()
        project.logger.debug "RUNTIME CLASSPATH"
        project.logger.debug runtimeClasspath.toString()
        project.logger.debug "TO WATCH"
        project.logger.debug toWatch.toString()
        project.logger.debug "====================================================================================="

        // == Start the DevShell
        def devShellSPI = new GradleDevShellSPI(
            applicationSources as URL[],
            applicationClasspath as URL[],
            runtimeClasspath as URL[],
            toWatch,
            new JavaWatcher(),
            [ "devshell_rebuild" ],
            project.getGradle().getGradleHomeDir(),
            project.getProjectDir()
        )
        new DevShellCommand( devShellSPI, configResource, configFile, configUrl, openBrowser ).run();
    }
}
