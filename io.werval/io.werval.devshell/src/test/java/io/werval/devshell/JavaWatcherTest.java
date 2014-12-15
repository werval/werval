/*
 * Copyright (c) 2014 the original author or authors
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
package io.werval.devshell;

import io.werval.spi.dev.DevShellSPI.SourceChangeListener;
import io.werval.spi.dev.DevShellSPI.SourceWatch;
import io.werval.util.DeltreeFileVisitor;
import io.werval.test.util.Slf4jRule;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.jayway.awaitility.Awaitility.await;
import static io.werval.util.Strings.indentTwoSpaces;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * JavaWatcher Test.
 */
public class JavaWatcherTest
{
    @BeforeClass
    public static void assumeNotTravis()
    {
        // This test is disabled on Travis as it is too fragile (Thread.sleeps ...)
        Assume.assumeTrue( System.getenv("TRAVIS") == null );
    }

    private static final long SLEEP_TIME = 2000;

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public final Slf4jRule slf4j = new Slf4jRule()
    {
        {
            record( Level.TRACE );
            recordForType( JavaWatcher.class );
        }
    };

    private volatile boolean changed;
    private final SourceChangeListener changed_l = new SourceChangeListener()
    {
        @Override
        public void onChange()
        {
            changed = true;
        }
    };
    private final Callable<Boolean> changed_c = new Callable<Boolean>()
    {
        @Override
        public Boolean call()
            throws Exception
        {
            return changed;
        }
    };

    @Before
    public void notChanged()
    {
        changed = false;
    }

    private class Check
        implements AutoCloseable
    {
        private final Path path;

        public Check( Path path )
        {
            this.path = path;
            slf4j.clear();
        }

        @Override
        public void close()
        {
            System.out.println( "Logs of CheckBlock for " + path );
            for( String log : slf4j.allStatements() )
            {
                System.out.println( indentTwoSpaces( log, 2 ) );
            }
            assertThat( slf4j.contains( path.toAbsolutePath().toString() ), is( true ) );
            changed = false;
        }
    }

    @Test
    public void watchExistingDirectory()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        SourceWatch watch = new JavaWatcher().watch( singleton( root.toFile() ), changed_l );
        try
        {
            assertThat( changed, is( false ) );

            Path file = root.resolve( "file" );
            Path subdir = root.resolve( "subdir" );
            Path subfile = subdir.resolve( "subfile" );

            try( Check block = new Check( file ) )
            {
                createFile( file );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( subdir ) )
            {
                createDirectories( subdir );
                await().until( changed_c, is( true ) );
                assertTrue( slf4j.contains( "newly created directory " + subdir.toAbsolutePath() ) );
            }

            try( Check block = new Check( subfile ) )
            {
                createFile( subfile );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( subfile ) )
            {
                delete( subfile );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( subdir ) )
            {
                walkFileTree( subdir, new DeltreeFileVisitor() );
                await().until( changed_c, is( true ) );
                // assertTrue( slf4j.contains( "delete" ) ); // Fail on Linux
            }

            try( Check block = new Check( file ) )
            {
                delete( file );
                await().until( changed_c, is( true ) );
            }
        }
        finally
        {
            watch.unwatch();
        }
    }

    @Test
    public void watchExistingSingleFile()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        Path singleFile = root.resolve( "single-file" );
        Path otherFile = root.resolve( "other-file" );

        createFile( singleFile );
        SourceWatch watch = new JavaWatcher().watch( singleton( singleFile.toFile() ), changed_l );
        Thread.sleep( SLEEP_TIME );
        try
        {
            assertThat( changed, is( false ) );

            try( Check block = new Check( singleFile ) )
            {
                write( singleFile, "CHANGED".getBytes(), WRITE, SYNC );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( singleFile ) )
            {
                delete( singleFile );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( singleFile ) )
            {
                createFile( singleFile );
                await().until( changed_c, is( true ) );
            }

            createFile( otherFile );
            Thread.sleep( SLEEP_TIME );
            assertFalse( changed );
        }
        finally
        {
            watch.unwatch();
        }
    }

    @Test
    public void watchAbsentDirectory()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        Path dir = root.resolve( "dir" );
        Path file = dir.resolve( "file" );
        SourceWatch watch = new JavaWatcher().watch( singleton( dir.toFile() ), changed_l );
        Thread.sleep( SLEEP_TIME );
        try
        {
            assertThat( changed, is( false ) );

            try( Check block = new Check( dir ) )
            {
                createDirectories( dir );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( dir ) )
            {
                createFile( file );
                await().until( changed_c, is( true ) );
            }
        }
        finally
        {
            watch.unwatch();
        }
    }

    @Test
    public void watchAbsentSingleFile()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        Path singleFile = root.resolve( "single-file" );
        SourceWatch watch = new JavaWatcher().watch( singleton( singleFile.toFile() ), changed_l );
        Thread.sleep( SLEEP_TIME );
        try
        {
            assertThat( changed, is( false ) );

            try( Check block = new Check( singleFile ) )
            {
                createFile( singleFile );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( singleFile ) )
            {
                write( singleFile, "CHANGED".getBytes() );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( singleFile ) )
            {
                delete( singleFile );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( singleFile ) )
            {
                createFile( singleFile );
                await().until( changed_c, is( true ) );
            }
        }
        finally
        {
            watch.unwatch();
        }
    }

    @Test
    public void watchAbsentDeepPath()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        Path dir = root.resolve( "dir" );
        Path subdir = root.resolve( "subdir" );
        SourceWatch watch = new JavaWatcher().watch( singleton( subdir.toFile() ), changed_l );
        Thread.sleep( SLEEP_TIME );
        try
        {
            assertThat( changed, is( false ) );

            createDirectories( dir );
            Thread.sleep( SLEEP_TIME );
            assertFalse( changed );

            try( Check block = new Check( subdir ) )
            {
                createDirectories( subdir );
                await().until( changed_c, is( true ) );
            }
        }
        finally
        {
            watch.unwatch();
        }
    }

    @Test
    public void watchPresentAbsentPresentSingleFile()
        throws Exception
    {
        Path root = tmp.getRoot().toPath().resolve( "root" );
        createDirectories( root );
        SourceWatch watch = new JavaWatcher().watch( singleton( root.toFile() ), changed_l );
        try
        {
            assertThat( changed, is( false ) );

            Path file = root.resolve( "file" );

            try( Check block = new Check( root ) )
            {
                createFile( file );
                await().until( changed_c, is( true ) );
            }

            try( Check block = new Check( root ) )
            {
                walkFileTree( root, new DeltreeFileVisitor() );
                await().until( changed_c, is( true ) );
                // assertTrue( slf4j.contains( "delete" ) ); // Fail on Linux
            }

            try( Check block = new Check( root ) )
            {
                createDirectories( root );
                createFile( file );
                await().until( changed_c, is( true ) );
            }
        }
        finally
        {
            watch.unwatch();
        }
    }
}
