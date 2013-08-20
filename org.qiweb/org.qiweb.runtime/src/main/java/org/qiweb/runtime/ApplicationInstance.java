/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Crypto;
import org.qiweb.api.Global;
import org.qiweb.api.MetaData;
import org.qiweb.api.exceptions.ParameterBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.ParameterBinder;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Routes;
import org.qiweb.runtime.mime.MimeTypesInstance;
import org.qiweb.runtime.routes.ParameterBindersInstance;
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
    private ParameterBinders parameterBinders;
    private MimeTypes mimeTypes;
    private final MetaData metaData;

    /**
     * Create a new Application instance in {@link Mode#test}.
     * <p>Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.</p>
     */
    public ApplicationInstance( RoutesProvider routesProvider )
    {
        this( Mode.TEST, routesProvider );
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
        this.metaData = new MetaData();
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
    public ParameterBinders parameterBinders()
    {
        return parameterBinders;
    }

    @Override
    public MimeTypes mimeTypes()
    {
        return mimeTypes;
    }

    @Override
    public MetaData metaData()
    {
        return metaData;
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
        configureParameterBinders();
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

    private void configureParameterBinders()
    {
        List<ParameterBinder<?>> list = new ArrayList<>();
        for( String parameterBinderClassName : config.stringList( "qiweb.routes.parameter-binders" ) )
        {
            try
            {
                list.add( (ParameterBinder<?>) classLoader.loadClass( parameterBinderClassName ).newInstance() );
            }
            catch( ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex )
            {
                throw new ParameterBinderException( "Unable to instanciate ParameterBinders: " + parameterBinderClassName, ex );
            }
        }
        parameterBinders = new ParameterBindersInstance( list );
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
