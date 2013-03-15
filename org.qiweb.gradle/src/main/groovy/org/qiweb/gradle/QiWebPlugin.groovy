package org.qiweb.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.qiweb.devshell.DevShellSPIAdapter
import org.qiweb.devshell.JNotifyWatcher
import org.qiweb.devshell.NewDevShell

class QiWebPlugin implements Plugin<Project> {


    void apply( Project project ) {

        project.logger.lifecycle ">> Applying QiWeb DevShell Gradle Plugin on " + project.name
        
        project.extensions.create( "qiweb", QiWebPluginExtension )
        
        def projectName = project.getName()
        def projectDir = project.getProjectDir()
        def buildDir = project.getBuildDir();

        def mainSources = project.sourceSets*.allSource*.srcDirs[0]
        def mainOutput = new File( buildDir, "classes/main" )
        def mainRebuildTasks = project.getTasksByName( project.qiweb.mainRebuildTask, true)
        
        def testSources = project.sourceSets*.allSource*.srcDirs[1]
        def testOutput = new File( buildDir, "classes/test" )
        def testRebuildTasks = project.getTasksByName( project.qiweb.testRebuildTask, true)

        project.task( "devshell", dependsOn: mainRebuildTasks, description: 'Start the QiWeb DevShell.' ) << {
            
            project.logger.lifecycle "QiWeb DevShell for " + projectName + " starting..."
            
            def mainClassPath = project.sourceSets.main.runtimeClasspath.files.collect { f -> f.toURI().toURL() }
            def testClassPath = project.sourceSets.test.runtimeClasspath.files.collect { f -> f.toURI().toURL() }
            
            JNotifyWatcher.deployNativeLibraries( buildDir )
            
            def devShellSPI = new GradleDevShellSPI(
                projectName, projectDir, buildDir, 
                mainSources, mainOutput, mainClassPath as URL[], mainRebuildTasks,
                testSources, testOutput, testClassPath as URL[], testRebuildTasks,
                new JNotifyWatcher() )
            
            def devShell = new NewDevShell( devShellSPI )
            addShutdownHook { devShell.stop() }
            devShell.start()
        }
    }

}
