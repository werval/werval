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
import org.gradle.api.tasks.TaskAction

import org.qiweb.commands.StartCommand

/**
 * QiWeb Start Task.
 */
class QiWebStartTask extends DefaultTask
{
    private String mainClass = "org.qiweb.server.bootstrap.Main"
    private String[] arguments = new String[ 0 ]

    @TaskAction
    void startProductionMode()
    {
        project.logger.lifecycle ">> QiWeb Production Mode for " + project.getName() + " starting..."

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

        def applicationClasspath = [
            project.sourceSets.main.output.classesDir.toURI().toURL(),
            project.sourceSets.main.output.resourcesDir.toURI().toURL()
        ]
        def runtimeClasspath = project.configurations.runtime.files.collect { f -> f.toURI().toURL() }

        new StartCommand(
            StartCommand.ExecutionModel.ISOLATED_THREADS,
            mainClass,
            arguments,
            ( applicationClasspath + runtimeClasspath ) as URL[]
        ).run();
    }
}
