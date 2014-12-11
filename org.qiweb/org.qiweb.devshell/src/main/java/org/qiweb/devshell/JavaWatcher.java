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

import io.werval.util.LinkedMultiValueMap;
import io.werval.util.MultiValueMap;
import io.werval.spi.dev.DevShellSPI.SourceChangeListener;
import io.werval.spi.dev.DevShellSPI.SourceWatch;
import io.werval.spi.dev.DevShellSPI.SourceWatcher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.runtime.util.Iterables.first;
import static io.werval.util.IllegalArguments.ensureNotNull;
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

/**
 * Java WatchService based SourceWatcher.
 * <p>
 * Based on the <a href="http://docs.oracle.com/javase/tutorial/essential/io/notification.html">Watching a Directory
 * for Changes</a> Java Tutorial.
 * <p>
 * Adapted for QiWeb needs and extended to support watching individual files and deletion of watched directories.
 */
// Note that thanks to the akward Java WatchService API, this code is pretty fragile.
// This implementation is greedy and may leak. Not critical tough as it is used in dev mode only.
// =====================================================================================================================
// TODO JavaWatcher> Handle OVERFLOW> reprocess watched directory
//      Could lead to missed events if it prevents new watches registration.
//      Not critical as this would be after stating that the source changed.
// TODO JavaWatcher> Refactor to move WatchedDirectory deletion handling into the main logic
public class JavaWatcher
    implements SourceWatcher
{
    private abstract static class Watched
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

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "{" + "path=" + path + '}';
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

    private static final class WatchedAbsent
        extends Watched
    {
        private final Path upstream;

        WatchedAbsent( Path path, Path upstream )
        {
            super( path );
            ensureNotNull( "Watched Absent Path", path );
            ensureNotNull( "Watched Absent Upstream Path", upstream );
            this.upstream = upstream;
        }

        public Path upstream()
        {
            return upstream;
        }

        @Override
        boolean satisfiedBy( WatchEvent<?> event )
        {
            if( event.context() != null && event.context() instanceof Path )
            {
                Path eventPath = (Path) event.context();
                if( path.startsWith( upstream.resolve( eventPath ) ) )
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = super.hashCode();
            hash = 17 * hash + Objects.hashCode( this.upstream );
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
            final WatchedAbsent other = (WatchedAbsent) obj;
            return !( !Objects.equals( this.upstream, other.upstream ) || !Objects.equals( path(), other.path() ) );
        }
    }

    private static final class ArtificialWatchedDirectoryDeletedEvent
        implements WatchEvent<Path>
    {
        private final Path context;

        private ArtificialWatchedDirectoryDeletedEvent( Path context )
        {
            this.context = context;
        }

        @Override
        public Kind<Path> kind()
        {
            return ENTRY_DELETE;
        }

        @Override
        public int count()
        {
            return 1;
        }

        @Override
        public Path context()
        {
            return context;
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

                // Retrieve events for the signalled key
                List<WatchEvent<?>> watchEvents = key.pollEvents();

                LOG.trace( ">> {} total events for {}", watchEvents.size(), watcheds );

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
                        }
                    }
                }

                // WatchedDirectory special handling
                for( final Watched watched : watcheds )
                {
                    if( watched instanceof WatchedDirectory
                        && !matchedEvents.containsKey( watched )
                        && watchEvents.isEmpty()
                        && !exists( watched.path() ) )
                    {
                        LOG.trace( "{} deleted! Inserting an artificial event.", watched );
                        matchedEvents.add( watched, new ArtificialWatchedDirectoryDeletedEvent( watched.path() ) );
                    }
                }

                // Handle watch matches
                for( Watched watched : matchedEvents.keySet() )
                {
                    LOG.trace( ">>>> {} matching events for {}", matchedEvents.get( watched ).size(), watched );
                    if( watched instanceof WatchedDirectory )
                    {
                        LOG.trace( "Directory changed {}", watched.path().toAbsolutePath() );
                        sourceChanged = true;
                        if( exists( watched.path() ) )
                        {
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

                                LOG.trace( "{}: {}", event.kind().name(), child );

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
                        else
                        {
                            try
                            {
                                registerAbsent( watched.path(), watchService, keys );
                            }
                            catch( IOException ex )
                            {
                                LOG.warn(
                                    "Unable to watch absent '{}' on watched directory deletion: {}",
                                    watched.path(), ex.getMessage(), ex
                                );
                            }
                            finally
                            {
                                // key.cancel();
                            }
                        }
                    }
                    else if( watched instanceof WatchedSingleFile )
                    {
                        LOG.trace( "Single File changed {}", watched.path().toAbsolutePath() );
                        sourceChanged = true;
                        if( !exists( watched.path().getParent() ) )
                        {
                            try
                            {
                                registerAbsent( watched.path(), watchService, keys );
                            }
                            catch( IOException ex )
                            {
                                LOG.warn(
                                    "Unable to watch absent '{}' on single-file watch parent deletion: {}",
                                    watched.path(), ex.getMessage(), ex
                                );
                            }
                            finally
                            {
                                // key.cancel();
                            }
                        }
                    }
                    else if( watched instanceof WatchedAbsent )
                    {
                        LOG.trace( "Absent File upstream changed {}", watched.path().toAbsolutePath() );
                        if( exists( watched.path() ) )
                        {
                            // Created
                            sourceChanged = true;
                            if( isRegularFile( watched.path(), NOFOLLOW_LINKS ) )
                            {
                                try
                                {
                                    registerSingleFile( watched.path(), watchService, keys );
                                    LOG.trace( "Watching newly created (was absent) single file {}", watched.path() );
                                    // key.cancel();
                                }
                                catch( IOException ex )
                                {
                                    LOG.warn(
                                        "Unable to watch newly created (was absent) single file: {}",
                                        ex.getMessage(), ex
                                    );
                                }
                            }
                            else if( isDirectory( watched.path(), NOFOLLOW_LINKS ) )
                            {
                                try
                                {
                                    registerDirectory( watched.path(), watchService, keys );
                                    LOG.trace( "Watching newly created (was absent) directory {}", watched.path() );
                                    // key.cancel();
                                }
                                catch( IOException ex )
                                {
                                    LOG.warn(
                                        "Unable to watch newly created (was absent) directory: {}",
                                        ex.getMessage(), ex
                                    );
                                }
                            }
                            else
                            {
                                LOG.warn(
                                    "Absent File '" + watched.path()
                                    + "' created but it is neither a directory nor a regular file, ignoring."
                                );
                            }
                        }
                        else
                        {
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
                                Path child = ( (WatchedAbsent) watched ).upstream().resolve( name );

                                if( !watched.path().startsWith( child ) )
                                {
                                    // Ignore non relevant event, not in the target path
                                    continue;
                                }

                                if( kind == ENTRY_CREATE && isDirectory( child, NOFOLLOW_LINKS ) )
                                {
                                    try
                                    {
                                        registerAbsent( watched.path(), watchService, keys );
                                        LOG.trace(
                                            "Watching newly created upstream path of absent {}", watched.path()
                                        );
                                        // key.cancel();
                                    }
                                    catch( IOException ex )
                                    {
                                        LOG.warn(
                                            "Unable to update absent watch after path component creation: {}",
                                            ex.getMessage(), ex
                                        );
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new InternalError(
                            "Something is wrong with " + JavaWatcher.class.getName() + " codebase, please report!"
                        );
                    }
                }

                // Notify source change
                if( sourceChanged )
                {
                    listener.onChange();
                }

                // reset key and remove from set if invalid
                boolean valid = key.reset();
                if( !valid )
                {
                    keys.remove( key );
//                    for( Watched watched : watcheds )
//                    {
//                        if( !exists( watched.path() ) )
//                        {
//                            try
//                            {
//                                registerAbsent( watched.path(), watchService, keys );
//                                LOG.trace( "Deleted {}, now watching as absent", watched.path() );
//                            }
//                            catch( IOException ex )
//                            {
//                                LOG.warn(
//                                    "Unable to watch '{}' as absent, just got deleted: {}",
//                                    watched.path(), ex.getMessage(), ex
//                                );
//                            }
//                            sourceChanged = true;
//                        }
//                    }
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
                    throw new DevShellStartException(
                        "Cannot watch '" + start + "', it is neither a file nor a directory."
                    );
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
        throws IOException
    {
        Path upstream = findUpstream( absent );
        WatchKey key = upstream.register( watchService, WATCHED_EVENT_KINDS, WATCHED_MODIFIERS );
        keys.add( key, new WatchedAbsent( absent, upstream ) );
        LOG.trace( "Registered a watch on absent {} starting at upstream {}", absent, upstream );
    }

    private static Path findUpstream( Path path )
    {
        Path upstream = path;
        while( ( upstream = upstream.getParent() ) != null )
        {
            if( exists( upstream ) )
            {
                if( isDirectory( upstream, NOFOLLOW_LINKS ) )
                {
                    return upstream;
                }
                throw new DevShellStartException(
                    "Cannot watch absent '" + path + "' for changes, it has an existing non-directory parent!"
                );
            }
        }
        throw new InternalError( "Cannot watch '" + path + "' for changes, is has no root!" );
    }
}
