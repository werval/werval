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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.qiweb.spi.dev.DevShellSPI.SourceChangeListener;
import org.qiweb.spi.dev.DevShellSPI.SourceWatch;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.qiweb.runtime.util.Iterables.first;
import static org.qiweb.util.IllegalArguments.ensureNotNull;

/**
 * Java WatchService based SourceWatcher.
 *
 * Based on the <a href="http://docs.oracle.com/javase/tutorial/essential/io/notification.html">Watching a Directory
 * for Changes</a> Java Tutorial.
 * <p>
 * Adapted for QiWeb needs and extended to support watching individual files.
 * <p>
 * Note that thanks to the akward WatchService API, this code is pretty fragile.
 */
public class JavaWatcher
    implements SourceWatcher
{
    private static final class WatchedFileOrDir
    {
        private final Path fileOrDir;
        private final FilenameFilter filePredicate;

        private WatchedFileOrDir( Path fileOrDir, FilenameFilter filePredicate )
        {
            ensureNotNull( "Watched File or Directory", fileOrDir );
            this.fileOrDir = fileOrDir;
            if( Files.isRegularFile( fileOrDir ) )
            {
                ensureNotNull( "Watched File Predicate", filePredicate );
            }
            this.filePredicate = filePredicate;
        }

        private boolean satisfiedBy( List<WatchEvent<?>> events )
        {
            if( filePredicate == null )
            {
                return true;
            }
            for( WatchEvent<?> event : events )
            {
                if( event.context() != null
                    && event.context() instanceof Path
                    && fileOrDir.getFileName().equals( ( (Path) event.context() ).getFileName() ) )
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
        private final Map<WatchKey, List<WatchedFileOrDir>> keys;
        private final SourceChangeListener listener;
        private boolean run = true;

        private SourceChangeWatcher(
            WatchService watchService,
            Map<WatchKey, List<WatchedFileOrDir>> keys,
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
                    return;
                }

                // wait for key to be signalled
                WatchKey key;
                try
                {
                    key = watchService.take();
                }
                catch( InterruptedException x )
                {
                    return;
                }

                List<WatchedFileOrDir> watchedFileOrDirs = keys.get( key );
                if( keys.get( key ) == null )
                {
                    LOG.warn( "WatchKey not recognized!!" );
                    continue;
                }

                boolean sourceChanged = false;
                List<Path> changedDirectories = new ArrayList<>();

                List<WatchEvent<?>> watchEvents = key.pollEvents();
                for( WatchedFileOrDir watchedFileOrDir : watchedFileOrDirs )
                {
                    if( watchedFileOrDir.satisfiedBy( watchEvents ) )
                    {
                        sourceChanged = true;
                        if( Files.isDirectory( watchedFileOrDir.fileOrDir ) )
                        {
                            changedDirectories.add( watchedFileOrDir.fileOrDir );
                        }
                    }
                }

                if( sourceChanged )
                {
                    // Notify source change
                    listener.onChange();
                }

                for( Path changedDir : changedDirectories )
                {
                    // Process events to maintain watched directories recursively
                    for( WatchEvent<?> event : key.pollEvents() )
                    {
                        WatchEvent.Kind<?> kind = event.kind();

                        // TBD - provide example of how OVERFLOW event is handled
                        if( kind == OVERFLOW )
                        {
                            continue;
                        }

                        // Context for directory entry event is the file name of entry
                        WatchEvent<Path> ev = cast( event );
                        Path name = ev.context();
                        Path child = changedDir.resolve( name );

                        LOG.debug( "{}: {}", event.kind().name(), child );

                        // if directory is created, and watching recursively, then
                        // register it and its sub-directories
                        if( kind == ENTRY_CREATE && Files.isDirectory( child, NOFOLLOW_LINKS ) )
                        {
                            try
                            {
                                registerAll( child, watchService, keys );
                            }
                            catch( IOException ex )
                            {
                                LOG.warn( "Unable to watch newly created directory: {}", ex.getMessage(), ex );
                            }
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if( !valid )
                {
                    keys.remove( key );

                    // all directories are inaccessible
                    if( keys.isEmpty() )
                    {
                        break;
                    }
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
    private static final WatchEvent.Kind<?>[] WATCHED_EVENT_KINDS = new WatchEvent.Kind<?>[]
    {
        ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
    };

    @Override
    public synchronized SourceWatch watch( Set<File> filesAndDirectories, SourceChangeListener listener )
    {
        try
        {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            final Map<WatchKey, List<WatchedFileOrDir>> keys = new HashMap<>();
            for( File fileOrDirectory : filesAndDirectories )
            {
                try
                {
                    registerAll( fileOrDirectory.toPath(), watchService, keys );
                }
                catch( NoSuchFileException ex )
                {
                    LOG.warn(
                        "{} absent, not watched, you'll need to restart the DevShell if you create it",
                        fileOrDirectory
                    );
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
                        sourceChangeWatcher.stop();
                        watchThread.interrupt();
                    }
                }
            };
        }
        catch( IOException ex )
        {
            throw new DevShellStartException( "Unable to watch sources for changes", ex );
        }
    }

    private static void registerAll(
        final Path start,
        final WatchService watchService,
        final Map<WatchKey, List<WatchedFileOrDir>> keys
    )
        throws IOException
    {
        if( Files.isRegularFile( start ) )
        {
            registerFile( start, watchService, keys );
        }
        else
        {
            // register directory and sub-directories
            Files.walkFileTree(
                start,
                new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs )
                    throws IOException
                    {
                        registerDir( dir, watchService, keys );
                        return FileVisitResult.CONTINUE;
                    }
                }
            );
        }
    }

    private static void registerFile(
        final Path file,
        WatchService watchService,
        Map<WatchKey, List<WatchedFileOrDir>> keys
    )
        throws IOException
    {
        WatchKey key = file.getParent().register( watchService, WATCHED_EVENT_KINDS, modifiers() );
        FilenameFilter filter = new FilenameFilter()
        {
            @Override
            public boolean accept( File dir, String name )
            {
                return name.equals( file.getFileName().toString() );
            }
        };
        if( !keys.containsKey( key ) )
        {
            keys.put( key, new ArrayList<WatchedFileOrDir>() );
        }
        keys.get( key ).add( new WatchedFileOrDir( file, filter ) );
    }

    private static void registerDir(
        Path directory,
        WatchService watchService,
        Map<WatchKey, List<WatchedFileOrDir>> keys
    )
        throws IOException
    {
        WatchKey key = directory.register( watchService, WATCHED_EVENT_KINDS, modifiers() );
        if( !keys.containsKey( key ) )
        {
            keys.put( key, new ArrayList<WatchedFileOrDir>() );
        }
        keys.get( key ).add( new WatchedFileOrDir( directory, null ) );
    }

    private static WatchEvent.Modifier[] modifiers()
    {
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
        return modifiers;
    }
}
