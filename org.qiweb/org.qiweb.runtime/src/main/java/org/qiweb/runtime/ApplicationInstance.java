package org.qiweb.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Crypto;
import org.qiweb.api.Global;
import org.qiweb.api.exceptions.PathBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.PathBinder;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.mime.MimeTypesInstance;
import org.qiweb.runtime.routes.PathBindersInstance;
import org.qiweb.runtime.routes.ReverseRoutesInstance;
import org.qiweb.runtime.routes.RoutesProvider;

import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.*;

/**
 * An Application Instance.
 * <p>Application Mode defaults to {@link Mode#test}.</p>
 * <p>Application Config behaviour is the very same whatever the Mode is.</p>
 * <p>Application ClassLoader defaults to the ClassLoader that defined the ApplicationInstance class.</p>
 * <p>Application Routes are fetched from a given RoutesProvider.</p>
 * <p>Others are based on Application Config and created by Application instances.</p>
 */
public final class ApplicationInstance
    implements Application
{

    private final Mode mode;
    private Config config;
    private Global global;
    private Crypto crypto;
    private File tmpdir;
    private ClassLoader classLoader;
    private final RoutesProvider routesProvider;
    private Routes routes;
    private ReverseRoutes reverseRoutes;
    private PathBinders pathBinders;
    private MimeTypes mimeTypes;

    /**
     * Create a new Application instance in {@link Mode#test}.
     * <p>Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.</p>
     */
    public ApplicationInstance( RoutesProvider routesProvider )
    {
        this( Mode.test, routesProvider );
    }

    public ApplicationInstance( Mode mode, RoutesProvider routesProvider )
    {
        this( mode,
              new ConfigInstance( ApplicationInstance.class.getClassLoader() ),
              ApplicationInstance.class.getClassLoader(),
              routesProvider );
    }

    public ApplicationInstance( Mode mode, Config config, ClassLoader classLoader, RoutesProvider routesProvider )
    {
        ensureNotNull( "Application Mode", mode );
        ensureNotNull( "Application Config", config );
        ensureNotNull( "Application ClassLoader", classLoader );
        ensureNotNull( "Application RoutesProvider", routesProvider );
        this.mode = mode;
        this.config = config;
        this.routesProvider = routesProvider;
        this.classLoader = classLoader;
        configurationChanged();
    }

    @Override
    public Mode mode()
    {
        return mode;
    }

    @Override
    public Config config()
    {
        return config;
    }

    public Global global()
    {
        return global;
    }

    @Override
    public Crypto crypto()
    {
        return crypto;
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
        return routes;
    }

    @Override
    public ReverseRoutes reverseRoutes()
    {
        return reverseRoutes;
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

    public void reload( ClassLoader newClassLoader )
    {
        this.classLoader = newClassLoader;
        this.config = new ConfigInstance( newClassLoader );
        configurationChanged();
    }

    private void configurationChanged()
    {
        configureGlobal();
        configureCrypto();
        configureTmpdir();
        configurePathBinders();
        configureMimeTypes();
        loadRoutes();
    }

    private void loadRoutes()
    {
        routes = routesProvider.routes( this );
        reverseRoutes = new ReverseRoutesInstance( this );
    }

    private void configureGlobal()
    {
        String globalClassName = config.string( "app.global" );
        try
        {
            this.global = (Global) classLoader.loadClass( globalClassName ).newInstance();
        }
        catch( ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException ex )
        {
            throw new QiWebException( "Invalid Global class: " + globalClassName, ex );
        }
    }

    private void configureCrypto()
    {
        this.crypto = new CryptoInstance( config.string( "app.secret" ) );
    }

    private void configureTmpdir()
    {
        File tmpdirFile = config.file( "qiweb.fs.temp" );
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
        for( String pathBinderClassName : config.stringList( "qiweb.routes.path-binders" ) )
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
            mimeTypes = new MimeTypesInstance( config.stringMap( "app.mimetypes" ) );
        }
        else
        {
            mimeTypes = new MimeTypesInstance();
        }
    }
}
