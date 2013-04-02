package org.qiweb.runtime;

import java.io.File;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.QiWebException;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.routes.RoutesProvider;

/**
 * An Application Instance.
 */
public final class ApplicationInstance
    implements Application
{

    private Config config;
    private File tmpdir;
    private ClassLoader classLoader;
    private final RoutesProvider routesProvider;

    public ApplicationInstance( Config config, ClassLoader classLoader, RoutesProvider routesProvider )
    {
        this.config = config;
        this.routesProvider = routesProvider;
        this.classLoader = classLoader;
        configurationChanged();
    }

    @Override
    public Config config()
    {
        return config;
    }

    @Override
    public File tmpdir()
    {
        return tmpdir;
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    @Override
    public Routes routes()
    {
        return routesProvider.routes( config, classLoader );
    }

    public void changeClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    public void changeConfig( Config config )
    {
        this.config = config;
        configurationChanged();
    }

    private void configurationChanged()
    {
        File tmpdirFile = config.getFile( "qiweb.fs.temp" );
        if( tmpdirFile.isFile() )
        {
            throw new QiWebException( "tmpdir already exist but is a file: " + tmpdirFile );
        }
        if( !tmpdirFile.exists() && !tmpdirFile.mkdirs() )
        {
            throw new QiWebException( "Unable to create non existant tmpdir: " + tmpdirFile );
        }
        this.tmpdir = tmpdirFile;
    }
}
