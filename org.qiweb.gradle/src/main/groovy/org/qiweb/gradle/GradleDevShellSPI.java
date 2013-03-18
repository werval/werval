package org.qiweb.gradle;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.qiweb.spi.dev.DevShellSPIAdapter;
import org.qiweb.spi.dev.Watcher;

public class GradleDevShellSPI
    extends DevShellSPIAdapter
{

    private final String mainRebuildTask;
    private final String testRebuildTask;
    private final GradleConnector connector = GradleConnector.newConnector();

    public GradleDevShellSPI(
        String name, File rootDir, File buildDir,
        Set<File> mainSources, File mainOutput, URL[] mainClassPath, String mainRebuildTask,
        Set<File> testSources, File testOutput, URL[] testClassPath, String testRebuildTask,
        Watcher watcher )
    {
        super( name, rootDir, buildDir, mainSources, mainOutput, mainClassPath, testSources, testOutput, testClassPath, watcher );
        this.mainRebuildTask = mainRebuildTask;
        this.testRebuildTask = testRebuildTask;
        this.connector.forProjectDirectory( rootDir );
    }

    @Override
    protected void doRebuildMain()
    {
        System.out.println( "-------------------------------------------------------" );
        System.out.println( "REBUILD MAIN" );

        ProjectConnection connection = connector.connect();
        try
        {
            connection.newBuild().
                forTasks( mainRebuildTask ).
                run();
        }
        finally
        {
            connection.close();
            System.out.println( "-------------------------------------------------------" );
        }
    }

    @Override
    protected void doRebuildTest()
    {
        System.out.println( "-------------------------------------------------------" );
        System.out.println( "REBUILD TEST" );

        ProjectConnection connection = connector.connect();
        try
        {
            connection.newBuild().
                forTasks( testRebuildTask ).
                run();
        }
        finally
        {
            connection.close();
            System.out.println( "-------------------------------------------------------" );
        }
    }
}
