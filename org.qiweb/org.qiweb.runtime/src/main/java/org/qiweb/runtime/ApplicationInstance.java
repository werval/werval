/**
 * Copyright (c) 2013-2014 the original author or authors
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Crypto;
import org.qiweb.api.Errors;
import org.qiweb.api.Global;
import org.qiweb.api.MetaData;
import org.qiweb.api.Mode;
import org.qiweb.api.context.Context;
import org.qiweb.api.context.ThreadContextHelper;
import org.qiweb.api.exceptions.ParameterBinderException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.exceptions.RouteNotFoundException;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.Session;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.outcomes.OutcomeBuilder;
import org.qiweb.api.outcomes.Outcomes;
import org.qiweb.api.routes.ParameterBinder;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.ReverseRoutes;
import org.qiweb.api.routes.Route;
import org.qiweb.api.routes.Routes;
import org.qiweb.api.util.Reflectively;
import org.qiweb.api.util.Stacktraces;
import org.qiweb.runtime.context.ContextInstance;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.filters.FilterChainFactory;
import org.qiweb.runtime.http.HttpBuildersInstance;
import org.qiweb.runtime.http.ResponseHeaderInstance;
import org.qiweb.runtime.http.SessionInstance;
import org.qiweb.runtime.mime.MimeTypesInstance;
import org.qiweb.runtime.outcomes.OutcomesInstance;
import org.qiweb.runtime.routes.ParameterBindersInstance;
import org.qiweb.runtime.routes.ReverseRoutesInstance;
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.http.HttpBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;
import static org.qiweb.api.http.Headers.Names.CONNECTION;
import static org.qiweb.api.http.Headers.Names.RETRY_AFTER;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_REQUEST_ID;
import static org.qiweb.api.http.Headers.Values.CLOSE;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;
import static org.qiweb.runtime.ConfigKeys.APP_GLOBAL;
import static org.qiweb.runtime.ConfigKeys.APP_SECRET;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_ONLYIFCHANGED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_CHARACTER_ENCODING;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_MIMETYPES_SUPPLEMENTARY;
import static org.qiweb.runtime.ConfigKeys.QIWEB_MIMETYPES_TEXTUAL;
import static org.qiweb.runtime.ConfigKeys.QIWEB_ROUTES_PARAMETERBINDERS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_RETRYAFTER;
import static org.qiweb.runtime.ConfigKeys.QIWEB_TMPDIR;

/**
 * An Application Instance.
 * <p>Application Mode defaults to {@link Mode#TEST}.</p>
 * <p>Application Config behaviour is the very same whatever the Mode is.</p>
 * <p>Application ClassLoader defaults to the ClassLoader that defined the ApplicationInstance class.</p>
 * <p>Application Routes are fetched from a given RoutesProvider.</p>
 * <p>Others are based on Application Config and created by Application instances.</p>
 */
@Reflectively.Loaded( by = "DevShell" )
public final class ApplicationInstance
    implements Application, ApplicationSPI
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplicationInstance.class );
    private volatile boolean activated;
    private final Mode mode;
    private Config config;
    private PluginsInstance plugins;
    private Global global;
    private Crypto crypto;
    private Charset defaultCharset;
    private File tmpdir;
    private ClassLoader classLoader;
    private final RoutesProvider routesProvider;
    private Routes routes;
    private ReverseRoutes reverseRoutes;
    private ParameterBinders parameterBinders;
    private MimeTypes mimeTypes;
    private HttpBuilders httpBuilders;
    private final MetaData metaData;
    private final Errors errors;
    private final DevShellSPI devSpi;

    /**
     * Create a new Application instance in {@link Mode#TEST}.
     * <p>Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.</p>
     *
     * @param routesProvider Routes provider, must be not null
     */
    public ApplicationInstance( RoutesProvider routesProvider )
    {
        this( Mode.TEST, routesProvider );
    }

    /**
     * Create a new Application instance in given {@link Mode}.
     * <p>Routes are loaded from the {@literal routes.conf} file.</p>
     * <p>Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.</p>
     *
     * @param mode Application Mode, must be not null
     */
    public ApplicationInstance( Mode mode )
    {
        this( mode, new RoutesConfProvider() );
    }

    private ApplicationInstance( Mode mode, RoutesProvider routesProvider )
    {
        this(
            mode,
            new ConfigInstance( ApplicationInstance.class.getClassLoader() ),
            ApplicationInstance.class.getClassLoader(),
            routesProvider,
            null
        );
    }

    /**
     * Create a new Application instance in given {@link Mode}.
     *
     * @param mode Application Mode, must be not null
     * @param config Application config, must be not null
     * @param classLoader Application ClassLoader, must be not null
     * @param routesProvider Routes provider, must be not null
     */
    public ApplicationInstance( Mode mode, Config config, ClassLoader classLoader, RoutesProvider routesProvider )
    {
        this( mode, config, classLoader, routesProvider, null );
    }

    /**
     * Create a new Application instance in given {@link Mode}.
     *
     * @param mode Application Mode, must be not null
     * @param config Application config, must be not null
     * @param classLoader Application ClassLoader, must be not null
     * @param routesProvider Routes provider, must be not null
     * @param devSpi DevShell SPI, can be null
     */
    @Reflectively.Invoked( by = "DevShell" )
    public ApplicationInstance( Mode mode, Config config, ClassLoader classLoader, RoutesProvider routesProvider, DevShellSPI devSpi )
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
        this.errors = new ErrorsInstance( config );
        this.devSpi = devSpi;
        configure();
    }

    @Override
    public synchronized void activate()
    {
        if( activated )
        {
            throw new IllegalStateException( "Application already activated." );
        }
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classLoader );
        try
        {
            plugins.onActivate( this );
            global.onActivate( this );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( previousLoader );
        }
        activated = true;
    }

    @Override
    public synchronized void passivate()
    {
        if( !activated )
        {
            throw new IllegalStateException( "Application already passivated." );
        }
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classLoader );
        try
        {
            try
            {
                global.onPassivate( this );
            }
            catch( Exception ex )
            {
                LOG.error( "There were errors during Global passivation", ex );
            }
            plugins.onPassivate( this );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( previousLoader );
        }
        activated = false;
    }

    @Override
    public boolean isActive()
    {
        return activated;
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

    @Override
    public <T> T plugin( Class<T> pluginApiType )
    {
        return plugins.plugin( pluginApiType );
    }

    @Override
    public <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        return plugins.plugins( pluginApiType );
    }

    @Override
    public Crypto crypto()
    {
        return crypto;
    }

    @Override
    public Charset defaultCharset()
    {
        return defaultCharset;
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

    @Override
    public Errors errors()
    {
        return errors;
    }

    @Override
    public Stacktraces.FileURLGenerator sourceFileURLGenerator()
    {
        if( devSpi != null )
        {
            return devSpi::sourceURL;
        }
        return new Stacktraces.NullFileURLGenerator();
    }

    // SPI
    @Override
    public Global global()
    {
        return global;
    }

    // SPI
    @Override
    @Reflectively.Invoked( by = "DevShell" )
    public void reload( ClassLoader newClassLoader )
    {
        passivate();
        this.classLoader = newClassLoader;
        this.config = new ConfigInstance( newClassLoader );
        configure();
        activate();
    }

    @Override
    public HttpBuilders httpBuilders()
    {
        return httpBuilders;
    }

    // SPI
    @Override
    public Outcome handleRequest( Request request )
    {
        // Prepare Controller Context
        ThreadContextHelper contextHelper = new ThreadContextHelper();
        try
        {
            // Route the request
            final Route route = routes().route( request );
            LOG.debug( "{} Routing request to: {}", request.identity(), route );

            // Bind parameters
            request.bind( parameterBinders(), route );

            // Parse Session Cookie
            Session session = new SessionInstance(
                config,
                crypto(),
                request.cookies().get( config.string( APP_SESSION_COOKIE_NAME ) )
            );

            // Prepare Response Header
            ResponseHeaderInstance responseHeader = new ResponseHeaderInstance(
                request.version(),
                config.bool( QIWEB_HTTP_HEADERS_MULTIVALUED )
            );

            // Set Controller Context
            Context context = new ContextInstance( this, session, route, request, responseHeader );
            contextHelper.setOnCurrentThread( context );

            // Invoke Controller FilterChain, ended by Controller Method Invokation
            LOG.trace( "Invoking controller method: {}", route.controllerMethod() );
            Outcome outcome = new FilterChainFactory().buildFilterChain( this, global(), context ).next( context );

            // Apply Session to ResponseHeader
            if( !config.bool( APP_SESSION_COOKIE_ONLYIFCHANGED ) || session.hasChanged() )
            {
                outcome.responseHeader().cookies().set( session.signedCookie() );
            }

            // Finalize!
            return finalizeOutcome( request, outcome );
        }
        catch( Throwable cause )
        {
            // Handle error
            Outcome errorOutcome = handleError( request, cause );
            // Finalize!
            return finalizeOutcome( request, errorOutcome );
        }
        finally
        {
            // Clean up Controller Context
            contextHelper.clearCurrentThread();
        }
    }

    private Outcome handleError( RequestHeader request, Throwable cause )
    {
        // Clean-up stacktrace
        Throwable rootCause;
        try
        {
            rootCause = global.getRootCause( cause );
        }
        catch( Exception ex )
        {
            LOG.warn(
                "An error occured in Global::getRootCause() method "
                + "and has been added as suppressed exception to the original error stacktrace. Message: {}",
                ex.getMessage(), ex
            );
            cause.addSuppressed( ex );
            rootCause = cause;
        }

        // Outcomes
        Outcomes outcomes = new OutcomesInstance(
            config,
            new ResponseHeaderInstance( request.version(), config.bool( QIWEB_HTTP_HEADERS_MULTIVALUED ) )
        );

        // Handle contingencies
        if( rootCause instanceof RouteNotFoundException )
        {
            LOG.trace( rootCause.getMessage() + " will return 404." );
            StringBuilder details = new StringBuilder();
            if( mode == Mode.DEV )
            {
                details.append( "<p>Tried:</p>\n<pre>\n" );
                for( Route route : routes )
                {
                    if( !route.path().startsWith( "/@" ) )
                    {
                        details.append( route.toString() ).append( "\n" );
                    }
                }
                details.append( "</pre>\n" );
            }
            return outcomes.notFound().
                withBody( errorHtml( "404 Route Not Found", details ) ).
                as( TEXT_HTML ).
                build();
        }
        else if( rootCause instanceof ParameterBinderException )
        {
            LOG.warn( "ParameterBinderException, will return 400.", rootCause );
            return outcomes.badRequest().
                withBody( errorHtml( "400 Bad Request", rootCause.getMessage() ) ).
                as( TEXT_HTML ).
                build();
        }
        else if( rootCause instanceof BadRequestException )
        {
            LOG.warn( "BadRequestException, will return 400.", rootCause );
            return outcomes.badRequest().
                withBody( errorHtml( "400 Bad Request", rootCause.getMessage() ) ).
                as( TEXT_HTML ).
                build();
        }

        // Handle faults
        Outcome outcome;
        try
        {
            // Delegates Outcome generation to Global object
            outcome = global.onApplicationError( this, outcomes, rootCause );
        }
        catch( Exception ex )
        {
            // Add as suppressed and replay Global default behaviour. This serve as a fault barrier
            rootCause.addSuppressed( ex );
            outcome = new Global().onApplicationError( this, outcomes, rootCause );
        }

        // Record error
        errors.record( request.identity(), rootCause.getMessage(), rootCause );

        // Done!
        return outcome;
    }

    private CharSequence errorHtml( CharSequence title, CharSequence content )
    {
        return new StringBuilder().
            append( "<html>\n<head><title>" ).append( title ).append( "</title></head>\n" ).
            append( "<body>\n" ).
            append( "<h1>" ).append( title ).append( "</h1>\n" ).
            append( content ).append( "\n" ).
            append( "</body>\n</html>\n" );
    }

    private Outcome finalizeOutcome( RequestHeader request, Outcome outcome )
    {
        // Apply Keep-Alive
        outcome.responseHeader().withKeepAliveHeaders( request.isKeepAlive() );
        // Add X-QiWeb-Request-ID
        outcome.responseHeader().headers().withSingle( X_QIWEB_REQUEST_ID, request.identity() );
        return outcome;
    }

    // SPI
    @Override
    public void onHttpRequestComplete( RequestHeader requestHeader )
    {
        try
        {
            global.onHttpRequestComplete( this, requestHeader );
        }
        catch( Exception ex )
        {
            LOG.error( "An error occured in Global::onHttpRequestComplete(): {}", ex.getMessage(), ex );
        }
    }

    // SPI
    @Override
    public Outcome shuttingDownOutcome( ProtocolVersion version, String requestIdentity )
    {
        // Outcomes
        Outcomes outcomes = new OutcomesInstance(
            config,
            new ResponseHeaderInstance( version, config.bool( QIWEB_HTTP_HEADERS_MULTIVALUED ) )
        );

        // Return 503 to incoming requests while shutting down
        OutcomeBuilder builder = outcomes.serviceUnavailable().
            withBody( errorHtml( "503 Service Unavailable", "Service is shutting down" ) ).
            as( TEXT_HTML ).
            withHeader( CONNECTION, CLOSE ).
            withHeader( X_QIWEB_REQUEST_ID, requestIdentity );

        // By default, no Retry-After, only if defined in configuration
        if( config.has( QIWEB_SHUTDOWN_RETRYAFTER ) )
        {
            builder.withHeader( RETRY_AFTER, String.valueOf( config.seconds( QIWEB_SHUTDOWN_RETRYAFTER ) ) );
        }

        // Build!
        return builder.build();
    }

    private void configure()
    {
        configureGlobal();
        configureDefaultCharset();
        configureCrypto();
        configureTmpdir();
        configureParameterBinders();
        configureMimeTypes();
        configureRoutes();
        configurePlugins();
        configureHttpBuilders();
    }

    private void configureGlobal()
    {
        String globalClassName = config.string( APP_GLOBAL );
        try
        {
            this.global = (Global) classLoader.loadClass( globalClassName ).newInstance();
        }
        catch( ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException ex )
        {
            throw new QiWebException( "Invalid Global class: " + globalClassName, ex );
        }
    }

    private void configureDefaultCharset()
    {
        this.defaultCharset = config.charset( QIWEB_CHARACTER_ENCODING );
    }

    private void configureCrypto()
    {
        this.crypto = new CryptoInstance( config.string( APP_SECRET ), defaultCharset );
    }

    private void configureTmpdir()
    {
        File tmpdirFile = config.file( QIWEB_TMPDIR );
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
        for( String parameterBinderClassName : config.stringList( QIWEB_ROUTES_PARAMETERBINDERS ) )
        {
            try
            {
                list.add( (ParameterBinder<?>) classLoader.loadClass( parameterBinderClassName ).newInstance() );
            }
            catch( ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex )
            {
                throw new ParameterBinderException( "Unable to instanciate ParameterBinders, failed at: "
                                                    + parameterBinderClassName, ex );
            }
        }
        parameterBinders = new ParameterBindersInstance( list );
    }

    private void configureMimeTypes()
    {
        // Load textuals mime-types
        Map<String, Charset> textuals = new LinkedHashMap<>();
        for( Map.Entry<String, String> textConfig : config.stringMap( QIWEB_MIMETYPES_TEXTUAL ).entrySet() )
        {
            String mimetype = textConfig.getKey();
            String charsetString = textConfig.getValue().trim();
            Charset charset = "default".equals( charsetString )
                              ? defaultCharset
                              : Charset.forName( charsetString );
            textuals.put( mimetype, charset );
        }
        // Load supplementary mime-types
        if( config.has( QIWEB_MIMETYPES_SUPPLEMENTARY ) )
        {
            Map<String, String> supplementaryMimetypes = config.stringMap( QIWEB_MIMETYPES_SUPPLEMENTARY );
            mimeTypes = new MimeTypesInstance( defaultCharset, supplementaryMimetypes, textuals );
        }
        else
        {
            mimeTypes = new MimeTypesInstance( defaultCharset, textuals );
        }
    }

    private void configureRoutes()
    {
        routes = routesProvider.routes( this );
        reverseRoutes = new ReverseRoutesInstance( this );
    }

    private void configurePlugins()
    {
        plugins = new PluginsInstance( config, global.extraPlugins() );
    }

    private void configureHttpBuilders()
    {
        httpBuilders = new HttpBuildersInstance( config );
    }
}
