package org.qiweb.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Enumeration;
import org.qi4j.functional.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpApplicationClassLoader
    extends URLClassLoader
{

    private static class FindLoadedClassClassLoader
        extends ClassLoader
    {

        private FindLoadedClassClassLoader( ClassLoader parent )
        {
            super( parent );
        }

        private boolean hasParentLoadedClass( String name )
        {
            return super.findLoadedClass( name ) != null;
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger( HttpApplicationClassLoader.class );
    private final ClassLoader parentClassLoader;
    private final ClassLoader systemClassLoader;

    public HttpApplicationClassLoader( URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
        parentClassLoader = parent;
        systemClassLoader = getSystemClassLoader();
    }

    @Override
    public Class<?> loadClass( String name )
        throws ClassNotFoundException
    {
        // First check whether it's already been loaded locally, if so use it
        Class<?> loadedClass = findLoadedClass( name );

        if( loadedClass == null && systemClassLoader != null )
        {
            // First try System ClassLoader
            try
            {
                loadedClass = systemClassLoader.loadClass( name );
                LOG.debug( "Loaded {} through the system ClassLoader: {}", name, systemClassLoader );
            }
            catch( ClassNotFoundException ex )
            {
                // Swallow exception - Class is not loadable by the System ClassLoader
            }
        }
        if( loadedClass == null )
        {
            // Check if parent ClassLoader already loaded it
            if( new FindLoadedClassClassLoader( parentClassLoader ).hasParentLoadedClass( name ) )
            {
                loadedClass = parentClassLoader.loadClass( name );
                LOG.debug( "Already loaded {} in parent ClassLoader: {}", name, parentClassLoader );
            }
        }
        if( loadedClass == null )
        {
            try
            {
                // Ignore parent delegation and just try to load locally
                loadedClass = findClass( name );
                LOG.debug( "Loaded {} locally", name );
            }
            catch( ClassNotFoundException e )
            {
                // Swallow exception - Does not exist locally
            }
        }
        if( loadedClass == null )
        {
            // If not found locally, try directly on parent ClassLoader
            // Throws ClassNotFoundException if not found in parent delegation hierarchy at all
            loadedClass = parentClassLoader.loadClass( name );
            LOG.debug( "Loaded {} through parent classloader delegation using {}", parentClassLoader );
        }
        // will never return null (ClassNotFoundException will be thrown)
        return loadedClass;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // TODO
    @Override
    public URL getResource( String name )
    {
        return super.getResource( name );
    }

    @Override
    public Enumeration<URL> getResources( String name )
        throws IOException
    {
        return super.getResources( name );
    }

    @Override
    public InputStream getResourceAsStream( String name )
    {
        return super.getResourceAsStream( name );
    }
    // -----------------------------------------------------------------------------------------------------------------
    // OLD CODE
    private static final String URL_PREFIX = "file:///Users/paul/src/github/eskatos/qiweb/devshell/build/classes/test";
    private final Specification<String> appCodeSpec = new Specification<String>()
    {
        @Override
        public boolean satisfiedBy( String typeName )
        {
            return typeName.startsWith( "com.acme.app" );
        }
    };

    protected Class<?> loadClass_OLD( String name, boolean resolve )
        throws ClassNotFoundException
    {
        // Already loaded?
        Class<?> loadedClass = findLoadedClass( name );
        if( loadedClass != null )
        {
            return loadedClass;
        }

        // Application code?
        if( appCodeSpec.satisfiedBy( name ) )
        {
            String url = URL_PREFIX + "/" + name.replaceAll( "\\.", "/" ) + ".class";
            LOG.debug( "Loading {} from {}", name, url );
            try
            {
                URL classUrl = new URL( url );
                URLConnection connection = classUrl.openConnection();
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = input.read();
                while( data != -1 )
                {
                    buffer.write( data );
                    data = input.read();
                }
                input.close();
                byte[] classData = buffer.toByteArray();
                loadedClass = defineClass( name, classData, 0, classData.length );
                if( resolve )
                {
                    resolveClass( loadedClass );
                }
                return loadedClass;
            }
            catch( IOException ex )
            {
                throw new ClassNotFoundException( "Unable to load " + name, ex );
            }
        }

        // Other code
        try
        {
            LOG.debug( "Trying to load {} locally", name );
            loadedClass = findClass( name );
        }
        catch( ClassNotFoundException e )
        {
            // Swallow exception - does not exist locally
        }
        // If not found locally, use normal parent delegation in URLClassloader
        if( loadedClass == null )
        {
            LOG.debug( "Delegate {} loading to parent ClassLoader", name );
            // throws ClassNotFoundException if not found in delegation hierarchy at all
            loadedClass = super.loadClass( name );
        }
        // Will never return null (ClassNotFoundException will be thrown)
        if( resolve )
        {
            resolveClass( loadedClass );
        }
        return loadedClass;
    }
}
