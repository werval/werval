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
package io.werval.spi.dev;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.werval.api.exceptions.WervalException;

/**
 * Adapter for DevShellSPI that listen to changes but has NOOP rebuild methods.
 * <p>
 * Extend and override {@link #doRebuild()} method to your will.
 * <p>
 * Note that this is the werval HttpServer responsibility to trigger rebuilds.
 */
public class DevShellSPIAdapter
    implements DevShellSPI
{
    private final URL[] applicationSources;
    private final URL[] applicationClassPath;
    private final URL[] runtimeClassPath;
    private boolean sourceChanged;

    protected DevShellSPIAdapter(
        URL[] applicationSources,
        URL[] applicationClassPath, URL[] runtimeClassPath,
        Set<File> toWatch, SourceWatcher watcher,
        boolean initialSourceChanged
    )
    {
        this.applicationSources = Arrays.copyOf( applicationSources, applicationSources.length );
        this.applicationClassPath = Arrays.copyOf( applicationClassPath, applicationClassPath.length );
        this.runtimeClassPath = Arrays.copyOf( runtimeClassPath, runtimeClassPath.length );
        // QUID Unwatch sources on DevShell passivation?
        this.sourceChanged = initialSourceChanged;
        watcher.watch(
            toWatch,
            new SourceChangeListener()
            {
                @Override
                public void onChange()
                {
                    System.out.println( "Source changed!" );
                    sourceChanged = true;
                }
            }
        );
    }

    @Override
    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public final URL[] applicationClassPath()
    {
        return applicationClassPath;
    }

    @Override
    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public final URL[] runtimeClassPath()
    {
        return runtimeClassPath;
    }

    @Override
    public String sourceURL( String packageName, final String fileName, int lineNumber )
    {
        final String packagePath = packageName.replaceAll( "\\.", "\\/" );
        for( URL path : applicationSources )
        {
            try
            {
                File root = new File( path.toURI() );
                if( root.isDirectory() )
                {
                    final List<File> found = new ArrayList<>( 1 );
                    Files.walkFileTree(
                        root.toPath(),
                        new SimpleFileVisitor<Path>()
                        {
                            @Override
                            public FileVisitResult visitFile( Path path, BasicFileAttributes attrs )
                            throws IOException
                            {
                                File file = path.toFile();
                                if( fileName.equals( file.getName() ) && path.getParent().endsWith( packagePath ) )
                                {
                                    found.add( file );
                                    return FileVisitResult.TERMINATE;
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    );
                    if( !found.isEmpty() )
                    {
                        return "file://" + found.get( 0 ).getAbsolutePath() + "#L" + lineNumber;
                    }
                }
            }
            catch( URISyntaxException | IOException ex )
            {
                throw new WervalException( ex.getMessage(), ex );
            }
        }
        return null;
    }

    @Override
    public final boolean isSourceChanged()
    {
        return sourceChanged;
    }

    @Override
    public final synchronized void rebuild()
        throws DevShellRebuildException
    {
        if( sourceChanged )
        {
            doRebuild();
            sourceChanged = false;
        }
    }

    @Override
    public final void stop()
    {
        doStop();
    }

    /**
     * No operation.
     *
     * @throws DevShellRebuildException if rebuilding fails
     *
     * @see DevShellSPI#rebuild()
     */
    protected void doRebuild()
        throws DevShellRebuildException
    {
        // NOOP
    }

    /**
     * No operation.
     *
     * @see DevShellSPI#stop()
     */
    protected void doStop()
    {
        // NOOP
    }
}
