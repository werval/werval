/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.gradle;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.qiweb.devshell.QiWebDevShellException;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.spi.dev.DevShellSPIAdapter;

/**
 * Gradle DevShellSPI implementation.
 *
 * Use the Gradle Tooling API to rebuild the project.
 */
public final class GradleDevShellSPI
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

    public GradleDevShellSPI(
        URL[] applicationClassPath, URL[] runtimeClassPath,
        Set<File> toWatch, SourceWatcher watcher,
        File rootDir, String rebuildTask
    )
    {
        super( applicationClassPath, runtimeClassPath, toWatch, watcher );
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
        catch( Exception ex )
        {
            throw new QiWebDevShellException( "Unable to rebuild application sources", ex );
        }
        finally
        {
            connection.close();
        }
    }
}
