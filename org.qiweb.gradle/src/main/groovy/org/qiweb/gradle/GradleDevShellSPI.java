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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.qiweb.spi.dev.DevShellRebuildException;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.spi.dev.DevShellSPIAdapter;

import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.Strings.hasText;

/**
 * Gradle DevShellSPI implementation.
 * <p>
 * Use the Gradle Tooling API to rebuild the project.
 */
public final class GradleDevShellSPI
    extends DevShellSPIAdapter
{
    private static final String BUILD_OUTPUT_STDOUT
                                = ""
                                  + "        __      __               __\n"
                                  + ".-----.|  |_.--|  |.-----.--.--.|  |_\n"
                                  + "|__ --||   _|  _  ||  _  |  |  ||   _|\n"
                                  + "|_____||____|_____||_____|_____||____|\n"
                                  + "-------------------------------------------------------------------------------\n"
                                  + "\n";
    private static final String BUILD_OUTPUT_STDERR
                                = ""
                                  + "        __      __                  \n"
                                  + ".-----.|  |_.--|  |.-----.----.----.\n"
                                  + "|__ --||   _|  _  ||  -__|   _|   _|\n"
                                  + "|_____||____|_____||_____|__| |__|\n"
                                  + "-------------------------------------------------------------------------------\n"
                                  + "\n";

    /**
     * Number of connection retries to attempt.
     */
    private static final int MAX_RETRIES = 3;

    /**
     * Names of the Gradle tasks to run to rebuild the sources.
     */
    private final List<String> rebuildTasks;

    /**
     * Gradle Tooling API Connector.
     */
    private final GradleConnector connector;

    /**
     * Gradle Daemon Connection.
     */
    private ProjectConnection connection;

    /**
     * Create a new DevShellSPI for the Gradle plugin.
     */
    public GradleDevShellSPI(
        URL[] applicationSources,
        URL[] applicationClassPath, URL[] runtimeClassPath,
        Set<File> toWatch, SourceWatcher watcher,
        List<String> rebuildTasks,
        File gradleHomeDir, File projectDir
    )
    {
        super( applicationSources, applicationClassPath, runtimeClassPath, toWatch, watcher, false );
        this.rebuildTasks = rebuildTasks;
        this.connector = GradleConnector.newConnector()
            .useInstallation( gradleHomeDir )
            .forProjectDirectory( projectDir );
    }

    @Override
    protected void doRebuild()
        throws DevShellRebuildException
    {
        effectivelyRebuild( 0 );
    }

    private void effectivelyRebuild( int retries )
    {
        if( connection == null )
        {
            connection = connector.connect();
        }
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try
        {
            connection.newBuild()
                .withArguments( "-q" )
                .setStandardOutput( stdout )
                .setStandardError( stderr )
                .forTasks( rebuildTasks.toArray( new String[ rebuildTasks.size() ] ) )
                .run();
        }
        catch( IllegalStateException disconnected )
        {
            if( retries < MAX_RETRIES )
            {
                connection = null;
                effectivelyRebuild( retries + 1 );
            }
            else
            {
                throw disconnected;
            }
        }
        catch( Exception buildError )
        {
            closeConnection();
            String stdoutString = uncheckedToString( stdout, UTF_8 );
            String stderrString = uncheckedToString( stderr, UTF_8 );
            boolean stdoutHasText = hasText( stdoutString );
            boolean stderrHasText = hasText( stderrString );
            StringBuilder buildOutput = new StringBuilder();
            if( stdoutHasText )
            {
                buildOutput.append( BUILD_OUTPUT_STDOUT ).append( stdoutString );
                if( stderrHasText )
                {
                    buildOutput.append( "\n\n" );
                }
            }
            if( stderrHasText )
            {
                buildOutput.append( BUILD_OUTPUT_STDERR ).append( stderrString );
            }
            throw new DevShellRebuildException(
                "Gradle build error for tasks: " + rebuildTasks,
                buildError,
                buildOutput.toString()
            );
        }
    }

    @Override
    protected void doStop()
    {
        closeConnection();
    }

    private void closeConnection()
    {
        if( connection != null )
        {
            connection.close();
            connection = null;
        }
    }

    private static String uncheckedToString( ByteArrayOutputStream baos, Charset charset )
    {
        try
        {
            return baos.toString( charset.name() );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
