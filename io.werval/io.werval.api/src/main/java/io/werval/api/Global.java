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
package io.werval.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import io.werval.api.context.Context;
import io.werval.api.exceptions.WervalException;
import io.werval.api.http.RequestHeader;
import io.werval.api.http.Status;
import io.werval.api.outcomes.DefaultErrorOutcomes;
import io.werval.api.outcomes.Outcome;
import io.werval.api.outcomes.Outcomes;
import io.werval.util.Stacktraces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.api.mime.MimeTypes.TEXT_HTML;
import static io.werval.util.Strings.EMPTY;
import static java.util.Collections.EMPTY_LIST;

/**
 * Application Global Object.
 * <p>
 * Provide lifecycle, instanciation, invocation and error handling hooks.
 * <p>
 * You are encouraged to subclass it in your Application code by setting the <code>app.global</code> configuration
 * property to the FQCN of your Global implementation.
 * <p>
 * Remeber you can call the {@literal super} methods to leverage the default behaviour.
 */
public class Global
{
    private static final Logger LOG = LoggerFactory.getLogger( Global.class );

    /**
     * Chance to provide extra Plugins instances programmatically.
     * <p>
     * Invoked before Application activation.
     * <p>
     * Default to no extra Plugins.
     *
     * @return Extra Plugin instances
     */
    public List<Plugin<?>> extraPlugins()
    {
        return EMPTY_LIST;
    }

    /**
     * Application activation.
     * <p>
     * Invoked after all Plugins activation.
     * <p>
     * Default to NOOP.
     *
     * @param application Application
     */
    public void onActivate( Application application )
    {
    }

    /**
     * Application passivation.
     * <p>
     * Invoked before all Plugins passivation.
     * <p>
     * Default to NOOP.
     *
     * @param application Application
     */
    public void onPassivate( Application application )
    {
    }

    /**
     * Invoked before binding Http Server.
     * <p>
     * Invoked after {@link #onActivate(io.werval.api.Application)}.
     * <p>
     * Default to NOOP.
     * <p>
     * Reloads occuring in development mode do not trigger this call.
     *
     * @param application Application
     */
    public void beforeHttpBind( Application application )
    {
    }

    /**
     * Invoked after binding Http Server.
     * <p>
     * Default to NOOP.
     * <p>
     * Reloads occuring in development mode do not trigger this call.
     *
     * @param application Application
     */
    public void afterHttpBind( Application application )
    {
    }

    /**
     * Invoked before unbinding Http Server.
     * <p>
     * Default to NOOP.
     * <p>
     * Reloads occuring in development mode do not trigger this call.
     *
     * @param application Application
     */
    public void beforeHttpUnbind( Application application )
    {
    }

    /**
     * Invoked after unbinding Http Server.
     * <p>
     * Invoked before {@link #onPassivate(io.werval.api.Application)}.
     * <p>
     * Default to NOOP.
     * <p>
     * Reloads occuring in development mode do not trigger this call.
     *
     * @param application Application
     */
    public void afterHttpUnbind( Application application )
    {
    }

    /**
     * Get Plugin instance.
     * <p>
     * Default to {@link Class#newInstance()}.
     *
     * @param <T>         Plugin Parameterized Type
     * @param application Application
     * @param pluginType  Plugin Type
     *
     * @return Plugin Instance
     */
    public <T> T getPluginInstance( Application application, Class<T> pluginType )
    {
        try
        {
            return pluginType.newInstance();
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new WervalException( "Unable to create a Plugin.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Get Filter instance.
     * <p>
     * Default to {@link Class#newInstance()}.
     *
     * @param <T>         Filter Parameterized Type
     * @param application Application
     * @param filterType  Filter Type
     *
     * @return Filter Instance
     */
    public <T> T getFilterInstance( Application application, Class<T> filterType )
    {
        try
        {
            return filterType.newInstance();
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new WervalException( "Unable to create a Filter.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Get Controller instance.
     * <p>
     * Default to {@link Class#newInstance()}.
     *
     * @param <T>            Controller Parameterized Type
     * @param application    Application
     * @param controllerType Controller Type
     *
     * @return Controller Instance
     */
    public <T> T getControllerInstance( Application application, Class<T> controllerType )
    {
        try
        {
            return controllerType.newInstance();
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new WervalException( "Unable to create a Controller.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Invoke Controller Method.
     * <p>
     * Default to {@link Method#invoke(java.lang.Object, java.lang.Object[])}.
     * <p>
     * Acceptable return types are:
     * <ul>
     * <li>{@literal Outcome},</li>
     * <li>{@literal CompletableFuture}&lt;{@literal Outcome}&gt;.</li>
     * </ul>
     * That is, the return types allowed on interaction methods.
     *
     * @param context    Request Context
     * @param controller Controller Instance
     *
     * @return Invocation Outcome, plain or future
     */
    public Object invokeControllerMethod( Context context, Object controller )
    {
        try
        {
            Method method = context.route().controllerMethod();
            Object[] parameters = context.request().parameters().values().toArray();
            return method.invoke( controller, parameters );
        }
        catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
        {
            throw new WervalException( "Unable to invoke Controller Method.", ex );
        }
    }

    /**
     * Invoked when a request completed successfully and all bytes are sent to the client.
     * <p>
     * Default to NOOP.
     * <p>
     * If this method throws an exception, it will be logged it but it won't be rethrown
     * since the request has already completed.
     *
     * @param application   Application
     * @param requestHeader Request Header
     */
    public void onHttpRequestComplete( Application application, RequestHeader requestHeader )
    {
    }

    /**
     * Give a chance to clean-up stacktraces of Throwables poping out of the Application.
     * <p>
     * Default to {@link WervalException}{@literal >}{@link InvocationTargetException} cleanup in development mode.
     * <p>
     * Do nothing in test and production modes.
     * <p>
     * Default behaviour is to clean-up the stacktrace by removing stack elements introduced by the reflective
     * calls done in
     * {@link #invokeControllerMethod(io.werval.api.context.Context, java.lang.Object)} implementation.
     * <p>
     * If this method is overriden and throws an exception, the later will be added as suppressed to the original cause.
     *
     * @param throwable A Throwable
     *
     * @return A Throwable
     */
    public Throwable getRootCause( Throwable throwable )
    {
        if( throwable instanceof WervalException && throwable.getCause() instanceof InvocationTargetException )
        {
            return throwable.getCause().getCause();
        }
        return throwable;
    }

    /**
     * Invoked when an exception pops out of the Application while processing a request.
     * <p>
     * Happens right before {@link Error} recording.
     * <p>
     * Default to logging the error and producing a minimal {@literal HTML} page, {@literal JSON} document or
     * {@literal text/plain} response advertising a 500 status code and the corresponding reason phrase.
     * Response content-type depends on content negociation.
     * <p>
     * Stacktrace is disclosed in development mode only.
     * In {@literal HTML} mode, links to project sources are added when available.
     * <p>
     * If this method is overriden and throws an exception, the later is added as suppressed to the original cause
     * and default behaviour is replayed.
     * This mecanism is the {@link Application} fault barrier.
     *
     * @param application Application
     * @param request     Request header
     * @param outcomes    Outcomes utilities
     * @param cause       Cause
     *
     * @return Outcome to send back to the client
     */
    public Outcome onRequestError( Application application, RequestHeader request, Outcomes outcomes, Throwable cause )
    {
        // Log error
        LOG.error( "Request error: {}: {}", cause.getClass(), cause.getMessage(), cause );

        // Generate Error Outcome
        String detail;
        if( application.mode() == Mode.DEV )
        {
            if( TEXT_HTML.equals( request.preferredMimeType() ) )
            {
                detail = Stacktraces.toHtml( cause, application.sourceFileURLGenerator() ).toString();
            }
            else
            {
                detail = Stacktraces.toString( cause );
            }
        }
        else
        {
            detail = EMPTY;
        }
        return DefaultErrorOutcomes.errorOutcome(
            request,
            Status.INTERNAL_SERVER_ERROR,
            Status.INTERNAL_SERVER_ERROR.reasonPhrase(),
            detail,
            outcomes
        ).build();
    }

    /**
     * Invoked when an uncaught exception pops out of the Application's threads.
     *
     * @param application Application
     * @param cause       Cause
     */
    public void onApplicationError( Application application, Throwable cause )
    {
        // Log error
        LOG.error( "Uncaught Exception: {}: {}", cause.getClass(), cause.getMessage(), cause );
    }
}
