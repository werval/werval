/**
 * Copyright (c) 2014-2015 the original author or authors
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.application.CreateStartScripts

/**
 * Werval Application Plugin.
 */
class WervalApplicationPlugin implements Plugin<Project>
{
    void apply( Project project )
    {
        // Werval
        project.plugins.apply( WervalPlugin )

        // Dependencies
        WervalDependencies wervalDependencies = project.extensions.findByType( WervalDependencies )
        project.dependencies {
            compile wervalDependencies.api
            runtime wervalDependencies.nettyServer
            testCompile wervalDependencies.test
        }

        // Application Distribution
        project.plugins.apply( ApplicationPlugin )
        project.applicationName = project.name
        project.mainClassName = "io.werval.server.bootstrap.Main"
        project.applicationDistribution.from( "src/main/resources" ) {
            include "*.conf"
            into "etc"
        }
        project.startScripts {
            defaultJvmOpts = [ "-server", "-Xmx128m" ]
        }
        project.startScripts.with {
            doLast {
                unixScript.text = unixScript.text.replace(
                    'CLASSPATH=$APP_HOME/lib',
                    'CLASSPATH=$APP_HOME/etc/:$APP_HOME/lib'
                ).replaceAll('CLASSPATH=.+\n', '$0cd "\\$APP_HOME"\n')
                windowsScript.text = windowsScript.text.replace(
                    'CLASSPATH=%APP_HOME%\\lib',
                    'CLASSPATH=%APP_HOME%\\etc\\;%APP_HOME%\\lib'
                ).replaceAll('CLASSPATH=.+\r\n', '$0cd "%APP_HOME%"\r\n')
            }
        }
        project.artifacts {
            archives project.distZip
        }

        // Staging from sources
        project.task(
            "stage",
            group: "Werval",
            description: "Install the Application distribution tree, ready to run.",
            dependsOn: [ project.tasks.getByName( "clean" ), project.tasks.getByName( "installDist" ) ]
        )

        // TODO task stageHeroku( dependsOn: [ 'stage' ] )
    }
}
