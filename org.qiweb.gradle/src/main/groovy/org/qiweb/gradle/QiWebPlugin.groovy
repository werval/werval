package org.qiweb.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.qiweb.devshell.JNotifyWatcher
import org.qiweb.devshell.DevShell
import org.gradle.api.Task

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
            
            def projectName = project.getName()
            def rootDir = project.getProjectDir()
            def buildDir = project.getBuildDir();

            def mainSources = project.sourceSets*.allSource*.srcDirs[0]
            def mainOutput = new File( buildDir, "classes/main" )
        
            def testSources = project.sourceSets*.allSource*.srcDirs[1]
            def testOutput = new File( buildDir, "classes/test" )

            def mainClassPath = project.sourceSets.main.runtimeClasspath.files.collect { f -> f.toURI().toURL() }
            def testClassPath = project.sourceSets.test.runtimeClasspath.files.collect { f -> f.toURI().toURL() }

            // == Deploy JNotify Native Librairies
            
            JNotifyWatcher.deployNativeLibraries( buildDir )
            
            // == Start the DevShell
            
            def devShellSPI = new org.qiweb.gradle.GradleDevShellSPI(
                projectName, rootDir, buildDir, 
                mainSources, mainOutput, mainClassPath as URL[], project.qiweb.mainRebuildTask,
                testSources, testOutput, testClassPath as URL[], project.qiweb.testRebuildTask,
                new JNotifyWatcher() )
            
            def final devShell = new DevShell( devShellSPI )
            addShutdownHook { devShell.stop() }
            devShell.start()
        }
    }

}
