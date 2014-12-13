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
import java.util.Properties
import org.asciidoctor.gradle.AsciidoctorPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static io.werval.util.Strings.isEmpty;

/**
 * Module Descriptor Task.
 */
class ModuleDescriptorTask extends DefaultTask
{
    /**
     * Skip flag.
     */
    boolean skip = false

    /**
     * Module's plugins.
     */
    List<Map> plugins = []

    /**
     * Project's plugins descriptor, defaulted to {@literal src/main/resources/META-INF/qiweb-plugins.properties}.
     */
    File descriptorFile = project.file( 'src/main/resources/META-INF/qiweb-plugins.properties' )

    void plugin( Map namedParams, String name )
    {
        plugin( namedParams << [ name: name ] )
    }

    void plugin( Map namedParams )
    {
        plugins << namedParams
    }

    @TaskAction
    void moduleDescriptor()
    throws Exception
    {
        if( skip )
        {
            project.logger.info 'Werval module descriptor generation skipped'
            return;
        }
        Properties props = new Properties();
        if( descriptorFile.exists() )
        {
            descriptorFile.withInputStream { input ->
                props.load( input );
            }
        }
        def changed = false;
        plugins.each { plugin ->
            if( isEmpty( plugin.name ) || isEmpty( plugin.impl ) )
            {
                throw new GradleException( "Module plugin declaration incomplete: " + plugin );
            }
            if( !plugin.impl.equals( props.getProperty( plugin.name ) ) )
            {
                if( props.getProperty( plugin.name ) != null )
                {
                    project.logger.warn "Plugin '${name}' found in ${descriptorFile} overriden with ${plugin.impl}"
                }
                props.setProperty( plugin.name, plugin.impl );
                changed = true;
            }
        }
        // TODO ensure each declared plugin impl is present in module classpath and is a plugin
        if( changed )
        {
            def genDescBaseDir = project.file( "build/generated-src/werval-dyndesc/resources/META-INF" )
            if( !genDescBaseDir.exists() )
            {
                assert genDescBaseDir.mkdirs()
            }
            def genDescRefConfFile = new File( genDescBaseDir,  'qiweb-plugins.properties' )
            genDescRefConfFile.withOutputStream { output ->
                props.save( output, "UTF-8" )
            }
            project.sourceSets.main.resources.srcDir genDescBaseDir.getParent()
        }
    }
}
