package org.qiweb.devshell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/* package */ class NativeWatcher
{

    private static boolean changed = false;
    private static Method addWatchMethod;
    private static Method removeWatchMethod;
    private static Object listener;

    public static int addWatch( File directoryToWatch )
    {
        try
        {
            return (int) addWatchMethod.invoke( null, directoryToWatch.getAbsolutePath(), 15, true, listener );
        }
        catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
        {
            throw new RuntimeException( "Unable to add watch on '" + directoryToWatch + "': " + ex.getMessage(), ex );
        }
    }

    public static void removeWatch( int watch )
    {
        try
        {
            removeWatchMethod.invoke( null, watch );
        }
        catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
        {
            throw new RuntimeException( "Unable to remove watch: " + ex.getMessage(), ex );
        }
    }

    public static void reloaded()
    {
        changed = false;
    }

    public static void changed()
    {
        changed = true;
    }

    public static boolean hasChanged()
    {
        return changed;
    }

    /* package */ static void init( File projectOutputDir )
    {
        URLClassLoader loader = (URLClassLoader) NativeWatcher.class.getClassLoader();
        URLClassLoader parentLoader = (URLClassLoader) NativeWatcher.class.getClassLoader().getParent();
        File jNotifyJar = jNotifyJar( loader );
        if( jNotifyJar == null )
        {
            throw new RuntimeException( "Unable to find JNotify JAR in the ClassPath." );
        }
        try
        {
            Method addURLMethod = URLClassLoader.class.getDeclaredMethod( "addURL", URL.class );
            addURLMethod.setAccessible( true );
            addURLMethod.invoke( parentLoader, jNotifyJar.toURI().toURL() );

            File nativeDir = new File( projectOutputDir, "native_libraries" );
            if( !nativeDir.exists() )
            {
                unzipFiltered( jNotifyJar, projectOutputDir, "native_libraries" );
            }
            File nativePlatformDir = new File( nativeDir, System.getProperty( "sun.arch.data.model" ) + "bits" );

            System.setProperty( "java.library.path",
                                System.getProperty( "java.library.path", "" ) + File.pathSeparator
                                + nativePlatformDir.getAbsolutePath() );
            Field sysPathField = ClassLoader.class.getDeclaredField( "sys_paths" );
            sysPathField.setAccessible( true );
            sysPathField.set( null, null );

            Class<?> jNotifyClass = parentLoader.loadClass( "net.contentobjects.jnotify.JNotify" );
            Class<?> jNotifyListenerClass = parentLoader.loadClass( "net.contentobjects.jnotify.JNotifyListener" );
            addWatchMethod = jNotifyClass.getMethod( "addWatch", String.class, int.class, boolean.class, jNotifyListenerClass );
            removeWatchMethod = jNotifyClass.getMethod( "removeWatch", int.class );
            Class[] listenerProxyIfaces = new Class[]
            {
                jNotifyListenerClass
            };
            listener = Proxy.newProxyInstance( parentLoader, listenerProxyIfaces, new InvocationHandler()
            {
                @Override
                public Object invoke( Object proxy, Method method, Object[] args )
                    throws Throwable
                {
                    QiWebDevShell.success( "Source has changed!" );
                    changed = true;
                    return null;
                }
            } );
        }
        catch( NoSuchMethodException | SecurityException | IOException |
               IllegalAccessException | IllegalArgumentException | InvocationTargetException |
               NoSuchFieldException | ClassNotFoundException ex )
        {
            throw new RuntimeException( "Unable to setup JNotify Hack: " + ex.getMessage(), ex );
        }
    }

    private static File jNotifyJar( URLClassLoader loader )
    {
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
        JarFile jar = new JarFile( jarFile );
        Enumeration<JarEntry> entries = jar.entries();
        while( entries.hasMoreElements() )
        {
            JarEntry entry = entries.nextElement();
            if( entry.getName().startsWith( startsWith ) )
            {
                File target = new java.io.File( targetDir + java.io.File.separator + entry.getName() );
                if( entry.isDirectory() )
                {
                    if( !target.mkdirs() )
                    {
                        throw new RuntimeException( "Unable to setup JNotify Hack: Cannot create directory " + target );
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
