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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * QiWeb Gradle Plugin.
 *
 * Apply the 'java' plugin on the project if absent.
 * <p>
 * Create {@literal secret}, {@literal start} and {@literal devshell} tasks.
 * <p>
 * Define 'devshell' configuration for dev mode classpath injection.
 */
class QiWebPlugin implements Plugin<Project>
{
    void apply( Project project )
    {
        // Java 8
        project.plugins.apply( JavaPlugin )
        project.sourceCompatibility = '1.8'
        project.targetCompatibility = '1.8'
        project.compileJava*.options*.encoding = 'UTF-8'
        project.compileTestJava*.options*.encoding = 'UTF-8'

        // Extensions
        project.extensions.create( "qiweb", QiWebDependencies, project.dependencies )

        // Repositories
        project.repositories { 
            if( project.hasProperty( 'qiwebLocalRepository' ) ) {
                maven { url project.qiwebLocalRepository }
            }
            maven { url "https://repo.codeartisans.org/qiweb" }
            jcenter()
        }

        // Secret Generation Task
        project.task(
            "secret",
            type: QiWebSecretTask,
            group:"QiWeb",
            description: 'Generate a new Application Secret.'
        )

        // Production Mode Task
        project.task(
            "start",
            type: QiWebStartTask,
            group: "QiWeb",
            description: "Start the Application in production mode.",
            dependsOn: project.tasks.getByName( "classes" )
        )

        // DevShell Task
        project.configurations.create( "devshell" )
        project.configurations.devshell {
            description = "QiWeb DevShell Configuration"
            visible = false
        }
        project.dependencies {
            devshell group: "org.qiweb", name: "org.qiweb.doc", version: BuildVersion.VERSION, transitive: false
        }

        project.task(
            "devshell",
            type: QiWebDevShellTask,
            group: "QiWeb",
            description: "Start the Application in development mode.",
            dependsOn: project.tasks.getByName( "classes" )
        )

        project.task(
            "devshell_rebuild",
            type: QiWebRebuildTask,
            group: "QiWeb",
            description: "Rebuild the Application while in development mode. Do not invoke directly.",
            dependsOn: project.tasks.getByName( "classes" )
        )
    }
}
