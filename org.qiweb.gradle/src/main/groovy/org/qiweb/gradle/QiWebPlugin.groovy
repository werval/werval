package org.qiweb.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.qiweb.devshell.JNotifyWatcher
import org.qiweb.devshell.DevShell
import org.gradle.api.Task

class QiWebPlugin implements Plugin<Project> {


    void apply( Project project ) {

        project.logger.lifecycle ">> Applying QiWeb DevShell Gradle Plugin on " + project.name
        
        project.extensions.create( "qiweb", QiWebPluginExtension )
        
        project.task( "devshell", description: 'Start the QiWeb DevShell.' ) << {
            
            project.logger.lifecycle ">> QiWeb DevShell for " + project.getName() + " starting..."
            
            def projectName = project.getName()
            def projectDir = project.getProjectDir()
            def buildDir = project.getBuildDir();

            def mainSources = project.sourceSets*.allSource*.srcDirs[0]
            def mainOutput = new File( buildDir, "classes/main" )
        
            def testSources = project.sourceSets*.allSource*.srcDirs[1]
            def testOutput = new File( buildDir, "classes/test" )

            def mainClassPath = project.sourceSets.main.runtimeClasspath.files.collect { f -> f.toURI().toURL() }
            def testClassPath = project.sourceSets.test.runtimeClasspath.files.collect { f -> f.toURI().toURL() }

            // Start with a build
            project.getTasksByName( project.qiweb.mainRebuildTask, true)*.each  { Task task ->
                //task.execute()
            }

            // Deploy JNotify            
            JNotifyWatcher.deployNativeLibraries( buildDir )
            
            // Start the DevShell
            
            def devShellSPI = new org.qiweb.gradle.GradleDevShellSPI(
                projectName, projectDir, buildDir, 
                mainSources, mainOutput, mainClassPath as URL[], project.qiweb.mainRebuildTask,
                testSources, testOutput, testClassPath as URL[], project.qiweb.testRebuildTask,
                new JNotifyWatcher() )
            
            def devShell = new DevShell( devShellSPI )
            addShutdownHook { devShell.stop() }
            devShell.start()
        }
    }

}
