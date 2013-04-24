package org.qiweb.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.qiweb.devshell.JNotifyWatcher
import org.qiweb.devshell.DevShell

/**
 * QiWeb Gradle Plugin.
 */
class QiWebPlugin implements Plugin<Project> {


    void apply( Project project ) {

        project.logger.info ">> Applying QiWeb DevShell Gradle Plugin on " + project.name
        
        project.extensions.create( "qiweb", QiWebPluginExtension )
        
        project.task( "devshell", description: 'Start the QiWeb DevShell.' ) << {
            
            project.logger.lifecycle ">> QiWeb DevShell for " + project.getName() + " starting..."
            
            // == Gather build info
            
            def sources = project.sourceSets*.allSource*.srcDirs[0]
            def classPath = project.sourceSets.main.runtimeClasspath.files.collect { f -> f.toURI().toURL() }

            // == Deploy JNotify Native Librairies
            
            JNotifyWatcher.deployNativeLibraries( project.getBuildDir() )
            
            // == Start the DevShell
            
            def devShellSPI = new org.qiweb.gradle.GradleDevShellSPI(
                sources, classPath as URL[], new JNotifyWatcher(),
                project.getProjectDir(), project.qiweb.rebuildTask )
            
            def final devShell = new DevShell( devShellSPI )
            addShutdownHook { devShell.stop() }
            devShell.start()
        }
    }

}
