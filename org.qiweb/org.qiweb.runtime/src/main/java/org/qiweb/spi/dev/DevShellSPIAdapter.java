/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.spi.dev;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
import org.codeartisans.java.toolbox.ObjectHolder;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.spi.dev.Watcher.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for DevShellSPI that listen to changes but has NOOP rebuild methods.
 * <p>Extend and override to your will.</p>
 * <p>Note that this is not the DevShellSPI responsibility to trigger rebuilds.</p>
 */
public class DevShellSPIAdapter
    implements DevShellSPI
{

    private static final Logger LOG = LoggerFactory.getLogger( DevShellSPIAdapter.class );
    private final URL[] classPath;
    private boolean sourceChanged = true;

    public DevShellSPIAdapter( URL[] classPath, Set<File> sources, Watcher watcher )
    {
        this.classPath = Arrays.copyOf( classPath, classPath.length );
        // TODO Unwatch sources on DevShell passivation
        watcher.watch( sources, new ChangeListener()
        {
            @Override
            public void onChange()
            {
                LOG.info( "Source changed!" );
                sourceChanged = true;
            }
        } );
    }

    @Override
    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public final URL[] classPath()
    {
        return classPath;
    }

    @Override
    public String sourceURL( final String fileName, int lineNumber )
    {
        for( URL path : classPath )
        {
            try
            {
                File root = new File( path.toURI() );
                if( root.isDirectory() )
                {
                    final ObjectHolder<File> found = new ObjectHolder<>();
                    Files.walkFileTree( root.toPath(), new SimpleFileVisitor<Path>()
                    {
                        @Override
                        public FileVisitResult visitFile( Path path, BasicFileAttributes attrs )
                            throws IOException
                        {
                            File file = path.toFile();
                            if( fileName.equals( file.getName() ) )
                            {
                                found.setHolded( file );
                                return FileVisitResult.TERMINATE;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    } );
                    if( found.getHolded() != null )
                    {
                        return "file://" + found.getHolded().getAbsolutePath() + "#L" + lineNumber;
                    }
                }
            }
            catch( URISyntaxException | IOException ex )
            {
                throw new QiWebException( ex.getMessage(), ex );
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
    {
        if( sourceChanged )
        {
            doRebuild();
            sourceChanged = false;
        }
    }

    /**
     * No operation.
     */
    protected void doRebuild()
    {
        // NOOP
    }
}
