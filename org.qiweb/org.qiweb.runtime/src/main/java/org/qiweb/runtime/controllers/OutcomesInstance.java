package org.qiweb.runtime.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qiweb.api.controllers.OutcomeBuilder;
import org.qiweb.api.http.MutableCookies;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.runtime.util.URLs;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Outcomes instance.
 */
public class OutcomesInstance
    implements org.qiweb.api.controllers.Outcomes
{

    private final MutableHeaders headers;
    private final MutableCookies cookies;

    public OutcomesInstance( MutableHeaders headers, MutableCookies cookies )
    {
        this.headers = headers;
        this.cookies = cookies;
    }

    @Override
    public OutcomeBuilder status( int status )
    {
        return new OutcomeBuilderInstance( status, headers, cookies );
    }

    @Override
    public OutcomeBuilder ok()
    {
        return new OutcomeBuilderInstance( OK.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder ok( String body )
    {
        return new OutcomeBuilderInstance( OK.code(), headers, cookies ).withBody( body );
    }

    @Override
    public OutcomeBuilder created()
    {
        return new OutcomeBuilderInstance( CREATED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder accepted()
    {
        return new OutcomeBuilderInstance( ACCEPTED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder nonAuthoritativeInformation()
    {
        return new OutcomeBuilderInstance( NON_AUTHORITATIVE_INFORMATION.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder noContent()
    {
        return new OutcomeBuilderInstance( NO_CONTENT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder resetContent()
    {
        return new OutcomeBuilderInstance( RESET_CONTENT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder partialContent()
    {
        return new OutcomeBuilderInstance( PARTIAL_CONTENT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder multiStatus()
    {
        return new OutcomeBuilderInstance( MULTI_STATUS.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder found( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), FOUND.code() );
    }

    @Override
    public OutcomeBuilder found( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, FOUND.code() );
    }

    @Override
    public OutcomeBuilder seeOther( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), SEE_OTHER.code() );
    }

    @Override
    public OutcomeBuilder seeOther( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, SEE_OTHER.code() );
    }

    @Override
    public OutcomeBuilder notModified()
    {
        return new OutcomeBuilderInstance( NOT_MODIFIED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder temporaryRedirect( String url )
    {
        return redirect( url, Collections.<String, List<String>>emptyMap(), TEMPORARY_REDIRECT.code() );
    }

    @Override
    public OutcomeBuilder temporaryRedirect( String url, Map<String, List<String>> queryString )
    {
        return redirect( url, queryString, TEMPORARY_REDIRECT.code() );
    }

    @Override
    public OutcomeBuilder badRequest()
    {
        return new OutcomeBuilderInstance( BAD_REQUEST.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder unauthorized()
    {
        return new OutcomeBuilderInstance( UNAUTHORIZED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder forbidden()
    {
        return new OutcomeBuilderInstance( FORBIDDEN.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder notFound()
    {
        return new OutcomeBuilderInstance( NOT_FOUND.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder methodNotAllowed()
    {
        return new OutcomeBuilderInstance( METHOD_NOT_ALLOWED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder notAcceptable()
    {
        return new OutcomeBuilderInstance( NOT_ACCEPTABLE.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder requestTimeout()
    {
        return new OutcomeBuilderInstance( REQUEST_TIMEOUT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder conflict()
    {
        return new OutcomeBuilderInstance( CONFLICT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder gone()
    {
        return new OutcomeBuilderInstance( GONE.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder preconditionFailed()
    {
        return new OutcomeBuilderInstance( PRECONDITION_FAILED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder requestEntityTooLarge()
    {
        return new OutcomeBuilderInstance( REQUEST_ENTITY_TOO_LARGE.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder requestUriTooLong()
    {
        return new OutcomeBuilderInstance( REQUEST_URI_TOO_LONG.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder unsupportedMediaType()
    {
        return new OutcomeBuilderInstance( UNSUPPORTED_MEDIA_TYPE.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder expectationFailed()
    {
        return new OutcomeBuilderInstance( EXPECTATION_FAILED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder unprocessableEntity()
    {
        return new OutcomeBuilderInstance( UNPROCESSABLE_ENTITY.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder locked()
    {
        return new OutcomeBuilderInstance( LOCKED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder failedDependency()
    {
        return new OutcomeBuilderInstance( FAILED_DEPENDENCY.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder tooManyRequest()
    {
        return new OutcomeBuilderInstance( 429, headers, cookies );
    }

    @Override
    public OutcomeBuilder internalServerError()
    {
        return new OutcomeBuilderInstance( INTERNAL_SERVER_ERROR.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder notImplemented()
    {
        return new OutcomeBuilderInstance( NOT_IMPLEMENTED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder badGateway()
    {
        return new OutcomeBuilderInstance( BAD_GATEWAY.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder serviceUnavailable()
    {
        return new OutcomeBuilderInstance( SERVICE_UNAVAILABLE.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder gatewayTimeout()
    {
        return new OutcomeBuilderInstance( GATEWAY_TIMEOUT.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder httpVersionNotSupported()
    {
        return new OutcomeBuilderInstance( HTTP_VERSION_NOT_SUPPORTED.code(), headers, cookies );
    }

    @Override
    public OutcomeBuilder insufficientStorage()
    {
        return new OutcomeBuilderInstance( INSUFFICIENT_STORAGE.code(), headers, cookies );
    }

    private OutcomeBuilder redirect( String url, Map<String, List<String>> queryString, int status )
    {
        return new OutcomeBuilderInstance( status, headers, cookies ).
            withHeader( LOCATION, URLs.appendQueryString( url, queryString ) );
    }
}
