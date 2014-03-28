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
package org.qiweb.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.util.Strings;
import org.qiweb.devshell.DevShell;
import org.qiweb.devshell.JavaWatcher;
import org.qiweb.devshell.QiWebDevShellException;
import org.qiweb.runtime.CryptoInstance;
import org.qiweb.runtime.util.ClassLoaders;
import org.qiweb.spi.dev.DevShellSPI.SourceWatcher;
import org.qiweb.spi.dev.DevShellSPIAdapter;

import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Damn Small QiWeb DevShell.
 */
public final class DamnSmallDevShell
{
    private static final class SPI
        extends DevShellSPIAdapter
    {
        private final URL[] applicationClasspath;
        private final URL[] runtimeClasspath;
        private final Set<File> sources;
        private final File classesDir;

        private SPI(
            URL[] applicationClasspath,
            URL[] runtimeClasspath,
            Set<File> sources,
            SourceWatcher watcher,
            File classesDir )
        {
            super( applicationClasspath, runtimeClasspath, sources, watcher );
            this.applicationClasspath = applicationClasspath;
            this.runtimeClasspath = runtimeClasspath;
            this.sources = sources;
            this.classesDir = classesDir;
        }

        @Override
        protected void doRebuild()
        {
            try
            {
                DamnSmallDevShell.rebuild( applicationClasspath, runtimeClasspath, sources, classesDir );
            }
            catch( Exception ex )
            {
                throw new QiWebException( ex.getMessage(), ex );
            }
        }
    }

    private static final class ShutdownHook
        implements Runnable
    {
        private final DevShell devShell;
        private final File tmpDir;

        private ShutdownHook( DevShell devShell, File tmpDir )
        {
            this.devShell = devShell;
            this.tmpDir = tmpDir;
        }

        @Override
        public void run()
        {
            devShell.stop();
            try
            {
                FileUtils.deleteDirectory( tmpDir );
            }
            catch( IOException ex )
            {
                ex.printStackTrace( System.err );
            }
        }
    }
    // figlet -f rectangles  "QiWeb DevShell"
    private static final String LOGO;

    static
    {
        LOGO
        = ""
          + " _____ _ _ _ _     _      ____          _____ _       _ _ \n"
          + "|     |_| | | |___| |_   |    \\ ___ _ _|   __| |_ ___| | |\n"
          + "|  |  | | | | | -_| . |  |  |  | -_| | |__   |   | -_| | |\n"
          + "|__  _|_|_____|___|___|  |____/|___|\\_/|_____|_|_|___|_|_|\n"
          + "   |__|                                "
          + "QiWeb v" + BuildVersion.VERSION + "-" + BuildVersion.COMMIT + ( BuildVersion.DIRTY ? " (DIRTY)" : "" )
          + "\n";
    }

    public static void main( String[] args )
    {
        Options options = declareOptions();

        CommandLineParser parser = new PosixParser();
        try
        {
            CommandLine cmd = parser.parse( options, args );

            // Handle --help
            if( cmd.hasOption( "help" ) )
            {
                PrintWriter out = new PrintWriter( System.out );
                printHelp( options, out );
                out.flush();
                System.exit( 0 );
            }

            // Handle --version
            if( cmd.hasOption( "version" ) )
            {
                System.out.println(
                    "QiWeb CLI v" + BuildVersion.VERSION + "\n"
                    + "Git commit: " + BuildVersion.COMMIT + ( BuildVersion.DIRTY ? " (DIRTY)" : "" ) + ", built on: " + BuildVersion.DATE + "\n"
                    + "Licence: Apache License Version 2.0, http://www.apache.org/licenses/LICENSE-2.0\n"
                    + "Java version: " + System.getProperty( "java.version" ) + ", vendor: " + System.getProperty( "java.vendor" ) + "\n"
                    + "Java home: " + System.getProperty( "java.home" ) + "\n"
                    + "Default locale: " + Locale.getDefault().toString() + ", platform encoding: " + System.getProperty( "file.encoding" ) + "\n"
                    + "OS name: " + System.getProperty( "os.name" ) + ", version: " + System.getProperty( "os.version" ) + ", arch: " + System.getProperty( "os.arch" ) );
                System.exit( 0 );
            }

            // Debug
            final boolean debug = cmd.hasOption( 'd' );

            // Temporary directory
            final File tmpDir = new File( cmd.getOptionValue( 't', "build/devshell.tmp" ) );
            if( debug )
            {
                System.out.println( "Temporary directory set to '" + tmpDir.getAbsolutePath() + "'." );
            }

            // Handle commands
            @SuppressWarnings( "unchecked" )
            List<String> commands = cmd.getArgList();
            if( commands.isEmpty() )
            {
                commands = Collections.singletonList( "run" );
            }
            if( debug )
            {
                System.out.println( "Commands to be executed: " + commands );
            }
            Iterator<String> commandsIterator = commands.iterator();
            while( commandsIterator.hasNext() )
            {
                String command = commandsIterator.next();
                switch( command )
                {
                    case "new":
                        System.out.println( LOGO );
                        newCommand( commandsIterator.hasNext() ? commandsIterator.next() : "qiweb-application", cmd );
                        break;
                    case "clean":
                        cleanCommand( debug, tmpDir );
                        break;
                    case "run":
                        System.out.println( LOGO );
                        runCommand( debug, tmpDir, cmd );
                        break;
                    case "secret":
                        secretCommand();
                        break;
                    default:
                        PrintWriter out = new PrintWriter( System.err );
                        System.err.println( "Unknown command: '" + command + "'" );
                        printHelp( options, out );
                        out.flush();
                        System.exit( 1 );
                        break;
                }
            }
        }
        catch( IllegalArgumentException | ParseException | IOException ex )
        {
            PrintWriter out = new PrintWriter( System.err );
            printHelp( options, out );
            out.flush();
            System.exit( 1 );
        }
        catch( QiWebException ex )
        {
            ex.printStackTrace( System.err );
            System.err.flush();
            System.exit( 1 );
        }
    }

    private static void newCommand( String name, CommandLine cmd )
        throws IOException
    {
        File baseDir = new File( name );
        File ctrlDir = new File( baseDir, "src/main/java/controllers" );
        File rsrcDir = new File( baseDir, "src/main/resources" );
        Files.createDirectories( ctrlDir.toPath() );
        Files.createDirectories( rsrcDir.toPath() );

        // Generate secret
        String conf = "\napp.secret = " + CryptoInstance.genRandom256bitsHexSecret() + "\n";
        Files.write( new File( rsrcDir, "application.conf" ).toPath(), conf.getBytes( UTF_8 ) );

        // Generate controller
        String controller = "package controllers;\n\n"
                            + "import org.qiweb.api.outcomes.Outcome;\n\n"
                            + "public class Application {\n\n"
                            + "    public Outcome index() {\n"
                            + "        return new org.qiweb.runtime.controllers.Welcome().welcome();\n"
                            + "    }\n\n"
                            + "}\n";
        Files.write( new File( ctrlDir, "Application.java" ).toPath(), controller.getBytes( UTF_8 ) );

        // Generate routes
        String routes = "\nGET / controllers.Application.index\n";
        Files.write( new File( rsrcDir, "routes.conf" ).toPath(), routes.getBytes( UTF_8 ) );

        // Inform user
        System.out.println( "New QiWeb Application generated in '" + baseDir.getAbsolutePath() + "'." );
    }

    private static void cleanCommand( boolean debug, File tmpDir )
    {
        try
        {
            // Clean
            FileUtils.deleteDirectory( tmpDir );
            // Inform user
            System.out.println(
                "Temporary files " + ( debug ? "in '" + tmpDir.getAbsolutePath() + "' " : "" ) + "deleted."
            );
        }
        catch( IOException ex )
        {
            ex.printStackTrace( System.err );
        }
    }

    private static void runCommand( boolean debug, File tmpDir, CommandLine cmd )
        throws IOException
    {
        // Classes directory
        final File classesDir = new File( tmpDir, "classes" );
        Files.createDirectories( classesDir.toPath() );
        if( debug )
        {
            System.out.println( "Classes directory is: " + classesDir.getAbsolutePath() );
        }

        // Sources
        String[] sourcesPaths = cmd.hasOption( 's' ) ? cmd.getOptionValues( 's' ) : new String[]
        {
            "src/main/java",
            "src/main/resources"
        };
        Set<File> sources = new LinkedHashSet<>();
        for( String sourcePath : sourcesPaths )
        {
            sources.add( new File( sourcePath ) );
        }
        if( debug )
        {
            System.out.println( "Sources directories are: " + sources );
        }

        // Classpath
        List<URL> classpathList = new ArrayList<>();
        // First, current classpath
        classpathList.addAll( ClassLoaders.urlsOf( DamnSmallDevShell.class.getClassLoader() ) );
        // Then add command line provided
        if( cmd.hasOption( 'c' ) )
        {
            for( String url : cmd.getOptionValues( 'c' ) )
            {
                classpathList.add( new URL( url ) );
            }
        }
        // Append Application sources
        for( File source : sources )
        {
            classpathList.add( source.toURI().toURL() );
        }
        URL[] runtimeClasspath = classpathList.toArray( new URL[ classpathList.size() ] );
        if( debug )
        {
            System.out.println( "Runtime Classpath is: " + classpathList );
        }
        // Then Application classpath
        URL[] applicationClasspath = new URL[]
        {
            classesDir.toURI().toURL()
        };
        if( debug )
        {
            System.out.println( "Application Classpath is: " + Arrays.toString( applicationClasspath ) );
        }

        // Apply System Properties
        Properties systemProperties = cmd.getOptionProperties( "D" );
        for( Iterator<Map.Entry<Object, Object>> it = systemProperties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<?, ?> entry = it.next();
            System.setProperty( entry.getKey().toString(), entry.getValue().toString() );
        }
        if( debug )
        {
            System.out.println( "Applied System Properties are: " + systemProperties );
        }

        System.out.println( "Loading..." );

        // Watch Sources
        SourceWatcher watcher = new JavaWatcher();

        // First build
        rebuild( applicationClasspath, runtimeClasspath, sources, classesDir );

        // Run DevShell
        final DevShell devShell = new DevShell(
            new SPI( applicationClasspath, runtimeClasspath, sources, watcher, classesDir )
        );
        Runtime.getRuntime().addShutdownHook(
            new Thread( new ShutdownHook( devShell, tmpDir ), "qiweb-devshell-shutdown" )
        );
        devShell.start();
    }

    private static void secretCommand()
    {
        System.out.println( CryptoInstance.genRandom256bitsHexSecret() );
    }
    private static final DefaultExecutor EXECUTOR = new DefaultExecutor();

    /* package */ static void rebuild(
        URL[] applicationClasspath, URL[] runtimeClasspath, Set<File> sources, File classesDir
    )
    {
        System.out.println( "Compiling Application..." );
        ByteArrayOutputStream javacOutput = new ByteArrayOutputStream();
        try
        {
            // Collect java files
            ByteArrayOutputStream findJavaOutput = new ByteArrayOutputStream();
            for( File source : sources )
            {
                if( source.exists() )
                {
                    org.apache.commons.exec.CommandLine findJava = new org.apache.commons.exec.CommandLine( "find" );
                    findJava.addArgument( source.getAbsolutePath() );
                    findJava.addArgument( "-type" );
                    findJava.addArgument( "f" );
                    findJava.addArgument( "-iname" );
                    findJava.addArgument( "*.java" );

                    EXECUTOR.setStreamHandler( new PumpStreamHandler( findJavaOutput ) );
                    EXECUTOR.execute( findJava );
                }
            }
            // Write list in a temporary file
            String javaFiles = findJavaOutput.toString( "UTF-8" );
            if( !Strings.isEmpty( javaFiles ) )
            {
                File javaListFile = new File( classesDir, ".devshell-java-list" );
                try( FileWriter writer = new FileWriter( javaListFile ) )
                {
                    writer.write( javaFiles );
                    writer.close();
                }
                // Compile
                org.apache.commons.exec.CommandLine javac = new org.apache.commons.exec.CommandLine( "javac" );
                javac.addArgument( "-encoding" );
                javac.addArgument( "UTF-8" );
                javac.addArgument( "-source" );
                javac.addArgument( "1.7" );
                javac.addArgument( "-d" );
                javac.addArgument( classesDir.getAbsolutePath() );
                javac.addArgument( "-classpath" );
                String[] classpathStrings = new String[ runtimeClasspath.length ];
                for( int idx = 0; idx < runtimeClasspath.length; idx++ )
                {
                    classpathStrings[idx] = runtimeClasspath[idx].toURI().toASCIIString();
                }
                javac.addArgument( Strings.join( classpathStrings, ":" ) );
                javac.addArgument( "@" + javaListFile.getAbsolutePath() );

                EXECUTOR.setStreamHandler( new PumpStreamHandler( javacOutput ) );
                EXECUTOR.execute( javac );
            }
        }
        catch( IOException | URISyntaxException ex )
        {
            String output = javacOutput.toString(); // utf-8
            throw new QiWebDevShellException(
                "Unable to rebuild" + ( Strings.isEmpty( output ) ? "" : "\n" + output ),
                ex
            );
        }
    }

    @SuppressWarnings( "static-access" )
    private static Options declareOptions()
    {
        Option classpathOption = OptionBuilder
            .withArgName( "element" )
            .hasArgs()
            .withDescription(
                "Set application classpath element. "
                + "Use this option several times to declare a full classpath. "
            )
            .withLongOpt( "classpath" )
            .create( 'c' );

        Option sourcesOption = OptionBuilder
            .withArgName( "directory" )
            .hasArgs()
            .withDescription(
                "Set application sources directories. "
                + "Use this option several times to declare multiple sources directories. "
                + "Defaults to 'src/main/java' and 'src/main/resources' in current directory."
            )
            .withLongOpt( "sources" )
            .create( 's' );

        Option tmpdirOption = OptionBuilder
            .withArgName( "directory" )
            .hasArgs()
            .withDescription( "Set temporary directory. Defaults to 'build/devshell.tmp' in current directory." )
            .withLongOpt( "tmpdir" )
            .create( 't' );

        Option propertiesOption = OptionBuilder.withArgName( "property=value" )
            .hasArgs( 2 )
            .withValueSeparator()
            .withDescription(
                "Define a system property. "
                + "Use this option several times to define multiple system properties. "
                + "Particularly convenient when used to override application configuration."
            )
            .withLongOpt( "define" )
            .create( 'D' );

        Option debugOption = OptionBuilder
            .withDescription( "Enable debug output." )
            .withLongOpt( "debug" )
            .create( 'd' );

        Option versionOption = OptionBuilder
            .withDescription( "Display version information." )
            .withLongOpt( "version" )
            .create();

        Option helpOption = OptionBuilder
            .withDescription( "Display help information." )
            .withLongOpt( "help" )
            .create();

        Options options = new Options();
        options.addOption( classpathOption );
        options.addOption( sourcesOption );
        options.addOption( tmpdirOption );
        options.addOption( propertiesOption );
        options.addOption( debugOption );
        options.addOption( versionOption );
        options.addOption( helpOption );
        return options;
    }

    private static final class OptionsComparator
        implements Comparator<Option>
    {
        private static final List<String> OPTIONS_ORDER = Arrays.asList( new String[]
        {
            "classpath",
            "sources",
            "tmpdir",
            "define",
            "debug",
            "version",
            "help",
        } );

        @Override
        public int compare( Option o1, Option o2 )
        {
            Integer o1idx = OPTIONS_ORDER.indexOf( o1.getLongOpt() );
            Integer o2idx = OPTIONS_ORDER.indexOf( o2.getLongOpt() );
            return o1idx.compareTo( o2idx );
        }
    }
    private static final int WIDTH = 80;

    private static void printHelp( Options options, PrintWriter out )
    {
        HelpFormatter help = new HelpFormatter();
        help.setOptionComparator( new OptionsComparator() );
        help.printUsage( out, WIDTH, "org.qiweb.cli [options] [command(s)]" );
        out.print(
            "\n"
            + "  The Damn Small QiWeb DevShell\n"
            + "  - do not manage dependencies ;\n"
            + "  - do not allow you to extend the build ;\n"
            + "  - do not assemble applications.\n"
        );
        help.printWrapped(
            out, WIDTH, 2,
            "\n"
            + "Meaning you have to manage your application dependencies and assembly yourself. "
            + "Theses limitations make this DevShell suitable for quick prototyping only. "
            + "Prefer the Gradle or Maven build systems integration."
        );
        out.println(
            "\n  org.qiweb.cli is part of the QiWeb Development Kit - http://qiweb.org"
        );
        out.println(
            "\n"
            + "Commands:\n\n"
            + "  new <appdir>  Create a new skeleton application in the 'appdir' directory.\n"
            + "  secret        Generate a new application secret.\n"
            + "  clean         Delete devshell temporary directory, see 'tmpdir' option.\n"
            + "  run           Run the QiWeb Development Shell.\n"
            + "\n"
            + "  If no command is specified, 'run' is assumed."
        );
        out.println(
            "\n"
            + "Options:"
            + "\n"
        );
        help.printOptions( out, WIDTH, options, 2, 2 );
        help.printWrapped(
            out, WIDTH, 2,
            "\n"
            + "All paths are relative to the current working directory, "
            + "except if they are absolute of course."
        );
        help.printWrapped(
            out, WIDTH, 2,
            "\n"
            + "Licensed under the Apache License Version 2.0, http://www.apache.org/licenses/LICENSE-2.0"
        );
        out.println();
    }

    private DamnSmallDevShell()
    {
    }
}
