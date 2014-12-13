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
package io.werval.runtime.outcomes;

import io.werval.api.Config;
import io.werval.api.http.Status;
import io.werval.api.mime.MimeTypes;
import io.werval.api.outcomes.OutcomeBuilder;
import io.werval.api.outcomes.Outcomes;
import io.werval.util.URLs;
import io.werval.runtime.http.ResponseHeaderInstance;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.werval.api.http.Headers.Names.LOCATION;
import static io.werval.api.http.Status.ACCEPTED;
import static io.werval.api.http.Status.BAD_GATEWAY;
import static io.werval.api.http.Status.BAD_REQUEST;
import static io.werval.api.http.Status.CONFLICT;
import static io.werval.api.http.Status.CREATED;
import static io.werval.api.http.Status.EXPECTATION_FAILED;
import static io.werval.api.http.Status.FAILED_DEPENDENCY;
import static io.werval.api.http.Status.FORBIDDEN;
import static io.werval.api.http.Status.FOUND;
import static io.werval.api.http.Status.GATEWAY_TIMEOUT;
import static io.werval.api.http.Status.GONE;
import static io.werval.api.http.Status.HTTP_VERSION_NOT_SUPPORTED;
import static io.werval.api.http.Status.INSUFFICIENT_STORAGE;
import static io.werval.api.http.Status.INTERNAL_SERVER_ERROR;
import static io.werval.api.http.Status.LOCKED;
import static io.werval.api.http.Status.METHOD_NOT_ALLOWED;
import static io.werval.api.http.Status.MULTI_STATUS;
import static io.werval.api.http.Status.NON_AUTHORITATIVE_INFORMATION;
import static io.werval.api.http.Status.NOT_ACCEPTABLE;
import static io.werval.api.http.Status.NOT_FOUND;
import static io.werval.api.http.Status.NOT_IMPLEMENTED;
import static io.werval.api.http.Status.NOT_MODIFIED;
import static io.werval.api.http.Status.NO_CONTENT;
import static io.werval.api.http.Status.OK;
import static io.werval.api.http.Status.PARTIAL_CONTENT;
import static io.werval.api.http.Status.PRECONDITION_FAILED;
import static io.werval.api.http.Status.REQUEST_ENTITY_TOO_LARGE;
import static io.werval.api.http.Status.REQUEST_TIMEOUT;
import static io.werval.api.http.Status.REQUEST_URI_TOO_LONG;
import static io.werval.api.http.Status.RESET_CONTENT;
import static io.werval.api.http.Status.SEE_OTHER;
import static io.werval.api.http.Status.SERVICE_UNAVAILABLE;
import static io.werval.api.http.Status.TEMPORARY_REDIRECT;
import static io.werval.api.http.Status.TOO_MANY_REQUESTS;
import static io.werval.api.http.Status.UNAUTHORIZED;
import static io.werval.api.http.Status.UNPROCESSABLE_ENTITY;
import static io.werval.api.http.Status.UNSUPPORTED_MEDIA_TYPE;
import static io.werval.runtime.ConfigKeys.WERVAL_CHARACTER_ENCODING;

/**
 * Outcomes instance.
 */
public final class OutcomesInstance
    implements Outcomes
{
    private final Config config;
    private final MimeTypes mimeTypes;
    private final ResponseHeaderInstance response;

    public OutcomesInstance( Config config, MimeTypes mimeTypes, ResponseHeaderInstance response )
    {
        this.config = config;
        this.mimeTypes = mimeTypes;
        this.response = response;
    }

    @Override
    public OutcomeBuilder status( int status )
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( status ) );
    }

    @Override
    public OutcomeBuilder ok()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( OK ) );
    }

    @Override
    public OutcomeBuilder ok( byte[] body )
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( OK ) ).withBody( body );
    }

    @Override
    public OutcomeBuilder ok( String body )
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( OK ) ).withBody( body );
    }

    @Override
    public OutcomeBuilder ok( String body, Charset charset )
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( OK ) ).withBody( body, charset );
    }

    @Override
    public OutcomeBuilder created()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( CREATED ) );
    }

    @Override
    public OutcomeBuilder accepted()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( ACCEPTED ) );
    }

    @Override
    public OutcomeBuilder nonAuthoritativeInformation()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NON_AUTHORITATIVE_INFORMATION ) );
    }

    @Override
    public OutcomeBuilder noContent()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NO_CONTENT ) );
    }

    @Override
    public OutcomeBuilder resetContent()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( RESET_CONTENT ) );
    }

    @Override
    public OutcomeBuilder partialContent()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( PARTIAL_CONTENT ) );
    }

    @Override
    public OutcomeBuilder multiStatus()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( MULTI_STATUS ) );
    }

    @Override
    public OutcomeBuilder found( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), FOUND );
    }

    @Override
    public OutcomeBuilder found( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, FOUND );
    }

    @Override
    public OutcomeBuilder seeOther( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), SEE_OTHER );
    }

    @Override
    public OutcomeBuilder seeOther( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, SEE_OTHER );
    }

    @Override
    public OutcomeBuilder notModified()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NOT_MODIFIED ) );
    }

    @Override
    public OutcomeBuilder temporaryRedirect( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), TEMPORARY_REDIRECT );
    }

    @Override
    public OutcomeBuilder temporaryRedirect( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, TEMPORARY_REDIRECT );
    }

    @Override
    public OutcomeBuilder badRequest()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( BAD_REQUEST ) );
    }

    @Override
    public OutcomeBuilder unauthorized()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( UNAUTHORIZED ) );
    }

    @Override
    public OutcomeBuilder forbidden()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( FORBIDDEN ) );
    }

    @Override
    public OutcomeBuilder notFound()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NOT_FOUND ) );
    }

    @Override
    public OutcomeBuilder methodNotAllowed()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( METHOD_NOT_ALLOWED ) );
    }

    @Override
    public OutcomeBuilder notAcceptable()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NOT_ACCEPTABLE ) );
    }

    @Override
    public OutcomeBuilder requestTimeout()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( REQUEST_TIMEOUT ) );
    }

    @Override
    public OutcomeBuilder conflict()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( CONFLICT ) );
    }

    @Override
    public OutcomeBuilder gone()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( GONE ) );
    }

    @Override
    public OutcomeBuilder preconditionFailed()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( PRECONDITION_FAILED ) );
    }

    @Override
    public OutcomeBuilder requestEntityTooLarge()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( REQUEST_ENTITY_TOO_LARGE ) );
    }

    @Override
    public OutcomeBuilder requestUriTooLong()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( REQUEST_URI_TOO_LONG ) );
    }

    @Override
    public OutcomeBuilder unsupportedMediaType()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( UNSUPPORTED_MEDIA_TYPE ) );
    }

    @Override
    public OutcomeBuilder expectationFailed()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( EXPECTATION_FAILED ) );
    }

    @Override
    public OutcomeBuilder unprocessableEntity()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( UNPROCESSABLE_ENTITY ) );
    }

    @Override
    public OutcomeBuilder locked()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( LOCKED ) );
    }

    @Override
    public OutcomeBuilder failedDependency()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( FAILED_DEPENDENCY ) );
    }

    @Override
    public OutcomeBuilder tooManyRequest()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( TOO_MANY_REQUESTS ) );
    }

    @Override
    public OutcomeBuilder internalServerError()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( INTERNAL_SERVER_ERROR ) );
    }

    @Override
    public OutcomeBuilder notImplemented()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( NOT_IMPLEMENTED ) );
    }

    @Override
    public OutcomeBuilder badGateway()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( BAD_GATEWAY ) );
    }

    @Override
    public OutcomeBuilder serviceUnavailable()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( SERVICE_UNAVAILABLE ) );
    }

    @Override
    public OutcomeBuilder gatewayTimeout()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( GATEWAY_TIMEOUT ) );
    }

    @Override
    public OutcomeBuilder httpVersionNotSupported()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( HTTP_VERSION_NOT_SUPPORTED ) );
    }

    @Override
    public OutcomeBuilder insufficientStorage()
    {
        return new OutcomeBuilderInstance( config, mimeTypes, response.withStatus( INSUFFICIENT_STORAGE ) );
    }

    private OutcomeBuilder redirect( String url, Map<String, List<String>> queryString, Status status )
    {
        OutcomeBuilder builder = new OutcomeBuilderInstance(
            config,
            mimeTypes,
            response.withStatus( status )
        );
        return builder.withHeader(
            LOCATION,
            URLs.appendQueryString( url, queryString, config.charset( WERVAL_CHARACTER_ENCODING ) )
        );
    }
}
