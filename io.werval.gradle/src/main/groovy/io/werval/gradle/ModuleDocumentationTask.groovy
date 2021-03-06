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
package io.werval.gradle

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Module Documentation Task.
 * <p>
 * Asciidoctor setup is done in WervalModulePlugin.
 */
class ModuleDocumentationTask extends DefaultTask
{
    /**
     * Skip flag.
     */
    boolean skip = false;

    /**
     * Module's documentation sources directory, defaulted to {@literal src/doc}.
     */
    File sourcesDir = project.file( 'src/doc' )

    /**
     * Module's reference configuration file, defaulted to {@literal src/main/resources/reference.conf}. 
     */
    File referenceConfig = project.file( 'src/main/resources/reference.conf' )

    /**
     * Module's Dynamic Documentation ID, defaulted to project's name.
     */
    String dynDocId = "${project.name}"

    /**
     * Module's Dynamic Documentation package prefix, defaulted to {@literal ${project.group}.${project.name} }.
     */
    String dynDocPackagePrefix = "${project.group}.${project.name}"

    /**
     * Documentation sources archive classifier, defaulted to {@literal werval-doc}.
     */
    String docSourcesClassifier = "werval-doc";

    @TaskAction
    void moduleDoc()
    {
        if( skip )
        {
            project.logger.info 'Werval module documentation generation skipped'
            return;
        }
        if( !sourcesDir.exists() ) {
            project.logger.warn "This Werval Module has no documentation"
            return;
        }
        // Meat of this task is implemented in the WervalModulePlugin class
    }
}
