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
package org.qiweb.devshell;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.qiweb.spi.dev.DevShellSPI.SourceChangeListener;
import org.qiweb.spi.dev.DevShellSPI.SourceWatch;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.util.LinkedMultiValueMap;
import org.qiweb.util.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.qiweb.runtime.util.Iterables.first;
import static org.qiweb.util.IllegalArguments.ensureNotNull;

/**
 * Java WatchService based SourceWatcher.
 * <p>
 * Based on the <a href="http://docs.oracle.com/javase/tutorial/essential/io/notification.html">Watching a Directory
 * for Changes</a> Java Tutorial.
 * <p>
 * Adapted for QiWeb needs and extended to support watching individual files.
 */
// Note that thanks to the akward Java WatchService API, this code is pretty fragile.
// This implementation is greedy and may leak. Not critical tough as it is used in dev mode only.
public class JavaWatcher
    implements SourceWatcher
{
    private static abstract class Watched
    {
        protected final Path path;

        Watched( Path path )
        {
            this.path = path;
        }

        final Path path()
        {
            return path;
        }

        boolean satisfiedBy( WatchEvent<?> event )
        {
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 41 * hash + Objects.hashCode( this.path );
            return hash;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( obj == null )
            {
                return false;
            }
            if( getClass() != obj.getClass() )
            {
                return false;
            }
            final Watched other = (Watched) obj;
            return Objects.equals( this.path, other.path );
        }

    }

    private static final class WatchedDirectory
        extends Watched
    {
        WatchedDirectory( Path path )
        {
            super( path );
            ensureNotNull( "Watched Directory Path", path );
        }
    }

    private static final class WatchedSingleFile
        extends Watched
    {
        WatchedSingleFile( Path path )
        {
            super( path );
            ensureNotNull( "Watched Single File Path", path );
        }

        @Override
        boolean satisfiedBy( WatchEvent<?> event )
        {
            if( event.context() != null && event.context() instanceof Path )
            {
                Path eventPath = (Path) event.context();
                if( path.getFileName().equals( eventPath ) )
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class SourceChangeWatcher
        implements Runnable
    {
        private final WatchService watchService;
        private final MultiValueMap<WatchKey, Watched> keys;
        private final SourceChangeListener listener;
        private boolean run = true;

        private SourceChangeWatcher(
            WatchService watchService,
            MultiValueMap<WatchKey, Watched> keys,
            SourceChangeListener listener
        )
            throws IOException
        {
            this.watchService = watchService;
            this.keys = keys;
            this.listener = listener;
        }

        @Override
        public void run()
        {
            for( ;; )
            {
                if( !run )
                {
                    LOG.trace( "Source Change Watcher Thread stopped" );
                    return;
                }

                // Wait for a key to be signalled
                WatchKey key;
                try
                {
                    key = watchService.take();
                }
                catch( InterruptedException ex )
                {
                    Thread.interrupted();
                    LOG.trace( "Source Change Watcher Thread interrupted" );
                    return;
                }

                List<Watched> watcheds = keys.get( key );
                if( watcheds == null || watcheds.isEmpty() )
                {
                    LOG.warn( "WatchKey not recognized!!" );
                    continue;
                }

                LOG.trace( "Handling change event for {} keyed to {}", watcheds, key );

                // Retrieve events for the signalled key
                List<WatchEvent<?>> watchEvents = key.pollEvents();

                // Has source changed?
                boolean sourceChanged = false;
                MultiValueMap<Watched, WatchEvent<Path>> matchedEvents = new LinkedMultiValueMap<>();

                // Collect WatchEvents for each matching Watched
                for( Watched watched : watcheds )
                {
                    for( WatchEvent<?> watchEvent : watchEvents )
                    {
                        if( watched.satisfiedBy( watchEvent ) )
                        {
                            matchedEvents.add( watched, (WatchEvent<Path>) watchEvent );
                            sourceChanged = true;
                        }
                    }
                }

                // Notify source change
                if( sourceChanged )
                {
                    listener.onChange();
                }

                // Handle watch matches
                for( Watched watched : matchedEvents.keySet() )
                {
                    if( watched instanceof WatchedDirectory )
                    {
                        LOG.trace( "Directory changed {}", watched.path().toAbsolutePath() );
                        // Recursively watch newly created sub-directories
                        for( WatchEvent<Path> event : matchedEvents.get( watched ) )
                        {
                            WatchEvent.Kind<?> kind = event.kind();

                            if( kind == OVERFLOW )
                            {
                                LOG.trace( "{} events may have been lost or discarded.", event.count() );
                                continue;
                            }

                            // Context for directory entry event is the file name of entry
                            WatchEvent<Path> ev = cast( event );
                            Path name = ev.context();
                            Path child = watched.path().resolve( name );

                            LOG.debug( "{}: {}", event.kind().name(), child );

                            // if directory is created then register it and its sub-directories
                            if( kind == ENTRY_CREATE && isDirectory( child, NOFOLLOW_LINKS ) )
                            {
                                try
                                {
                                    registerDirectory( child, watchService, keys );
                                    LOG.trace( "Watching newly created directory {}", child.toAbsolutePath() );
                                }
                                catch( IOException ex )
                                {
                                    LOG.warn( "Unable to watch newly created directory: {}", ex.getMessage(), ex );
                                }
                            }
                        }
                    }
                    else if( watched instanceof WatchedSingleFile )
                    {
                        LOG.trace( "Single File changed {}", watched.path().toAbsolutePath() );
                    }
                    else
                    {
                        throw new InternalError(
                            "Something is wrong with " + JavaWatcher.class.getName() + " codebase, please report!"
                        );
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if( !valid )
                {
                    keys.remove( key );
                    LOG.trace( "Invalidated (deleted?) {}", watcheds );
                }
                // all watched are gone
                if( keys.isEmpty() )
                {
                    LOG.warn( "Nothing left to watch, stopping source watcher thread!" );
                    break;
                }
            }
        }

        private void stop()
        {
            run = false;
        }

        private <T> WatchEvent<T> cast( WatchEvent<?> event )
        {
            return (WatchEvent<T>) event;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger( JavaWatcher.class );
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();
    private static final WatchEvent.Kind<?>[] WATCHED_EVENT_KINDS;
    private static final WatchEvent.Modifier[] WATCHED_MODIFIERS;

    static
    {
        WATCHED_EVENT_KINDS = new WatchEvent.Kind<?>[]
        {
            ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
        };
        WatchEvent.Modifier[] modifiers;
        try
        {
            // com.sun based tuning, **really** faster on OSX
            Class<?> sensitivityEnumClass = Class.forName( "com.sun.nio.file.SensitivityWatchEventModifier" );
            modifiers = new WatchEvent.Modifier[]
            {
                (WatchEvent.Modifier) sensitivityEnumClass.getEnumConstants()[0]
            };
        }
        catch( ClassNotFoundException ex )
        {
            // Sensitivity modifier not available, falling back to no modifiers
            modifiers = new WatchEvent.Modifier[ 0 ];
        }
        WATCHED_MODIFIERS = modifiers;
    }

    @Override
    public synchronized SourceWatch watch( Set<File> filesAndDirectories, SourceChangeListener listener )
    {
        try
        {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            final MultiValueMap<WatchKey, Watched> keys = new LinkedMultiValueMap<>();
            for( File fileOrDirectory : filesAndDirectories )
            {
                Path start = fileOrDirectory.toPath();
                if( isSymbolicLink( start ) )
                {
                    throw new DevShellStartException(
                        "Cannot watch '" + start + "', it is a symbolic link. If you need this feature, please report!"
                    );
                }
                else if( isRegularFile( start, NOFOLLOW_LINKS ) )
                {
                    registerSingleFile( start, watchService, keys );
                }
                else if( isDirectory( start, NOFOLLOW_LINKS ) )
                {
                    // register directory and sub-directories
                    walkFileTree(
                        start,
                        new SimpleFileVisitor<Path>()
                        {
                            @Override
                            public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs )
                            throws IOException
                            {
                                registerDirectory( dir, watchService, keys );
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    );
                }
                else if( exists( start ) )
                {
                    throw new DevShellStartException( "Cannot watch '" + start + "', it is neither a file nor a directory." );
                }
                else
                {
                    registerAbsent( start, watchService, keys );
                }
            }
            String watchThreadName = "qiweb-devshell-watcher-" + THREAD_NUMBER.getAndIncrement();
            final SourceChangeWatcher sourceChangeWatcher = new SourceChangeWatcher( watchService, keys, listener );
            final Thread watchThread = new Thread( sourceChangeWatcher, watchThreadName );
            watchThread.start();
            return new SourceWatch()
            {
                @Override
                public void unwatch()
                {
                    Set<WatchKey> keySet = keys.keySet();
                    while( !keySet.isEmpty() )
                    {
                        WatchKey key = first( keySet );
                        key.cancel();
                        keys.remove( key );
                    }
                    sourceChangeWatcher.stop();
                    watchThread.interrupt();
                }
            };
        }
        catch( IOException ex )
        {
            throw new DevShellStartException( "Unable to watch sources for changes", ex );
        }
    }

    private static void registerSingleFile(
        final Path file,
        WatchService watchService,
        MultiValueMap<WatchKey, Watched> keys
    )
        throws IOException
    {
        WatchKey key = file.getParent().register( watchService, WATCHED_EVENT_KINDS, WATCHED_MODIFIERS );
        keys.add( key, new WatchedSingleFile( file ) );
        LOG.trace( "Registered watch on single file {}", file );
    }

    private static void registerDirectory(
        Path directory,
        WatchService watchService,
        MultiValueMap<WatchKey, Watched> keys
    )
        throws IOException
    {
        WatchKey key = directory.register( watchService, WATCHED_EVENT_KINDS, WATCHED_MODIFIERS );
        keys.add( key, new WatchedDirectory( directory ) );
        LOG.trace( "Registered watch on directory {}", directory );
    }

    private static void registerAbsent(
        Path absent,
        WatchService watchService,
        MultiValueMap<WatchKey, Watched> keys
    )
    {
        LOG.warn(
            "{} absent, not watched, you'll need to restart the DevShell if you create it",
            absent
        );
    }
}
