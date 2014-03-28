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
package org.qiweb.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.qiweb.api.context.Context;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.outcomes.Outcomes;
import org.qiweb.api.util.Stacktraces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_LIST;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;

/**
 * Application Global Object.
 *
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
     *
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
     *
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
     *
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
     *
     * Invoked after {@link #onActivate(org.qiweb.api.Application)}.
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
     *
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
     *
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
     *
     * Invoked before {@link #onPassivate(org.qiweb.api.Application)}.
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
     *
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
            throw new QiWebException( "Unable to create a Plugin.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Get Filter instance.
     *
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
            throw new QiWebException( "Unable to create a Filter.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Get Controller instance.
     *
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
            throw new QiWebException( "Unable to create a Controller.instance: " + ex.getMessage(), ex );
        }
    }

    /**
     * Invoke Controller Method.
     *
     * Default to {@link Method#invoke(java.lang.Object, java.lang.Object[])}.
     *
     * @param context    Request Context
     * @param controller Controller Instance
     *
     * @return Invocation Outcome
     */
    public Outcome invokeControllerMethod( Context context, Object controller )
    {
        try
        {
            Method method = context.route().controllerMethod();
            Object[] parameters = context.request().parameters().values().toArray();
            return (Outcome) method.invoke( controller, parameters );
        }
        catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
        {
            throw new QiWebException( "Unable to invoke Controller Method.", ex );
        }
    }

    /**
     * Invoked when a request completed successfully and all bytes are sent to the client.
     *
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
     *
     * Default to {@link QiWebException}{@literal >}{@link InvocationTargetException} cleanup in development mode.
     * <p>
     * Do nothing in test and production modes.
     * <p>
     * Default behaviour is to clean-up the stacktrace by removing stack elements introduced by the reflective
     * calls done in
     * {@link #invokeControllerMethod(org.qiweb.api.context.Context, java.lang.Object)} implementation.
     * <p>
     * If this method is overriden and throws an exception, the later will be added as suppressed to the original cause.
     *
     * @param throwable A Throwable
     *
     * @return A Throwable
     */
    public Throwable getRootCause( Throwable throwable )
    {
        if( throwable instanceof QiWebException && throwable.getCause() instanceof InvocationTargetException )
        {
            return throwable.getCause().getCause();
        }
        return throwable;
    }

    /**
     * Invoked when an exception pops out of the Application.
     *
     * Happens right before {@link Error} recording.
     * <p>
     * Default to logging the error and producing a minimal HTML page advertising a 500 status code and the
     * corresponding reason phrase.
     * <p>
     * Stacktrace is disclosed in development mode only, with links to project sources when available.
     * <p>
     * If this method is overriden and throws an exception, the later is added as suppressed to the original cause
     * and default behaviour is replayed.
     * This mecanism is the {@literal Application} fault barrier.
     *
     * @param application Application
     * @param outcomes    Outcomes utilities
     * @param cause       Cause
     *
     * @return Outcome to send back to the client
     */
    public Outcome onApplicationError( Application application, Outcomes outcomes, Throwable cause )
    {
        // Log error
        LOG.error( cause.getMessage(), cause );

        // Generate Error Outcome
        StringBuilder html = new StringBuilder();
        html.append( "<html>\n<head><title>500 Internal Server Error</title></head>\n" );
        html.append( "<body>\n<h1>500 Internal Server Error</h1>\n" );
        if( application.mode() == Mode.DEV )
        {
            html.append( Stacktraces.toHtml( cause, application.sourceFileURLGenerator() ) );
        }
        html.append( "</body>\n</html>\n" );
        return outcomes.internalServerError().
            withBody( html.toString() ).
            as( TEXT_HTML ).
            build();
    }
}
