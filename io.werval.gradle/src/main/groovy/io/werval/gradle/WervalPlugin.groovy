/**
 * Copyright (c) 2013-2015 the original author or authors
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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Werval Gradle Plugin.
 *
 * Apply the 'java' plugin on the project if absent.
 * <p>
 * Create {@literal secret}, {@literal start} and {@literal devshell} tasks.
 * <p>
 * Define 'devshell' configuration and 'dev' SourceSet for dev mode classpath enrichment.
 */
class WervalPlugin implements Plugin<Project>
{
    final GRADLE_MIN_MAJOR = 2
    final GRADLE_MIN_MINOR = 2
    final GRADLE_MIN_VERSION = "$GRADLE_MIN_MAJOR.$GRADLE_MIN_VERSION"

    void apply( Project project )
    {
        // Ensure Gradle Version
        def gradleVersion = project.gradle.gradleVersion
        def gradleVerSplit = gradleVersion.split(/\./)
        if( gradleVerSplit[0].toInteger() < GRADLE_MIN_MAJOR || gradleVerSplit[1].toInteger() < GRADLE_MIN_MINOR ) {
            def err = "Werval $BuildVersion.VERSION requires Gradle >= $GRADLE_MIN_VERSION, you're using $gradleVersion"
            project.logger.error err
            throw new GradleException( err )
        }

        // Java 8
        project.plugins.apply( JavaPlugin )
        project.sourceCompatibility = '1.8'
        project.targetCompatibility = '1.8'
        project.compileJava*.options*.encoding = 'UTF-8'
        project.compileTestJava*.options*.encoding = 'UTF-8'

        // Extensions
        project.extensions.create( "werval", WervalDependencies, project.dependencies )

        // Repositories
        project.repositories { 
            if( project.hasProperty( 'wervalLocalRepository' ) ) {
                maven { url project.wervalLocalRepository }
            }
            jcenter()
        }

        // Secret Generation Task
        project.task(
            "secret",
            type: SecretTask,
            group:"Werval",
            description: 'Generate a new Application Secret.'
        )

        // Production Mode Task
        project.task(
            "start",
            type: StartTask,
            group: "Werval",
            description: "Start the Application in production mode.",
            dependsOn: project.tasks.getByName( "classes" )
        )

        // DevShell Task
        project.configurations.create( "devshell" )
        project.configurations.devshell {
            description = "Werval DevShell Configuration"
            // visible = false
        }
        project.dependencies {
            devshell( group: "io.werval", name: "io.werval.doc", version: BuildVersion.VERSION ) {
                exclude module: "io.werval.api"
            }
        }

        project.sourceSets {
            dev
        }

        project.task(
            "devshell",
            type: DevShellTask,
            group: "Werval",
            description: "Start the Application in development mode.",
            dependsOn: [ project.tasks.getByName( "classes" ), project.tasks.getByName( "devClasses" ) ]
        )

        project.task(
            "devshell_rebuild",
            type: RebuildTask,
            group: "Werval",
            description: "Rebuild the Application while in development mode. Do not invoke directly.",
            dependsOn: [ project.tasks.getByName( "classes" ), project.tasks.getByName( "devClasses" ) ]
        )
    }
}
