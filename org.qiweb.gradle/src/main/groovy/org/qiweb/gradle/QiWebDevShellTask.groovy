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
package org.qiweb.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

import org.qiweb.commands.DevShellCommand
import org.qiweb.devshell.JavaWatcher

class QiWebDevShellTask extends DefaultTask
{
    /**
     * Application source sets, default to {@literal main} only.
     */
    Set<SourceSet> sourceSets = new LinkedHashSet<>( [ project.sourceSets.main ] )

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

    @TaskAction
    void runDevShell()
    {
        project.logger.lifecycle ">> QiWeb DevShell for " + project.getName() + " starting..."

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
            [ sourceSet.output.classesDir.toURI().toURL(), sourceSet.output.resourcesDir.toURI().toURL() ]
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
            project.getProjectDir(),
            ["devshell_rebuild"]
        )
        new DevShellCommand( devShellSPI, configResource, configFile, configUrl ).run();
    }
}
