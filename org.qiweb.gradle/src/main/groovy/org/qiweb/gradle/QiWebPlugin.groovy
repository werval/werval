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

/**
 * QiWeb Gradle Plugin.
 */
class QiWebPlugin implements Plugin<Project>
{

    void apply( Project project )
    {
        project.configurations.create( "qiweb" )
        project.configurations.qiweb {
            description = "QiWeb Runtime Configuration"
            visible = false
            extendsFrom project.configurations.runtime
        }
        project.dependencies {
            qiweb "org.qiweb:org.qiweb.doc:" + BuildVersion.VERSION
        }

        project.extensions.create(
            "qiweb",
            QiWebPluginExtension
        )

        project.task( 
            "devshell",
            type: QiWebDevShellTask,
            group: "QiWeb",
            description: "Start the QiWeb DevShell.",
            // FIXME This use the default value as the project value is not set yet !!!! Chicken and egg problem again..
            dependsOn: project.tasks.getByName( project.qiweb.rebuildTask )
        )

        project.task( 
            "secret",
            type: QiWebSecretTask,
            group:"QiWeb",
            description: 'Generate a new Application Secret.'
        )
    }

}
