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
import org.gradle.api.tasks.TaskAction

import org.qiweb.commands.DevShellCommand
import org.qiweb.devshell.JavaWatcher

class QiWebDevShellTask extends DefaultTask
{
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
    Set<String> extraWatch = new HashSet<>()

    @TaskAction
    void runDevShell()
    {
        project.logger.lifecycle ">> QiWeb DevShell for " + project.getName() + " starting..."

        // == Gather build info
        def applicationSources = project.sourceSets.main.allSource.srcDirs.collect { f -> 
            f.toURI().toURL()
        }
        def applicationClasspath = [
            project.sourceSets.main.output.classesDir.toURI().toURL(),
            project.sourceSets.main.output.resourcesDir.toURI().toURL()
        ]
        def runtimeClasspath = project.configurations.devshell.files.collect { f ->
            f.toURI().toURL()
        }
        def toWatch = project.sourceSets*.allSource*.srcDirs[0]
        toWatch += extraWatch.collect { s -> project.file( s ) }
        if( configFile != null ) toWatch += configFile

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
