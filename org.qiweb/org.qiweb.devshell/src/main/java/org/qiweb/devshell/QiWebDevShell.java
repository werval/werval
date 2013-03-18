package org.qiweb.devshell;

import java.io.File;

import static org.qiweb.devshell.NewDevShell.*;

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
}
