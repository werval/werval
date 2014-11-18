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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.application.CreateStartScripts

/**
 * QiWeb Application Plugin.
 */
class QiWebApplicationPlugin implements Plugin<Project>
{
    void apply( Project project )
    {
        // QiWeb
        project.plugins.apply( QiWebPlugin )

        // Dependencies
        QiWebDependencies qiwebDependencies = project.extensions.findByType( QiWebDependencies )
        project.dependencies {
            compile qiwebDependencies.api
            runtime qiwebDependencies.nettyServer
            testCompile qiwebDependencies.test
        }

        // Application Distribution
        project.plugins.apply( ApplicationPlugin )
        project.applicationName = project.name
        project.mainClassName = "org.qiweb.server.bootstrap.Main"
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
            group: "QiWeb",
            description: "Install the Application distribution tree, ready to run.",
            dependsOn: [ project.tasks.getByName( "clean" ), project.tasks.getByName( "installApp" ) ]
        )

        // TODO task stageHeroku( dependsOn: [ 'stage' ] )
    }
}
