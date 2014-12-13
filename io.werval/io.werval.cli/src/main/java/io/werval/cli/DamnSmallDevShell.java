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
package io.werval.cli;

import io.werval.api.exceptions.WervalException;
import io.werval.commands.DevShellCommand;
import io.werval.commands.SecretCommand;
import io.werval.commands.StartCommand;
import io.werval.devshell.JavaWatcher;
import io.werval.runtime.CryptoInstance;
import io.werval.spi.dev.DevShellRebuildException;
import io.werval.spi.dev.DevShellSPI.SourceWatcher;
import io.werval.spi.dev.DevShellSPIAdapter;
import io.werval.util.ClassLoaders;
import io.werval.util.DeltreeFileVisitor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
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

import static io.werval.cli.BuildVersion.COMMIT;
import static io.werval.cli.BuildVersion.DATE;
import static io.werval.cli.BuildVersion.DIRTY;
import static io.werval.cli.BuildVersion.VERSION;
import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.readAllAsString;
import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.NEWLINE;
import static io.werval.util.Strings.hasText;
import static io.werval.util.Strings.isEmpty;
import static io.werval.util.Strings.join;
import static java.io.File.separator;

/**
 * Damn Small Werval DevShell.
 */
public final class DamnSmallDevShell
{
    private static final class SPI
        extends DevShellSPIAdapter
    {
        private final URL[] applicationClasspath;
        private final URL[] runtimeClasspath;
        private final Set<File> sourcesRoots;
        private final File classesDir;

        private SPI(
            URL[] applicationSources,
            URL[] applicationClasspath,
            URL[] runtimeClasspath,
            Set<File> sourcesRoots,
            SourceWatcher watcher,
            File classesDir )
        {
            super( applicationSources, applicationClasspath, runtimeClasspath, sourcesRoots, watcher, false );
            this.applicationClasspath = applicationClasspath;
            this.runtimeClasspath = runtimeClasspath;
            this.sourcesRoots = sourcesRoots;
            this.classesDir = classesDir;
        }

        @Override
        protected void doRebuild()
        {
            try
            {
                DamnSmallDevShell.rebuild( applicationClasspath, runtimeClasspath, sourcesRoots, classesDir );
            }
            catch( Exception ex )
            {
                throw new DevShellRebuildException( ex );
            }
        }
    }

    private static final class ShutdownHook
        implements Runnable
    {
        private final File tmpDir;

        private ShutdownHook( File tmpDir )
        {
            this.tmpDir = tmpDir;
        }

        @Override
        public void run()
        {
            try
            {
                if( tmpDir.exists() )
                {
                    Files.walkFileTree( tmpDir.toPath(), new DeltreeFileVisitor() );
                }
            }
            catch( IOException ex )
            {
                ex.printStackTrace( System.err );
            }
        }
    }
    // figlet -f rectangles  "Werval DevShell"
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
          + "Werval v" + VERSION + "-" + COMMIT + ( DIRTY ? " (DIRTY)" : "" )
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
                System.out.print(
                    String.format(
                        "Werval CLI v%s\n"
                        + "Git commit: %s%s, built on: %s\n"
                        + "Java version: %s, vendor: %s\n"
                        + "Java home: %s\n"
                        + "Default locale: %s, platform encoding: %s\n"
                        + "OS name: %s, version: %s, arch: %s\n",
                        VERSION,
                        COMMIT,
                        ( DIRTY ? " (DIRTY)" : "" ),
                        DATE,
                        System.getProperty( "java.version" ),
                        System.getProperty( "java.vendor" ),
                        System.getProperty( "java.home" ),
                        Locale.getDefault().toString(),
                        System.getProperty( "file.encoding" ),
                        System.getProperty( "os.name" ),
                        System.getProperty( "os.version" ),
                        System.getProperty( "os.arch" )
                    )
                );
                System.out.flush();
                System.exit( 0 );
            }

            // Debug
            final boolean debug = cmd.hasOption( 'd' );

            // Temporary directory
            final File tmpDir = new File( cmd.getOptionValue( 't', "build" + separator + "devshell.tmp" ) );
            if( debug )
            {
                System.out.println( "Temporary directory set to '" + tmpDir.getAbsolutePath() + "'." );
            }

            // Handle commands
            @SuppressWarnings( "unchecked" )
            List<String> commands = cmd.getArgList();
            if( commands.isEmpty() )
            {
                commands = Collections.singletonList( "start" );
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
                        newCommand( commandsIterator.hasNext() ? commandsIterator.next() : "werval-application", cmd );
                        break;
                    case "clean":
                        cleanCommand( debug, tmpDir );
                        break;
                    case "devshell":
                        System.out.println( LOGO );
                        devshellCommand( debug, tmpDir, cmd );
                        break;
                    case "start":
                        System.out.println( LOGO );
                        startCommand( debug, tmpDir, cmd );
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
        catch( WervalException ex )
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
        File ctrlDir = new File(
            baseDir,
            "src" + separator + "main" + separator + "java" + separator + "controllers"
        );
        File rsrcDir = new File(
            baseDir,
            "src" + separator + "main" + separator + "resources"
        );
        Files.createDirectories( ctrlDir.toPath() );
        Files.createDirectories( rsrcDir.toPath() );

        // Generate secret
        String conf = "\napp.secret = " + CryptoInstance.newRandomSecret256BitsHex() + "\n";
        Files.write( new File( rsrcDir, "application.conf" ).toPath(), conf.getBytes( UTF_8 ) );

        // Generate controller
        String controller = "package controllers;\n\n"
                            + "import io.werval.api.outcomes.Outcome;\n\n"
                            + "public class Application {\n\n"
                            + "    public Outcome index() {\n"
                            + "        return new io.werval.controllers.Welcome().welcome();\n"
                            + "    }\n\n"
                            + "}\n";
        Files.write( new File( ctrlDir, "Application.java" ).toPath(), controller.getBytes( UTF_8 ) );

        // Generate routes
        String routes = "\nGET / controllers.Application.index\n";
        Files.write( new File( rsrcDir, "routes.conf" ).toPath(), routes.getBytes( UTF_8 ) );

        // Generate Gradle build file
        String gradle = "buildscript {\n"
                        + "  repositories { maven { url 'https://repo.codeartisans.org/werval' } }\n"
                        + "	 dependencies { classpath 'org.qiweb:io.werval.gradle:" + VERSION + "' }\n"
                        + "}\n"
                        + "repositories { maven { url 'https://repo.codeartisans.org/werval' } }\n"
                        + "\n"
                        + "apply plugin: 'io.werval.application'\n"
                        + "\n"
                        + "applicationName = '" + name + "'\n"
                        + "\n"
                        + "dependencies {\n"
                        + "\n"
                        + "    // Add application compile dependencies here\n"
                        + "\n"
                        + "    runtime 'ch.qos.logback:logback-classic:1.1.2'\n"
                        + "    // Add application runtime dependencies here\n"
                        + "\n"
                        + "    // Add application test dependencies here\n"
                        + "\n"
                        + "}\n"
                        + "";
        Files.write( new File( baseDir, "build.gradle.example" ).toPath(), gradle.getBytes( UTF_8 ) );

        // Generate Maven POM file
        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                     + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                     + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
                     + "    <modelVersion>4.0.0</modelVersion>\n"
                     + "\n"
                     + "    <groupId>" + name + "</groupId>\n"
                     + "    <artifactId>" + name + "</artifactId>\n"
                     + "    <version>" + VERSION + "</version>\n"
                     + "\n"
                     + "    <properties>\n"
                     + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                     + "        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>\n"
                     + "    </properties>\n"
                     + "\n"
                     + "    <repositories>\n"
                     + "        <repository>\n"
                     + "            <id>wervalRepo</id>\n"
                     + "            <url>https://repo.codeartisans.org/werval</url>\n"
                     + "        </repository>\n"
                     + "    </repositories>\n"
                     + "\n"
                     + "    <dependencies>\n"
                     + "\n"
                     + "        <dependency>\n"
                     + "            <groupId>org.qiweb</groupId>\n"
                     + "            <artifactId>io.werval.api</artifactId>\n"
                     + "            <version>" + VERSION + "</version>\n"
                     + "        </dependency>\n"
                     + "        <!-- Add application compile dependencies here -->\n"
                     + "\n"
                     + "        <dependency>\n"
                     + "            <groupId>org.qiweb</groupId>\n"
                     + "            <artifactId>io.werval.server.bootstrap</artifactId>\n"
                     + "            <version>" + VERSION + "</version>\n"
                     + "            <scope>runtime</scope>\n"
                     + "        </dependency>\n"
                     + "        <dependency>\n"
                     + "            <groupId>ch.qos.logback</groupId>\n"
                     + "            <artifactId>logback-classic</artifactId>\n"
                     + "            <version>1.1.2</version>\n"
                     + "            <scope>runtime</scope>\n"
                     + "        </dependency>\n"
                     + "        <!-- Add application runtime dependencies here -->\n"
                     + "\n"
                     + "        <dependency>\n"
                     + "            <groupId>org.qiweb</groupId>\n"
                     + "            <artifactId>io.werval.test</artifactId>\n"
                     + "            <version>" + VERSION + "</version>\n"
                     + "            <scope>test</scope>\n"
                     + "        </dependency>\n"
                     + "        <!-- Add application test dependencies here -->\n"
                     + "\n"
                     + "    </dependencies>\n"
                     + "\n"
                     + "    <pluginRepositories>\n"
                     + "        <pluginRepository>\n"
                     + "            <id>wervalRepo</id>\n"
                     + "            <url>https://repo.codeartisans.org/werval</url>\n"
                     + "        </pluginRepository>\n"
                     + "    </pluginRepositories>\n"
                     + "\n"
                     + "    <build>\n"
                     + "        <plugins>\n"
                     + "            <plugin>\n"
                     + "                <artifactId>maven-compiler-plugin</artifactId>\n"
                     + "                <version>3.1</version>\n"
                     + "                <configuration>\n"
                     + "                    <source>1.8</source>\n"
                     + "                    <target>1.8</target>\n"
                     + "                </configuration>\n"
                     + "            </plugin>\n"
                     + "            <plugin>\n"
                     + "                <groupId>org.qiweb</groupId>\n"
                     + "                <artifactId>io.werval.maven</artifactId>\n"
                     + "                <version>" + VERSION + "</version>\n"
                     + "            </plugin>\n"
                     + "            <plugin>\n"
                     + "                <groupId>org.codehaus.mojo</groupId>\n"
                     + "                <artifactId>appassembler-maven-plugin</artifactId>\n"
                     + "                <version>1.8</version>\n"
                     + "                <executions>\n"
                     + "                    <execution>\n"
                     + "                        <id>app-assembly</id>\n"
                     + "                        <!-- Sample Packaging -->\n"
                     + "                        <phase>package</phase>\n"
                     + "                        <goals><goal>assemble</goal></goals>\n"
                     + "                        <configuration>\n"
                     + "                            <repositoryName>lib</repositoryName>\n"
                     + "                            <repositoryLayout>flat</repositoryLayout>\n"
                     + "                            <programs>\n"
                     + "                                <program>\n"
                     + "                                    <id>werval-sample-maven</id>\n"
                     + "                                    <mainClass>io.werval.server.bootstrap.Main</mainClass>\n"
                     + "                                </program>\n"
                     + "                            </programs>\n"
                     + "                        </configuration>\n"
                     + "                    </execution>\n"
                     + "                </executions>\n"
                     + "            </plugin>\n"
                     + "        </plugins>\n"
                     + "    </build>\n"
                     + "\n"
                     + "</project>\n";
        Files.write( new File( baseDir, "pom.xml.example" ).toPath(), pom.getBytes( UTF_8 ) );

        // Generate .gitignore
        String gitignore = "target\nbuild\n.devshell.lock\nbuild.gradle.example\npom.xml.example\n.gradle\n";
        Files.write( new File( baseDir, ".gitignore" ).toPath(), gitignore.getBytes( UTF_8 ) );

        // Inform user
        System.out.println( "New Werval Application generated in '" + baseDir.getAbsolutePath() + "'." );
    }

    private static void cleanCommand( boolean debug, File tmpDir )
    {
        try
        {
            // Clean
            if( tmpDir.exists() )
            {
                Files.walkFileTree( tmpDir.toPath(), new DeltreeFileVisitor() );
            }
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

    private static void devshellCommand( boolean debug, File tmpDir, CommandLine cmd )
        throws IOException
    {
        final File classesDir = createClassesDirectory( debug, tmpDir );
        Set<File> sourceRoots = prepareSourcesRoots( debug, cmd );
        Set<URL> applicationSourcesSet = new LinkedHashSet<>( sourceRoots.size() );
        for( File sourceRoot : sourceRoots )
        {
            applicationSourcesSet.add( sourceRoot.toURI().toURL() );
        }
        URL[] applicationSources = applicationSourcesSet.toArray( new URL[ applicationSourcesSet.size() ] );
        URL[] applicationClasspath = prepareApplicationClasspath( debug, classesDir );
        URL[] runtimeClasspath = prepareRuntimeClasspath( debug, sourceRoots, cmd );
        applySystemProperties( debug, cmd );
        System.out.println( "Loading..." );

        // Watch Sources
        SourceWatcher watcher = new JavaWatcher();

        // First build
        rebuild( applicationClasspath, runtimeClasspath, sourceRoots, classesDir );

        // Run DevShell
        Runtime.getRuntime().addShutdownHook( new Thread( new ShutdownHook( tmpDir ), "werval-cli-cleanup" ) );
        new DevShellCommand(
            new SPI( applicationSources, applicationClasspath, runtimeClasspath, sourceRoots, watcher, classesDir )
        ).run();
    }

    private static void startCommand( boolean debug, File tmpDir, CommandLine cmd )
        throws IOException, MalformedURLException
    {
        final File classesDir = createClassesDirectory( debug, tmpDir );
        Set<File> sourcesRoots = prepareSourcesRoots( debug, cmd );
        URL[] runtimeClasspath = prepareRuntimeClasspath( debug, sourcesRoots, cmd );
        URL[] applicationClasspath = prepareApplicationClasspath( debug, classesDir );
        applySystemProperties( debug, cmd );
        System.out.println( "Loading..." );

        // Build
        rebuild( applicationClasspath, runtimeClasspath, sourcesRoots, classesDir );

        // Start
        System.out.println( "Starting!" );
        Runtime.getRuntime().addShutdownHook( new Thread( new ShutdownHook( tmpDir ), "werval-cli-cleanup" ) );
        List<URL> globalClasspath = new ArrayList<>( Arrays.asList( runtimeClasspath ) );
        globalClasspath.addAll( Arrays.asList( applicationClasspath ) );
        new StartCommand(
            StartCommand.ExecutionModel.FORK,
            io.werval.server.bootstrap.Main.class.getName(),
            new String[ 0 ],
            globalClasspath.toArray( new URL[ globalClasspath.size() ] )
        ).run();
    }

    private static File createClassesDirectory( boolean debug, File tmpDir )
        throws IOException
    {
        final File classesDir = new File( tmpDir, "classes" );
        Files.createDirectories( classesDir.toPath() );
        if( debug )
        {
            System.out.println( "Classes directory is: " + classesDir.getAbsolutePath() );
        }
        return classesDir;
    }

    private static Set<File> prepareSourcesRoots( boolean debug, CommandLine cmd )
    {
        String[] sourcesPaths = cmd.hasOption( 's' ) ? cmd.getOptionValues( 's' ) : new String[]
        {
            "src" + separator + "main" + separator + "java",
            "src" + separator + "main" + separator + "resources"
        };
        Set<File> sourcesRoots = new LinkedHashSet<>();
        for( String sourceRoot : sourcesPaths )
        {
            sourcesRoots.add( new File( sourceRoot ) );
        }
        if( debug )
        {
            System.out.println( "Sources roots are: " + sourcesRoots );
        }
        return sourcesRoots;
    }

    private static URL[] prepareRuntimeClasspath( boolean debug, Set<File> sourcesRoots, CommandLine cmd )
        throws MalformedURLException
    {
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
        for( File sourceRoot : sourcesRoots )
        {
            classpathList.add( sourceRoot.toURI().toURL() );
        }
        URL[] runtimeClasspath = classpathList.toArray( new URL[ classpathList.size() ] );
        if( debug )
        {
            System.out.println( "Runtime Classpath is: " + classpathList );
        }
        return runtimeClasspath;
    }

    private static URL[] prepareApplicationClasspath( boolean debug, File classesDir )
        throws MalformedURLException
    {
        URL[] applicationClasspath = new URL[]
        {
            classesDir.toURI().toURL()
        };
        if( debug )
        {
            System.out.println( "Application Classpath is: " + Arrays.toString( applicationClasspath ) );
        }
        return applicationClasspath;
    }

    private static void applySystemProperties( boolean debug, CommandLine cmd )
    {
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
    }

    private static void secretCommand()
    {
        new SecretCommand().run();
    }

    /* package */ static void rebuild(
        URL[] applicationClasspath, URL[] runtimeClasspath, Set<File> sourcesRoots, File classesDir
    )
    {
        System.out.println( "Compiling Application..." );
        String javacOutput = EMPTY;
        try
        {
            // Collect java files
            String javaFiles = EMPTY;
            for( File sourceRoot : sourcesRoots )
            {
                if( sourceRoot.exists() )
                {
                    ProcessBuilder findBuilder = new ProcessBuilder(
                        "find", sourceRoot.getAbsolutePath(), "-type", "f", "-iname", "*.java"
                    );
                    Process find = findBuilder.start();
                    int returnCode = find.waitFor();
                    if( returnCode != 0 )
                    {
                        throw new IOException( "Unable to find java source files in " + sourceRoot );
                    }
                    javaFiles += NEWLINE + readAllAsString( find.getInputStream(), 4096, UTF_8 );
                }
            }
            if( hasText( javaFiles ) )
            {
                // Write list in a temporary file
                File javaListFile = new File( classesDir, ".devshell-java-list" );
                try( FileWriter writer = new FileWriter( javaListFile ) )
                {
                    writer.write( javaFiles );
                    writer.close();
                }
                // Compile
                String[] classpathStrings = new String[ runtimeClasspath.length ];
                for( int idx = 0; idx < runtimeClasspath.length; idx++ )
                {
                    classpathStrings[idx] = runtimeClasspath[idx].toURI().toASCIIString();
                }
                ProcessBuilder javacBuilder = new ProcessBuilder(
                    "javac",
                    "-encoding", "UTF-8",
                    "-source", "1.8",
                    "-d", classesDir.getAbsolutePath(),
                    "-classpath", join( classpathStrings, ":" ),
                    "@" + javaListFile.getAbsolutePath()
                );
                Process javac = javacBuilder.start();
                int returnCode = javac.waitFor();
                if( returnCode != 0 )
                {
                    throw new IOException( "Unable to build java source files." );
                }
                javacOutput = readAllAsString( javac.getInputStream(), 4096, UTF_8 );
            }
        }
        catch( InterruptedException | IOException | URISyntaxException ex )
        {
            throw new WervalException(
                "Unable to rebuild" + ( isEmpty( javacOutput ) ? "" : "\n" + javacOutput ),
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
        help.printUsage( out, WIDTH, "io.werval.cli [options] [command(s)]" );
        out.print(
            "\n"
            + "  The Damn Small Werval DevShell\n"
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
            "\n  io.werval.cli is part of the Werval Development Kit - http://werval.io"
        );
        out.println(
            "\n"
            + "Commands:\n\n"
            + "  new <appdir>  Create a new skeleton application in the 'appdir' directory.\n"
            + "  secret        Generate a new application secret.\n"
            + "  clean         Delete devshell temporary directory, see 'tmpdir' option.\n"
            + "  devshell      Run the Application in development mode.\n"
            + "  start         Run the Application in production mode.\n"
            + "\n"
            + "  If no command is specified, 'start' is assumed."
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
