/**
 * Copyright (c) 2014 the original author or authors
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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

import org.qiweb.commands.StartCommand

/**
 * QiWeb Start Task.
 */
class QiWebStartTask extends DefaultTask
{
    /**
     * Application source sets, default to {@literal main} only.
     */
    Set<SourceSet> sourceSets = new LinkedHashSet<>( [ project.sourceSets.main ] )

    /**
     * Main class, default to io.werval.server.bootstrap.Main.
     */
    String mainClass = "io.werval.server.bootstrap.Main"

    /**
     * Main class arguments.
     */
    String[] arguments = new String[ 0 ]

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

    @TaskAction
    void startProductionMode()
    {
        project.logger.lifecycle ">> QiWeb Production Mode for " + project.getName() + " starting..."

        // Default behaviour
        if( sourceSets == null )
        {
            sourceSets = new LinkedHashSet<>( [ project.sourceSets.main ] )
        }

        StringBuffer msg = new StringBuffer( "Invoking : " );
        msg.append( mainClass );
        msg.append( ".main(" );
        arguments.eachWithIndex { arg, idx ->
            if( idx > 0 ) {
                msg.append( ", " )
            }
            msg.append( arg )
        }
        msg.append( ")" );
        project.logger.debug msg.toString()

        def applicationClasspath = sourceSets.collect { sourceSet ->
            [ sourceSet.output.classesDir.toURI().toURL(), sourceSet.output.resourcesDir.toURI().toURL() ]
        }.flatten()
        def runtimeClasspath = sourceSets.collect { sourceSet ->
            project.configurations[sourceSet.runtimeConfigurationName].files.collect { f -> f.toURI().toURL() }
        }.flatten()

        project.logger.debug "====================================================================================="
        project.logger.debug "APPLICATION CLASSPATH"
        project.logger.debug applicationClasspath.toString()
        project.logger.debug "RUNTIME CLASSPATH"
        project.logger.debug runtimeClasspath.toString()
        project.logger.debug "====================================================================================="

        new StartCommand(
            StartCommand.ExecutionModel.ISOLATED_THREADS,
            mainClass,
            arguments,
            ( applicationClasspath + runtimeClasspath ) as URL[],
            configResource, configFile, configUrl
        ).run();
    }
}
