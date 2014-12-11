/*
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

import io.werval.api.Application;
import io.werval.api.ApplicationExecutors;
import io.werval.api.Config;
import io.werval.api.Crypto;
import io.werval.api.Errors;
import io.werval.api.Global;
import io.werval.api.MetaData;
import io.werval.api.Mode;
import io.werval.api.cache.Cache;
import io.werval.api.context.Context;
import io.werval.api.context.ThreadContextHelper;
import io.werval.api.exceptions.ParameterBinderException;
import io.werval.api.exceptions.PassivationException;
import io.werval.api.exceptions.WervalException;
import io.werval.api.exceptions.RouteNotFoundException;
import io.werval.api.filters.FilterChain;
import io.werval.api.http.Cookies.Cookie;
import io.werval.api.http.FormUploads;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.Request;
import io.werval.api.http.RequestHeader;
import io.werval.api.http.Session;
import io.werval.api.http.Status;
import io.werval.api.i18n.Langs;
import io.werval.api.mime.MimeTypes;
import io.werval.api.outcomes.DefaultErrorOutcomes;
import io.werval.api.outcomes.Outcome;
import io.werval.api.outcomes.OutcomeBuilder;
import io.werval.api.outcomes.Outcomes;
import io.werval.api.routes.ParameterBinder;
import io.werval.api.routes.ParameterBinders;
import io.werval.api.routes.ReverseRoutes;
import io.werval.api.routes.Route;
import io.werval.api.routes.Routes;
import io.werval.api.templates.Templates;
import io.werval.util.Reflectively;
import io.werval.util.Stacktraces;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.qiweb.runtime.context.ContextInstance;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.filters.FilterChainFactory;
import org.qiweb.runtime.http.HttpBuildersInstance;
import org.qiweb.runtime.events.EventsInstance;
import org.qiweb.runtime.http.ResponseHeaderInstance;
import org.qiweb.runtime.http.SessionInstance;
import org.qiweb.runtime.i18n.LangsInstance;
import org.qiweb.runtime.mime.MimeTypesInstance;
import org.qiweb.runtime.outcomes.OutcomesInstance;
import org.qiweb.runtime.routes.ParameterBindersInstance;
import org.qiweb.runtime.routes.ReverseRoutesInstance;
import org.qiweb.runtime.routes.RoutesConfProvider;
import org.qiweb.runtime.routes.RoutesInstance;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.runtime.util.TypeResolver;
import org.qiweb.spi.ApplicationSPI;
import org.qiweb.spi.dev.DevShellSPI;
import org.qiweb.spi.http.HttpBuildersSPI;
import org.qiweb.spi.events.EventsSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Names.COOKIE;
import static io.werval.api.http.Headers.Names.RETRY_AFTER;
import static io.werval.api.http.Headers.Names.SET_COOKIE;
import static io.werval.api.http.Headers.Names.X_QIWEB_REQUEST_ID;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;
import static io.werval.api.mime.MimeTypes.TEXT_HTML;
import static io.werval.util.IllegalArguments.ensureNotNull;
import static io.werval.util.InputStreams.BUF_SIZE_4K;
import static io.werval.util.InputStreams.transferTo;
import static io.werval.util.Strings.NEWLINE;
import static io.werval.util.Strings.hasText;
import static io.werval.util.Strings.indentTwoSpaces;
import static org.qiweb.runtime.BuildVersion.COMMIT;
import static org.qiweb.runtime.BuildVersion.DATE;
import static org.qiweb.runtime.BuildVersion.DIRTY;
import static org.qiweb.runtime.BuildVersion.VERSION;
import static org.qiweb.runtime.ConfigKeys.APP_BANNER;
import static org.qiweb.runtime.ConfigKeys.APP_GLOBAL;
import static org.qiweb.runtime.ConfigKeys.APP_LANGS;
import static org.qiweb.runtime.ConfigKeys.APP_SECRET;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_ONLYIFCHANGED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_CHARACTER_ENCODING;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_FORMS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_HEADERS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_QUERYSTRING_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_UPLOADS_MULTIVALUED;
import static org.qiweb.runtime.ConfigKeys.QIWEB_MIMETYPES_SUPPLEMENTARY;
import static org.qiweb.runtime.ConfigKeys.QIWEB_MIMETYPES_TEXTUAL;
import static org.qiweb.runtime.ConfigKeys.QIWEB_ROUTES_PARAMETERBINDERS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_SHUTDOWN_RETRYAFTER;
import static org.qiweb.runtime.ConfigKeys.QIWEB_TMPDIR;

/**
 * An Application Instance.
 *
 * Application Mode defaults to {@link Mode#TEST}.
 * <p>
 * Application Config behaviour is the very same whatever the Mode is.
 * <p>
 * Application ClassLoader defaults to the ClassLoader that defined the ApplicationInstance class.
 * <p>
 * Application Routes are fetched from a given RoutesProvider.
 * <p>
 * Others are based on Application Config and created by Application instances.
 */
@Reflectively.Loaded( by = "DevShell" )
public final class ApplicationInstance
    implements Application, ApplicationSPI
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplicationInstance.class );

    static
    {
        org.qiweb.runtime.util.Versions.ensureQiWebComponentsVersions();
    }

    private volatile boolean activated = false;
    private volatile boolean activatingOrPassivating = false;
    private final Mode mode;
    private ConfigInstance config;
    private PluginsInstance plugins;
    private Global global;
    private Crypto crypto;
    private Langs langs;
    private Charset defaultCharset;
    private File tmpdir;
    private ClassLoader classLoader;
    private final RoutesProvider routesProvider;
    private Routes routes;
    private ReverseRoutes reverseRoutes;
    private ParameterBinders parameterBinders;
    private MimeTypes mimeTypes;
    private HttpBuildersSPI httpBuilders;
    private ApplicationExecutorsInstance executors;
    private final MetaData metaData;
    private final EventsInstance events;
    private final Errors errors;
    private final DevShellSPI devSpi;

    /**
     * Create a new Application instance in given {@link Mode}.
     *
     * Routes are loaded from the {@literal routes.conf} file.
     * <p>
     * Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.
     *
     * @param mode Application Mode, must be not null
     */
    public ApplicationInstance( Mode mode )
    {
        this( mode, new RoutesConfProvider() );
    }

    /**
     * Create a new Application instance in given {@link Mode}.
     *
     * Use the ClassLoader that loaded the {@link ApplicationInstance} class as Application ClassLoader.
     *
     * @param mode           Application Mode, must be not null
     * @param routesProvider Routes provider, must be not null
     */
    public ApplicationInstance( Mode mode, RoutesProvider routesProvider )
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
     * @param mode           Application Mode, must be not null
     * @param config         Application config, must be not null
     * @param classLoader    Application ClassLoader, must be not null
     * @param routesProvider Routes provider, must be not null
     */
    public ApplicationInstance(
        Mode mode,
        ConfigInstance config,
        ClassLoader classLoader,
        RoutesProvider routesProvider
    )
    {
        this( mode, config, classLoader, routesProvider, null );
    }

    /**
     * Create a new Application instance in given {@link Mode}.
     *
     * @param mode        Application Mode, must be not null
     * @param config      Application config, must be not null
     * @param classLoader Application ClassLoader, must be not null
     * @param devSpi      DevShell SPI, can be null
     */
    @Reflectively.Invoked( by = "DevShell" )
    public ApplicationInstance(
        Mode mode,
        ConfigInstance config,
        ClassLoader classLoader,
        DevShellSPI devSpi
    )
    {
        this( mode, config, classLoader, new RoutesConfProvider(), devSpi );
    }

    private ApplicationInstance(
        Mode mode,
        ConfigInstance config,
        ClassLoader classLoader,
        RoutesProvider routesProvider,
        DevShellSPI devSpi
    )
    {
        ensureNotNull( "Application Mode", mode );
        ensureNotNull( "Application Config", config );
        ensureNotNull( "Application ClassLoader", classLoader );
        ensureNotNull( "Application RoutesProvider", routesProvider );
        if( mode == Mode.DEV )
        {
            // Disable TypeResolver caching in Development Mode
            TypeResolver.disableCache();
        }
        this.mode = mode;
        this.config = config;
        this.routesProvider = routesProvider;
        this.reverseRoutes = new ReverseRoutesInstance( this );
        this.classLoader = classLoader;
        this.metaData = new MetaData();
        this.events = new EventsInstance( this );
        this.errors = new ErrorsInstance( config );
        this.devSpi = devSpi;
        if( mode == Mode.DEV && LOG.isDebugEnabled() )
        {
            LOG.debug( "Runtime classpath: {}", Arrays.toString( devSpi.runtimeClassPath() ) );
            LOG.debug( "Application classpath: {}", Arrays.toString( devSpi.applicationClassPath() ) );
        }
        configure();
        showBanner();
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

        activatingOrPassivating = true;

        try
        {
            // Clear Errors and Events
            errors.clear();
            events.unregisterAll();

            // Executors
            executors = new ApplicationExecutorsInstance( this );
            executors.activate();

            // Global
            String globalClassName = config.string( APP_GLOBAL );
            this.global = executors.supplyAsync(() ->
                {
                    try
                    {
                        return (Global) classLoader.loadClass( globalClassName ).newInstance();
                    }
                    catch( ClassNotFoundException | ClassCastException |
                           InstantiationException | IllegalAccessException ex )
                    {
                        throw new WervalException( "Invalid Global class: " + globalClassName, ex );
                    }
                }
            ).join();

            // Application Routes
            List<Route> resolvedRoutes = new ArrayList<>();
            resolvedRoutes.addAll( executors.supplyAsync( () -> routesProvider.routes( this ) ).join() );

            // Activate Plugins
            plugins = new PluginsInstance();
            executors.runAsync( () -> plugins.onActivate( this ) ).join();

            // Plugin contributed Routes
            resolvedRoutes.addAll( 0, executors.supplyAsync( () -> plugins.firstRoutes( this ) ).join() );
            resolvedRoutes.addAll( executors.supplyAsync( () -> plugins.lastRoutes( this ) ).join() );
            routes = new RoutesInstance( resolvedRoutes );

            // Activated
            activated = true;
            executors.runAsync( () -> global.onActivate( this ) ).join();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( previousLoader );
            activatingOrPassivating = false;
        }

        if( LOG.isInfoEnabled() )
        {
            StringBuilder runtimeSummary = new StringBuilder( "Runtime Summary\n\n" );
            String header = String.format(
                "QiWeb v%s\n"
                + "  Git commit: %s%s, built on: %s\n"
                + "  Java version: %s, vendor: %s\n"
                + "  Java home: %s\n"
                + "  Default locale: %s, platform encoding: %s\n"
                + "  OS name: %s, version: %s, arch: %s\n",
                VERSION,
                COMMIT,
                ( DIRTY ? " (DIRTY)" : "" ),
                DATE,
                System.getProperty( "java.version" ),
                System.getProperty( "java.vendor" ),
                System.getProperty( "java.home" ),
                Locale.getDefault().toString(),
                System.getProperty( "file.encoding" ),
                System.getProperty( "os.name" ),
                System.getProperty( "os.version" ),
                System.getProperty( "os.arch" )
            );
            runtimeSummary.append( indentTwoSpaces( header, 1 ) ).append( NEWLINE ).append( NEWLINE );
            String configLocation = config.location().toStringShort();
            if( hasText( configLocation ) )
            {
                runtimeSummary
                    .append( indentTwoSpaces( "Configuration", 1 ) )
                    .append( NEWLINE )
                    .append( indentTwoSpaces( configLocation, 2 ) )
                    .append( NEWLINE )
                    .append( NEWLINE );
            }
            String allRoutes = routes.toString();
            if( hasText( allRoutes ) )
            {
                runtimeSummary
                    .append( indentTwoSpaces( "Routes", 1 ) )
                    .append( NEWLINE )
                    .append( indentTwoSpaces( allRoutes, 2 ) )
                    .append( NEWLINE )
                    .append( NEWLINE );
            }
            runtimeSummary
                .append( indentTwoSpaces( "Executors", 1 ) )
                .append( NEWLINE )
                .append( indentTwoSpaces( executors.toString(), 2 ) )
                .append( NEWLINE );
            String allPlugins = plugins.toString();
            if( hasText( allPlugins ) )
            {
                runtimeSummary
                    .append( NEWLINE )
                    .append( indentTwoSpaces( "Plugins", 1 ) )
                    .append( NEWLINE )
                    .append( indentTwoSpaces( allPlugins, 2 ) )
                    .append( NEWLINE );
            }
            LOG.info( runtimeSummary.toString() );
        }
        LOG.debug( "Application Activated ({} mode)", mode );
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

        activatingOrPassivating = true;
        try
        {
            List<Exception> passivationErrors = new ArrayList<>();
            try
            {
                executors.runAsync( () -> global.onPassivate( this ) ).join();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new PassivationException( "Exception(s) on Global::onPassivate(): " + ex.getMessage(), ex )
                );
            }
            try
            {
                executors.runAsync( () -> plugins.onPassivate( this ) ).join();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new PassivationException( "Exception(s) on Plugins::onPassivate(): " + ex.getMessage(), ex )
                );
            }
            try
            {
                executors.passivate();
            }
            catch( Exception ex )
            {
                passivationErrors.add(
                    new PassivationException( "Exception(s) on Executors::onPassivate(): " + ex.getMessage(), ex )
                );
            }
            if( !passivationErrors.isEmpty() )
            {
                PassivationException ex = new PassivationException(
                    "There were errors during Application passivation"
                );
                for( Exception err : passivationErrors )
                {
                    ex.addSuppressed( err );
                }
                throw ex;
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( previousLoader );
            activated = false;
            activatingOrPassivating = false;
        }

        LOG.debug( "Application Passivated" );
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
        ensureActive();
        return plugins.plugin( pluginApiType );
    }

    @Override
    public <T> Iterable<T> plugins( Class<T> pluginApiType )
    {
        ensureActive();
        return plugins.plugins( pluginApiType );
    }

    @Override
    public Crypto crypto()
    {
        return crypto;
    }

    @Override
    public Langs langs()
    {
        return langs;
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
        ensureActive();
        return routes;
    }

    @Override
    public ReverseRoutes reverseRoutes()
    {
        ensureActive();
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
    public Cache cache()
    {
        return plugin( Cache.class );
    }

    @Override
    public Templates templates()
    {
        return plugin( Templates.class );
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
        ensureActive();
        return global;
    }

    // SPI
    @Override
    @Reflectively.Invoked( by = "DevShell" )
    public void reload( ClassLoader newClassLoader )
    {
        if( activated )
        {
            passivate();
        }
        this.classLoader = newClassLoader;
        this.config = new ConfigInstance( newClassLoader, config.location() );
        configure();
        activate();
    }

    @Override
    public ApplicationExecutors executors()
    {
        ensureActive();
        return executors;
    }

    // API/SPI
    @Override
    public EventsSPI events()
    {
        return events;
    }

    // API/SPI
    @Override
    public HttpBuildersSPI httpBuilders()
    {
        return httpBuilders;
    }

    // SPI
    @Override
    public CompletableFuture<Outcome> handleRequest( Request request )
    {
        ensureActive();
        return executors.supplyAsync(
            () ->
            {
                // Prepare Controller Context
                ThreadContextHelper contextHelper = new ThreadContextHelper();
                try
                {
                    // Validates incoming request
                    validatesRequestHeader( request );
                    validatesRequestBody( request );

                    // Route the request
                    final Route route = routes().route( request );
                    LOG.debug( "Routing to: {}", route );

                    // Bind parameters
                    request.bind( parameterBinders(), route );

                    // Parse Session Cookie
                    Session session = new SessionInstance(
                        config,
                        crypto(),
                        request.cookies().get( config.string( APP_SESSION_COOKIE_NAME ) )
                    );

                    // Prepare Response Header
                    ResponseHeaderInstance responseHeader = new ResponseHeaderInstance( request.version() );

                    // Set Controller Context
                    Context context = new ContextInstance(
                        this,
                        session, route, request,
                        responseHeader,
                        executors.defaultExecutor()
                    );
                    contextHelper.setOnCurrentThread( context );

                    // Plugins beforeInteraction
                    plugins.beforeInteraction( context );

                    try
                    {
                        // Invoke Controller FilterChain, ended by Controller Method Invokation
                        // TODO Handle Timeout when invoking Controller FilterChain!
                        LOG.trace( "Invoking interaction method: {}", route.controllerMethod() );
                        FilterChain chain = new FilterChainFactory().buildFilterChain( this, global, context );
                        CompletableFuture<Outcome> interaction = chain.next( context );
                        Outcome outcome = interaction.get( 30, TimeUnit.SECONDS );

                        // Apply Session to ResponseHeader
                        if( !config.bool( APP_SESSION_COOKIE_ONLYIFCHANGED ) || session.hasChanged() )
                        {
                            outcome.responseHeader().cookies().set( session.signedCookie() );
                        }

                        // Add Set-Cookie headers
                        for( Cookie cookie : outcome.responseHeader().cookies() )
                        {
                            HttpCookie jCookie = new HttpCookie( cookie.name(), cookie.value() );
                            jCookie.setVersion( cookie.version() );
                            jCookie.setPath( cookie.path() );
                            jCookie.setDomain( cookie.domain() );
                            jCookie.setMaxAge( cookie.maxAge() );
                            jCookie.setSecure( cookie.secure() );
                            jCookie.setHttpOnly( cookie.httpOnly() );
                            jCookie.setComment( cookie.comment() );
                            jCookie.setCommentURL( cookie.commentUrl() );
                            outcome.responseHeader().headers().with( SET_COOKIE, jCookie.toString() );
                        }

                        // Finalize!
                        finalizeOutcome( request, outcome );

                        // Done!
                        LOG.trace( "Interaction outcome: {}", outcome );
                        return outcome;
                    }
                    finally
                    {
                        // Plugins afterInteraction
                        plugins.afterInteraction( context );
                    }
                }
                catch( Throwable cause )
                {
                    // Handle error
                    return handleError( request, cause );
                }
                finally
                {
                    // Clean up Controller Context
                    contextHelper.clearCurrentThread();
                }
            }
        );
    }

    private void validatesRequestHeader( RequestHeader requestHeader )
    {
        // Multi-valued QueryString parameters
        if( !config.bool( QIWEB_HTTP_QUERYSTRING_MULTIVALUED ) )
        {
            for( List<String> values : requestHeader.queryString().allValues().values() )
            {
                if( values.size() > 1 )
                {
                    throw new BadRequestException( "Multi-valued query string parameters are not allowed" );
                }
            }
        }
        // Multi-valued Headers
        if( !config.bool( QIWEB_HTTP_HEADERS_MULTIVALUED ) )
        {
            for( List<String> values : requestHeader.headers().allValues().values() )
            {
                if( values.size() > 1 )
                {
                    throw new BadRequestException( "Multi-valued headers are not allowed" );
                }
            }
        }
        // Some are prohibited anyway
        if( requestHeader.headers().values( COOKIE ).size() > 1 )
        {
            throw new BadRequestException(
                "RFC 6265 - 5.4. The Cookie Header - When the user agent generates an HTTP request, "
                + "the user agent MUST NOT attach more than one Cookie header field."
            );
        }
        // TODO Multi-valued Cookies
    }

    private void validatesRequestBody( Request request )
    {
        // Multi-valued Form Attributes
        if( !config.bool( QIWEB_HTTP_FORMS_MULTIVALUED ) )
        {
            for( List<String> values : request.body().formAttributes().allValues().values() )
            {
                if( values.size() > 1 )
                {
                    throw new BadRequestException( "Multi-valued form attributes are not allowed" );
                }
            }
        }
        // Multi-valued Form Uploads
        if( !config.bool( QIWEB_HTTP_UPLOADS_MULTIVALUED ) )
        {
            for( List<FormUploads.Upload> values : request.body().formUploads().allValues().values() )
            {
                if( values.size() > 1 )
                {
                    throw new BadRequestException( "Multi-valued form uploads are not allowed" );
                }
            }
        }
    }

    // SPI
    @Override
    public Outcome handleError( RequestHeader request, Throwable cause )
    {
        // Clean-up stacktrace
        final Throwable rootCause = ErrorHandling.cleanUpStackTrace( this, LOG, cause );

        // Outcomes
        Outcomes outcomes = new OutcomesInstance(
            config,
            mimeTypes,
            new ResponseHeaderInstance( request.version() )
        );

        // Handle contingencies
        if( rootCause instanceof RouteNotFoundException )
        {
            LOG.trace( rootCause.getMessage() + " will return 404." );
            StringBuilder details = new StringBuilder();
            if( mode == Mode.DEV )
            {
                if( TEXT_HTML.equals( request.preferredMimeType() ) )
                {
                    details.append( "<p>Tried:</p>\n<pre>\n" );
                }
                else
                {
                    details.append( "Tried:\n\n" );
                }
                for( Route route : routes )
                {
                    if( !route.path().startsWith( "/@" ) )
                    {
                        details.append( route.toString() ).append( NEWLINE );
                    }
                }
                if( TEXT_HTML.equals( request.preferredMimeType() ) )
                {
                    details.append( "</pre>\n" );
                }
            }
            return finalizeOutcome(
                request,
                DefaultErrorOutcomes.errorOutcome(
                    request,
                    Status.NOT_FOUND,
                    "404 Route Not Found",
                    details.toString(),
                    outcomes
                ).build()
            );
        }
        else if( rootCause instanceof ParameterBinderException )
        {
            if( mode == Mode.DEV )
            {
                LOG.warn( "ParameterBinderException, will return 400.", rootCause );
            }
            else
            {
                LOG.trace( "ParameterBinderException, will return 400.", rootCause );
            }
            return finalizeOutcome(
                request,
                DefaultErrorOutcomes.errorOutcome(
                    request,
                    Status.BAD_REQUEST,
                    Status.BAD_REQUEST.reasonPhrase(),
                    rootCause.getMessage(),
                    outcomes
                ).build()
            );
        }
        else if( rootCause instanceof BadRequestException )
        {
            if( mode == Mode.DEV )
            {
                LOG.warn( "BadRequestException, will return 400.", rootCause );
            }
            else
            {
                LOG.trace( "BadRequestException, will return 400.", rootCause );
            }
            return finalizeOutcome(
                request,
                DefaultErrorOutcomes.errorOutcome(
                    request,
                    Status.BAD_REQUEST,
                    Status.BAD_REQUEST.reasonPhrase(),
                    rootCause.getMessage(),
                    outcomes
                ).build()
            );
        }

        // Handle faults
        Outcome outcome;
        try
        {
            // Delegates Outcome generation to Global object
            if( executors.inDefaultExecutor() )
            {
                outcome = global.onRequestError( this, request, outcomes, rootCause );
            }
            else
            {
                outcome = executors.supplyAsync(
                    () -> global.onRequestError( this, request, outcomes, rootCause )
                ).join();
            }
        }
        catch( Exception ex )
        {
            // Add as suppressed and replay Global default behaviour. This serve as a fault barrier
            rootCause.addSuppressed( ex );
            if( executors.inDefaultExecutor() )
            {
                outcome = new Global().onRequestError( this, request, outcomes, rootCause );
            }
            else
            {
                outcome = executors.supplyAsync(
                    () -> new Global().onRequestError( this, request, outcomes, rootCause )
                ).join();
            }
        }

        // Record error
        errors.record( request.identity(), rootCause.getMessage(), rootCause );

        // Done!
        return finalizeOutcome( request, outcome );
    }

    private Outcome finalizeOutcome( RequestHeader request, Outcome outcome )
    {
        // Apply Keep-Alive
        applyKeepAlive( request, outcome );
        // Add X-QiWeb-Request-ID
        outcome.responseHeader().headers().withSingle( X_QIWEB_REQUEST_ID, request.identity() );
        return outcome;
    }

    /**
     * Apply Keep-Alive headers if needed.
     *
     * Do nothing if the Outcome already has a {@literal Connection} header.
     * <p>
     * If status is an error or unknown status, {@literal Connection} is set to {@literal Close} if protocol version
     * use Keep-Alive by default or if request is not Keep-Alive.
     * <p>
     * Otherwise, set {@literal Connection} to {@link Keep-Alive} if protocol version use Keep-Alive by default
     * or if request is Keep-Alive.
     */
    private void applyKeepAlive( RequestHeader request, Outcome outcome )
    {
        String connection = outcome.responseHeader().headers().singleValue( CONNECTION );
        if( connection.isEmpty() )
        {
            if( outcome.responseHeader().status().statusClass().isForceClose() || !request.isKeepAlive() )
            {
                outcome.responseHeader().headers().withSingle( CONNECTION, CLOSE );
            }
            else
            {
                outcome.responseHeader().headers().withSingle( CONNECTION, KEEP_ALIVE );
            }
        }
    }

    // SPI
    @Override
    public void onHttpRequestComplete( RequestHeader requestHeader )
    {
        executors.runAsync(
            () -> global.onHttpRequestComplete( this, requestHeader )
        ).exceptionally(
            ex ->
            {
                LOG.error( "An error occured in Global::onHttpRequestComplete(): {}", ex.getMessage(), ex );
                return null;
            }
        );
    }

    // SPI
    @Override
    public CompletableFuture<Outcome> shuttingDownOutcome( ProtocolVersion version, String requestIdentity )
    {
        return executors.supplyAsync(
            () ->
            {
                // Outcomes
                Outcomes outcomes = new OutcomesInstance(
                    config,
                    mimeTypes,
                    new ResponseHeaderInstance( version )
                );

                // Return 503 to incoming requests while shutting down
                OutcomeBuilder builder = DefaultErrorOutcomes.errorOutcome(
                    null,
                    Status.SERVICE_UNAVAILABLE,
                    Status.SERVICE_UNAVAILABLE.reasonPhrase(),
                    "Service is shutting down",
                    outcomes
                );
                builder.withHeader( CONNECTION, CLOSE )
                    .withHeader( X_QIWEB_REQUEST_ID, requestIdentity );

                // By default, no Retry-After, only if defined in configuration
                if( config.has( QIWEB_SHUTDOWN_RETRYAFTER ) )
                {
                    builder.withHeader( RETRY_AFTER, String.valueOf( config.seconds( QIWEB_SHUTDOWN_RETRYAFTER ) ) );
                }

                // Build!
                return builder.build();
            }
        );
    }

    private void ensureActive()
    {
        if( !activated && !activatingOrPassivating )
        {
            throw new IllegalStateException( "Application is not active." );
        }
    }

    private void configure()
    {
        configureDefaultCharset();
        configureCrypto();
        configureLangs();
        configureTmpdir();
        configureParameterBinders();
        configureMimeTypes();
        configureHttpBuilders();
    }

    private void configureDefaultCharset()
    {
        this.defaultCharset = config.charset( QIWEB_CHARACTER_ENCODING );
    }

    private void configureCrypto()
    {
        this.crypto = new CryptoInstance( config.string( APP_SECRET ), defaultCharset );
    }

    private void configureLangs()
    {
        this.langs = new LangsInstance( config.stringList( APP_LANGS ) );
    }

    private void configureTmpdir()
    {
        File tmpdirFile = config.file( QIWEB_TMPDIR );
        if( tmpdirFile.isFile() )
        {
            throw new WervalException( "tmpdir already exist but is a file: " + tmpdirFile );
        }
        if( !tmpdirFile.exists() && !tmpdirFile.mkdirs() )
        {
            throw new WervalException( "Unable to create non existant tmpdir: " + tmpdirFile );
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

    private void configureHttpBuilders()
    {
        httpBuilders = new HttpBuildersInstance( config, defaultCharset, langs );
    }

    private void showBanner()
    {
        try( InputStream input = classLoader.getResourceAsStream( config.string( APP_BANNER ) ) )
        {
            if( input != null )
            {
                transferTo( input, System.out, BUF_SIZE_4K );
            }
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
