/**
 * Copyright (c) 2013 the original author or authors
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.qiweb.devshell.JNotifyWatcher
import org.qiweb.devshell.DevShell

/**
 * QiWeb Gradle Plugin.
 */
class QiWebPlugin implements Plugin<Project> {


    void apply( Project project ) {

        project.logger.info ">> Applying QiWeb DevShell Gradle Plugin on " + project.name
        
        project.extensions.create( "qiweb", QiWebPluginExtension )
        
        project.task( "devshell", description: 'Start the QiWeb DevShell.' ) << {
            
            project.logger.lifecycle ">> QiWeb DevShell for " + project.getName() + " starting..."
            
            // == Gather build info
            
            def sources = project.sourceSets*.allSource*.srcDirs[0]
            def applicationClasspath = [
                project.sourceSets.main.output.classesDir.toURI().toURL(),
                project.sourceSets.main.output.resourcesDir.toURI().toURL()
            ]
            def runtimeClasspath = project.sourceSets.main.runtimeClasspath.files.collect { f -> 
                f.toURI().toURL()
            }

            // == Deploy JNotify Native Librairies
            
            JNotifyWatcher.deployNativeLibraries( project.getBuildDir() )
            
            // == Start the DevShell
            
            def devShellSPI = new org.qiweb.gradle.GradleDevShellSPI(
                applicationClasspath as URL[], runtimeClasspath as URL[],
                sources,new JNotifyWatcher(),
                project.getProjectDir(), project.qiweb.rebuildTask )
            
            def final devShell = new DevShell( devShellSPI )
            addShutdownHook { devShell.stop() }
            devShell.start()
        }

        project.task( "secret", description: 'Generate a new Application Secret.' ) << {
            project.logger.lifecycle "New Application Secret: " + org.qiweb.runtime.CryptoInstance.genRandom256bitsHexSecret()
        }

    }

}
