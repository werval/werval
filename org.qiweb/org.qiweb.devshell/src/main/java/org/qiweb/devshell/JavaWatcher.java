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
package org.qiweb.devshell;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.qiweb.spi.dev.DevShellSPI.SourceChangeListener;
import org.qiweb.spi.dev.DevShellSPI.SourceWatch;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.qiweb.runtime.util.Iterables.first;

/**
 * Java WatchService based SourceWatcher.
 * <p>
 *     Based on the <a href="http://docs.oracle.com/javase/tutorial/essential/io/notification.html">Watching a Directory
 *     for Changes</p> Java Tutorial.
 * </p>
 */
public class JavaWatcher
    implements SourceWatcher
{

    private static final class SourceChangeWatcher
        implements Runnable
    {

        private final WatchService watchService;
        private final Map<WatchKey, Path> keys;
        private final SourceChangeListener listener;
        private boolean run = true;

        private SourceChangeWatcher( WatchService watchService, Map<WatchKey, Path> keys, SourceChangeListener listener )
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

                Path fileOrDir = keys.get( key );
                if( fileOrDir == null )
                {
                    System.err.println( "WatchKey not recognized!!" );
                    continue;
                }

                // Notify source change
                listener.onChange();

                if( Files.isDirectory( fileOrDir ) )
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
                        Path child = fileOrDir.resolve( name );

                        // debug
                        if( DEBUG )
                        {
                            System.out.format( "%s: %s\n", event.kind().name(), child );
                        }

                        // if directory is created, and watching recursively, then
                        // register it and its sub-directories
                        if( kind == ENTRY_CREATE )
                        {
                            try
                            {
                                if( Files.isDirectory( child, NOFOLLOW_LINKS ) )
                                {
                                    registerAll( child, watchService, keys );
                                }
                            }
                            catch( IOException x )
                            {
                                // ignore to keep sample readbale
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
    }

    @SuppressWarnings( "unchecked" )
    private static <T> WatchEvent<T> cast( WatchEvent<?> event )
    {
        return (WatchEvent<T>) event;
    }
    private static final boolean DEBUG = false;
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();

    @Override
    public synchronized SourceWatch watch( Set<File> filesAndDirectories, SourceChangeListener listener )
    {
        try
        {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            final Map<WatchKey, Path> keys = new HashMap<>();
            for( File fileOrDirectory : filesAndDirectories )
            {
                registerAll( fileOrDirectory.toPath(), watchService, keys );
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
            throw new QiWebDevShellException( "Unable to watch sources for changes", ex );
        }
    }

    private static void registerAll( final Path start, final WatchService watchService, final Map<WatchKey, Path> keys )
        throws IOException
    {
        if( Files.isRegularFile( start ) )
        {
            register( start, watchService, keys );
        }
        else
        {
            // register directory and sub-directories
            Files.walkFileTree( start, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs )
                    throws IOException
                {
                    register( dir, watchService, keys );
                    return FileVisitResult.CONTINUE;
                }
            } );
        }
    }

    private static void register( Path fileOrDir, WatchService watchService, Map<WatchKey, Path> keys )
        throws IOException
    {
        WatchEvent.Kind<?>[] watchedEvents = new WatchEvent.Kind<?>[]
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

        WatchKey key = fileOrDir.register( watchService, watchedEvents, modifiers );

        if( DEBUG )
        {
            Path prev = keys.get( key );
            if( prev == null )
            {
                System.out.format( "register: %s\n", fileOrDir );
            }
            else
            {
                if( !fileOrDir.equals( prev ) )
                {
                    System.out.format( "update: %s -> %s\n", prev, fileOrDir );
                }
            }
        }

        keys.put( key, fileOrDir );
    }
}
