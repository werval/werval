package org.qiweb.devshell;

import java.io.File;

/**
 * 1. Compile on start
 * 2. Monitor sources and resources directories for changes
 * 3. Has a global status : CLEAN, DIRTY, READY
 * 4. Run DevShell
 */
public class QiWebDevShell
{

    public static void main( String[] args )
    {
        //warn( Arrays.toString( ( (URLClassLoader) Thread.currentThread().getContextClassLoader() ).getURLs() ) );
        try
        {
            info( "QiWeb DevShell loading..." );
            File workingDir = new File( System.getProperty( "user.dir" ) );

            info( "Loading Project Model..." );
            ProjectBuilder projectBuilder = new ProjectBuilder( workingDir );
            ProjectModel projectModel = projectBuilder.buildProjectModel();

            info( "Compiling " + projectModel.name() + " a first time..." );
            projectBuilder.compileJava();

            info( "Watching for changes in " + projectModel.mainSources() );
            NativeWatcher.init( projectModel.projectOutputDir() );
            for( File mainSource : projectModel.mainSources() )
            {
                NativeWatcher.addWatch( mainSource );
            }

            info( "Starting Netty..." );
            DumbNetty netty = new DumbNetty();
            netty.start();

            info( "Ready for requests!" );

            //loadModel( args );
            //runBuild( args );
        }
        catch( Exception ex )
        {
            error( "Something went pretty bad: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void success( String message )
    {
        System.out.println( ANSI_RESET + ANSI_GREEN + message + ANSI_RESET );
    }

    public static void highlight( String message )
    {
        System.out.println( ANSI_RESET + ANSI_WHITE + message + ANSI_RESET );
    }

    public static void info( String message )
    {
        System.out.println( ANSI_RESET + message + ANSI_RESET );
    }

    public static void warn( String message )
    {
        System.out.println( ANSI_RESET + ANSI_YELLOW + message + ANSI_RESET );
    }

    public static void error( String message )
    {
        System.out.println( ANSI_RESET + ANSI_RED + message + ANSI_RESET );
    }
}
