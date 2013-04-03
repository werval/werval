package org.qiweb.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.exceptions.PathBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.PathBinder;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.mime.MimeTypesInstance;
import org.qiweb.runtime.routes.PathBindersInstance;
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
    private PathBinders pathBinders;
    private MimeTypes mimeTypes;

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

    @Override
    public PathBinders pathBinders()
    {
        return pathBinders;
    }

    @Override
    public MimeTypes mimeTypes()
    {
        return mimeTypes;
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
        configureTmpdir();
        configurePathBinders();
        configureMimeTypes();
    }

    private void configureTmpdir()
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
        tmpdir = tmpdirFile;
    }

    private void configurePathBinders()
    {
        List<PathBinder<?>> list = new ArrayList<>();
        for( String pathBinderClassName : config.getStringList( "qiweb.routes.path-binders" ) )
        {
            try
            {
                list.add( (PathBinder<?>) classLoader.loadClass( pathBinderClassName ).newInstance() );
            }
            catch( ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex )
            {
                throw new PathBinderException( "Unable to instanciate PathBinder: " + pathBinderClassName, ex );
            }
        }
        pathBinders = new PathBindersInstance( list );
    }

    private void configureMimeTypes()
    {
        if( config.has( "app.mimetypes" ) )
        {
            mimeTypes = new MimeTypesInstance( config.getStringMap( "app.mimetypes" ) );
        }
        else
        {
            mimeTypes = new MimeTypesInstance();
        }
    }
}
