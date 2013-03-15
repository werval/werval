package org.qiweb.gradle

import org.qiweb.devshell.DevShellSPIAdapter
import org.gradle.api.Task

class GradleDevShellSPI extends DevShellSPIAdapter  {

    private Set<Task> mainRebuildTasks, testRebuildTasks

    GradleDevShellSPI( 
        projectName, rootDir, buildDir, 
        mainSources, mainOutput, mainClassPath, mainRebuildTasks, 
        testSources, testOutput, testClassPath, testRebuildTasks,
        watcher )
    {
        super( 
            projectName, rootDir, buildDir, 
            mainSources, mainOutput, mainClassPath, 
            testSources, testOutput, testClassPath, 
            watcher )
        this.mainRebuildTasks = mainRebuildTasks
        this.testRebuildTasks = testRebuildTasks
    }

    void rebuildMain()
    {
        System.out.println( "-------------------------------------------------------" )
        System.out.println( "REBUILD MAIN" )

        mainRebuildTasks*.each  { Task task ->
            task.execute()
        }

        System.out.println( "-------------------------------------------------------" )
    }

    void rebuildTest()
    {
        System.out.println( "-------------------------------------------------------" )
        System.out.println( "REBUILD TEST" )

        testRebuildTasks*.each  { Task task ->
            task.execute()
        }

        System.out.println( "-------------------------------------------------------" )
    }

}
