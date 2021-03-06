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

import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.internal.DefaultPublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.bundling.Zip

import static io.werval.util.Strings.withoutHead;

/**
 * Werval Module Plugin.
 */
class WervalModulePlugin implements Plugin<Project>
{
    void apply( Project project )
    {
        // Werval
        project.plugins.apply( WervalPlugin )

        // Dependencies
        WervalDependencies wervalDependencies = project.extensions.findByType( WervalDependencies )
        project.dependencies {
            compile wervalDependencies .api
            testCompile wervalDependencies .test
        }

        // Plugins Descriptor
        project.task(
            'moduleDescriptor',
            type: ModuleDescriptorTask,
            group: 'Werval',
            description: 'Generate Module Descriptor'
        )
        project.afterEvaluate {
            if( !project.moduleDescriptor.skip )
            {
                // Hook before processResources
                project.processResources.dependsOn project.moduleDescriptor                
            }
        }

        // Module Documentation
        project.task(
            'moduleDocumentation',
            type: ModuleDocumentationTask,
            group: 'Werval',
            description: 'Generate Module Documentation'
        )
        project.afterEvaluate {
            if( !project.moduleDocumentation.skip && project.moduleDocumentation.sourcesDir.exists() )
            {
                // Hook before processResources
                project.processResources.dependsOn project.moduleDocumentation

                // Asciidoctor task setup
                def docGenResourcesPath = "build/generated-src/${project.moduleDocumentation.docSourcesClassifier}/resources"
                def projectSubPath = withoutHead( project.moduleDocumentation.dynDocPackagePrefix, '.' ).replace( '.', '/' ) + '/doc'
                def docBuildPath = docGenResourcesPath + '/' + projectSubPath                
                Configuration asciidoctorConfig = project.configurations.maybeCreate( 'moduleDocumentation_asciidoctor' )
                project.dependencies { moduleDocumentation_asciidoctor 'org.asciidoctor:asciidoctorj:1.5.1' }
                project.task(
                    'moduleDocumentation_asciidoctor',
                    type: AsciidoctorTask,
                    group: 'Werval',
                    description: 'Generate Module Documentation - Asciidoctor sub-task, don\'t use directly',
                ) {
                    doFirst {
                        // Workaround for https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/61
                        System.setProperty( 'jruby.logger.class', 'org.jruby.util.log.StandardErrorLogger' )
                    }
                    classpath = asciidoctorConfig
                    sourceDir = project.moduleDocumentation.sourcesDir
                    outputDir = project.file( docBuildPath )
                    backends = [ 'html5' ]
                    logDocuments = true
                    options = [
                        compact: true,
                        attributes: [
                            'toc': 'right',
                            'sectlink': '',
                            'sectanchors':'',
                            'linkattrs': '',
                            'linkcss':'',
                            'source-highlighter': 'coderay', 'coderay-css': 'class',
                            'werval-version': BuildVersion.VERSION
                        ]
                    ]
                    doLast {
                        project.copy {
                            from project.moduleDocumentation.sourcesDir
                            into docBuildPath
                            excludes += [ '**/*.asciidoc', '**/*.adoc', '**/*.asc', '**/*.ad' ]
                        }
                    }
                    inputs.dir project.moduleDocumentation.sourcesDir
                    outputs.dir project.file( docBuildPath )
                }
                project.sourceSets.main.resources.srcDir project.file( docGenResourcesPath )
                project.moduleDocumentation.dependsOn project.moduleDocumentation_asciidoctor

                // Dynamic Documentation configuration
                def originalRefConfFile = project.moduleDocumentation.referenceConfig
                def dynDocId = project.moduleDocumentation.dynDocId
                if( !originalRefConfFile.exists()
                    || !originalRefConfFile.getText( 'UTF-8' ).contains( 'werval.devshell.dyndocs' ) ) {
                    def dynDocsRefConfPath = 'build/generated-src/werval-dyndocs/resources'
                    project.task(
                        'moduleDocumentation_dyndocs',
                        group: 'Werval',
                        description: 'Generate Module Documentation - DynDocs sub-task, don\'t use directly',
                    ) {
                        inputs.file project.file( 'build.gradle' )
                        inputs.file originalRefConfFile
                        outputs.dir project.file( dynDocsRefConfPath )
                        doLast {
                            if( originalRefConfFile.exists() ) {
                                project.copy {
                                    from originalRefConfFile
                                    into dynDocsRefConfPath
                                }
                            }
                            def dynDocsRefConfFile = project.file( "$dynDocsRefConfPath/reference.conf" )
                            if ( !dynDocsRefConfFile.exists() ) {
                                assert dynDocsRefConfFile.getParentFile().mkdirs()
                                assert dynDocsRefConfFile.createNewFile()
                            }
                            dynDocsRefConfFile.withWriterAppend( 'UTF-8' ) { writer ->
                                writer.write '\n'
                                writer.write '// BEGIN DynDoc Generated Declaration\n'
                                writer.write 'werval.devshell.dyndocs {\n'
                                writer.write '    \"' + dynDocId + '\" {\n'
                                writer.write '        name = "' + (
                                        ( project.description == null || project.description.isEmpty() ) 
                                        ? project.name 
                                        : project.description
                                    ) + '"\n'
                                writer.write '        base_path = "' + projectSubPath + '"\n'
                                writer.write '    }\n'
                                writer.write '}\n'
                                writer.write '//  END  DynDoc Generated Declaration\n'
                                writer.write '\n'
                            }
                        }
                    }
                    project.sourceSets.main.resources.srcDir project.file( dynDocsRefConfPath )
                    project.moduleDocumentation.dependsOn project.moduleDocumentation_dyndocs
                }

                // Documentation sources archives
                project.task(
                    'moduleDocumentation_doczip',
                    type: Zip,
                    group: 'Werval',
                    description: 'Generate Module Documentation - Doc archive sub-task, don\'t use directly',
                    dependsOn: project.moduleDocumentation_asciidoctor
                ) {
                    classifier = project.moduleDocumentation.docSourcesClassifier
                    from project.moduleDocumentation.sourcesDir
                }
                project.artifacts {
                    archives project.moduleDocumentation_doczip
                }
                // Special care must be taken configuring publishing because the extension must be created once only
                // See http://stackoverflow.com/questions/21190230
                project.plugins.withType(PublishingPlugin) { PublishingPlugin publishingPlugin ->
                    DefaultPublishingExtension publishingExtension = project.getExtensions().getByType(DefaultPublishingExtension)
                    publishingExtension.publications.withType(MavenPublication) {
                        artifact( project.moduleDocumentation_doczip ) {
                            classifier = project.moduleDocumentation.docSourcesClassifier
                            extension = 'zip'
                        }
                    }
                }
            }
        }
    }
}
