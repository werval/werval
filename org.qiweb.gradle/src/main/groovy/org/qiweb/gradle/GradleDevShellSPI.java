package org.qiweb.gradle;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.qiweb.spi.dev.DevShellSPIAdapter;
import org.qiweb.spi.dev.Watcher;

/**
 * Gradle DevShellSPI implementation.
 * <p>Use the Gradle Tooling API to rebuild the project.</p>
 */
public class GradleDevShellSPI
    extends DevShellSPIAdapter
{

    /**
     * Name of the Gradle task to run to rebuild the main sources.
     */
    private final String mainRebuildTask;
    /**
     * Name of the Gradle task to run to rebuild the test sources.
     */
    private final String testRebuildTask;
    /**
     * Gradle Tooling API Connector.
     */
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
        }
    }

    @Override
    protected void doRebuildTest()
    {
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
        }
    }
}
