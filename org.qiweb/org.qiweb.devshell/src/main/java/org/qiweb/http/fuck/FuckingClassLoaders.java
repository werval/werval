package org.qiweb.http.fuck;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.qiweb.http.HttpApplicationProvider;
import org.qiweb.http.server.HttpApplicationClassLoader;
import org.qiweb.util.ClassLoaders;

public class FuckingClassLoaders
{

    public static void main( String[] args )
    {
        URLClassLoader originalClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] jvmClassPath = originalClassLoader.getURLs();
        List<URL> directoriesClassPath = new ArrayList<URL>();
        for( URL jvmClassPathElement : jvmClassPath )
        {
            if( jvmClassPathElement.toString().endsWith( "/" ) )
            {
                directoriesClassPath.add( jvmClassPathElement );
            }
        }
        URL[] classPathToUse = (URL[]) directoriesClassPath.toArray();
        HttpApplicationClassLoader appClassLoader = new HttpApplicationClassLoader( classPathToUse, originalClassLoader );
        try
        {
            HttpApplicationProvider appProvider = null;
            Thread.currentThread().setContextClassLoader( appClassLoader );
            appProvider = (HttpApplicationProvider) appClassLoader.loadClass( "org.qiweb.http.fuck.FuckBootstrap" ).newInstance();
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( originalClassLoader );
            ClassLoaders.printClassLoaders( appClassLoader );
        }
    }
}
