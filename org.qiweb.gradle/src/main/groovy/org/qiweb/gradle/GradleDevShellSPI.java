package org.qiweb.gradle;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.spi.dev.DevShellSPIAdapter;

/**
 * Gradle DevShellSPI implementation.
 * <p>Use the Gradle Tooling API to rebuild the project.</p>
 */
public class GradleDevShellSPI
    extends DevShellSPIAdapter
{

    /**
     * Gradle Tooling API Connector.
     */
    private final GradleConnector connector = GradleConnector.newConnector();
    /**
     * Name of the Gradle task to run to rebuild the sources.
     */
    private final String rebuildTask;

    public GradleDevShellSPI( URL[] applicationClassPath, URL[] runtimeClassPath,
                              Set<File> sources, SourceWatcher watcher,
                              File rootDir, String rebuildTask )
    {
        super( applicationClassPath, runtimeClassPath, sources, watcher );
        this.connector.forProjectDirectory( rootDir );
        this.rebuildTask = rebuildTask;
    }

    @Override
    protected void doRebuild()
    {
        ProjectConnection connection = connector.connect();
        try
        {
            connection.newBuild().
                forTasks( rebuildTask ).
                run();
        }
        finally
        {
            connection.close();
        }
    }
}
