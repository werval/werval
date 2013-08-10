package org.qiweb.devshell;

import org.qiweb.spi.dev.Watcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyWatcher
    implements Watcher
{

    private static boolean nativeLibsDeployed = false;

    public static synchronized void deployNativeLibraries( File writableDirectory )
    {
        if( !nativeLibsDeployed )
        {
            // Find the JNotify JAR in the ClassPath
            URLClassLoader loader = (URLClassLoader) JNotifyWatcher.class.getClassLoader();
            File jNotifyJar = jNotifyJar( loader );
            if( jNotifyJar == null )
            {
                throw new QiWebDevShellException( "Unable to find JNotify JAR in the ClassPath." );
            }
            try
            {
                // Unpack native libraries if necessary
                File nativeDir = new File( writableDirectory, "native_libraries" );
                if( !nativeDir.exists() )
                {
                    unzipFiltered( jNotifyJar, writableDirectory, "native_libraries" );
                }

                // Update Java Library Path
                File nativePlatformDir = new File( nativeDir, System.getProperty( "sun.arch.data.model" ) + "bits" );
                System.setProperty( "java.library.path",
                                    System.getProperty( "java.library.path", "" ) + File.pathSeparator
                                    + nativePlatformDir.getAbsolutePath() );
                Field sysPathField = ClassLoader.class.getDeclaredField( "sys_paths" );
                sysPathField.setAccessible( true );
                sysPathField.set( null, null );

                // Done!
                nativeLibsDeployed = true;
            }
            catch( IOException | SecurityException | IllegalAccessException |
                   IllegalArgumentException | NoSuchFieldException ex )
            {
                throw new QiWebDevShellException( "Unable to deploy JNotify Native Libraries: " + ex.getMessage(), ex );
            }
        }
    }

    private static File jNotifyJar( URLClassLoader loader )
    {
        if( loader.getURLs().length == 1 )
        {
            // We should be in a single fat jar
            return new File( loader.getURLs()[0].getFile() );
        }
        for( URL url : loader.getURLs() )
        {
            if( url.getFile().contains( "/jnotify" ) )
            {
                return new File( url.getFile() );
            }
        }
        return null;
    }

    private static void unzipFiltered( File jarFile, File targetDir, String startsWith )
        throws IOException
    {
        try( JarFile jar = new JarFile( jarFile ) )
        {
            Enumeration<JarEntry> entries = jar.entries();
            while( entries.hasMoreElements() )
            {
                JarEntry entry = entries.nextElement();
                if( entry.getName().startsWith( startsWith ) )
                {
                    File target = new File( targetDir + File.separator + entry.getName() );
                    if( entry.isDirectory() )
                    {
                        if( !target.mkdirs() )
                        {
                            throw new QiWebDevShellException( "Unable to deploy JNotify Native Libraries: Cannot create directory " + target );
                        }
                        continue;
                    }
                    try( InputStream input = jar.getInputStream( entry );
                         FileOutputStream output = new FileOutputStream( target ) )
                    {
                        while( input.available() > 0 )
                        {
                            output.write( input.read() );
                        }
                    }
                }
            }
        }
    }

    private static class ListenerWrapper
        implements JNotifyListener
    {

        private final ChangeListener listener;

        public ListenerWrapper( ChangeListener listener )
        {
            this.listener = listener;
        }

        @Override
        public void fileCreated( int i, String string, String string1 )
        {
            listener.onChange();
        }

        @Override
        public void fileDeleted( int i, String string, String string1 )
        {
            listener.onChange();
        }

        @Override
        public void fileModified( int i, String string, String string1 )
        {
            listener.onChange();
        }

        @Override
        public void fileRenamed( int i, String string, String string1, String string2 )
        {
            listener.onChange();
        }
    }

    @Override
    public Watch watch( final Set<File> directories, final ChangeListener listener )
    {
        try
        {
            JNotifyListener jNotifyListener = new ListenerWrapper( listener );
            final List<Integer> watches = new ArrayList<>();
            for( File dir : directories )
            {
                watches.add( JNotify.addWatch( dir.getAbsolutePath(), JNotify.FILE_ANY, true, jNotifyListener ) );
            }
            return new Watch()
            {
                @Override
                public void unwatch()
                {
                    try
                    {
                        for( Integer watch : watches )
                        {
                            JNotify.removeWatch( watch );
                        }
                    }
                    catch( JNotifyException ex )
                    {
                        throw new QiWebDevShellException( "Unable to unwatch directories." + ex.getMessage(), ex );
                    }
                }
            };
        }
        catch( JNotifyException ex )
        {
            throw new QiWebDevShellException( "Unable to watch directories." + ex.getMessage(), ex );
        }
    }
}
