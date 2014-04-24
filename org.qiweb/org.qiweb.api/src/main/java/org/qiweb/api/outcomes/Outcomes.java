/*
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
package org.qiweb.api.outcomes;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Outcomes fluent api.
 */
public interface Outcomes
{
    /**
     * @param status HTTP Status Code
     *
     * @return Builder for an Outcome of the given status
     */
    OutcomeBuilder status( int status );

    /**
     * @return Builder for a 200 OK Outcome
     */
    OutcomeBuilder ok();

    /**
     * @param body Response body as bytes
     *
     * @return Builder for a 200 OK Outcome
     */
    OutcomeBuilder ok( byte[] body );

    /**
     * @param body Response body as String using the default character encoding
     *
     * @return Builder for a 200 OK Outcome
     */
    OutcomeBuilder ok( String body );

    /**
     * @param body    Response body as String
     * @param charset Body charset
     *
     * @return Builder for a 200 OK Outcome
     */
    OutcomeBuilder ok( String body, Charset charset );

    /**
     * @return Builder for a 201 CREATED Outcome
     */
    OutcomeBuilder created();

    /**
     * @return Builder for a 202 ACCEPTED Outcome
     */
    OutcomeBuilder accepted();

    /**
     * @return Builder for a 203 NON_AUTHORITATIVE_INFORMATION Outcome
     */
    OutcomeBuilder nonAuthoritativeInformation();

    /**
     * @return Builder for a 204 NO_CONTENT Outcome
     */
    OutcomeBuilder noContent();

    /**
     * @return Builder for a 205 RESET_CONTENT Outcome
     */
    OutcomeBuilder resetContent();

    /**
     * @return Builder for a 206 PARTIAL_CONTENT Outcome
     */
    OutcomeBuilder partialContent();

    /**
     * @return Builder for a 207 MULTI_STATUS Outcome
     */
    OutcomeBuilder multiStatus();

    /**
     * @param url Redirect target
     *
     * @return Builder for a 302 FOUND Outcome
     */
    OutcomeBuilder found( String url );

    /**
     * @param url         Redirect target
     * @param queryString QueryString values
     *
     * @return Builder for a 302 FOUND Outcome
     */
    OutcomeBuilder found( String url, Map<String, List<String>> queryString );

    /**
     * @param url Redirect target
     *
     * @return Builder for a 303 SEE_OTHER Outcome
     */
    OutcomeBuilder seeOther( String url );

    /**
     * @param url         Redirect target
     * @param queryString QueryString values
     *
     * @return Builder for a 303 SEE_OTHER Outcome
     */
    OutcomeBuilder seeOther( String url, Map<String, List<String>> queryString );

    /**
     * @return Builder for a 304 NOT_MODIFIED Outcome
     */
    OutcomeBuilder notModified();

    /**
     * @param url Redirect target
     *
     * @return Builder for a 307 TEMPORARY_REDIRECT Outcome
     */
    OutcomeBuilder temporaryRedirect( String url );

    /**
     * @param url         Redirect target
     * @param queryString QueryString values
     *
     * @return Builder for a 307 TEMPORARY_REDIRECT Outcome
     */
    OutcomeBuilder temporaryRedirect( String url, Map<String, List<String>> queryString );

    /**
     * @return Builder for a 400 BAD_REQUEST Outcome
     */
    OutcomeBuilder badRequest();

    /**
     * @return Builder for a 401 UNAUTHORIZED Outcome
     */
    OutcomeBuilder unauthorized();

    /**
     * @return Builder for a 403 FORBIDDEN Outcome
     */
    OutcomeBuilder forbidden();

    /**
     * @return Builder for a 404 NOT_FOUND Outcome
     */
    OutcomeBuilder notFound();

    /**
     * @return Builder for a 405 METHOD_NOT_ALLOWED Outcome
     */
    OutcomeBuilder methodNotAllowed();

    /**
     * @return Builder for a 406 NOT_ACCEPTABLE Outcome
     */
    OutcomeBuilder notAcceptable();

    /**
     * @return Builder for a 408 REQUEST_TIMEOUT Outcome
     */
    OutcomeBuilder requestTimeout();

    /**
     * @return Builder for a 409 CONFLICT Outcome
     */
    OutcomeBuilder conflict();

    /**
     * @return Builder for a 410 GONE Outcome
     */
    OutcomeBuilder gone();

    /**
     * @return Builder for a 412 PRECONDITION_FAILED Outcome
     */
    OutcomeBuilder preconditionFailed();

    /**
     * @return Builder for a 413 REQUEST_ENTITY_TOO_LARGE Outcome
     */
    OutcomeBuilder requestEntityTooLarge();

    /**
     * @return Builder for a 414 REQUEST_URI_TOO_LONG Outcome
     */
    OutcomeBuilder requestUriTooLong();

    /**
     * @return Builder for a 415 UNSUPPORTED_MEDIA_TYPE Outcome
     */
    OutcomeBuilder unsupportedMediaType();

    /**
     * @return Builder for a 417 EXPECTATION_FAILED Outcome
     */
    OutcomeBuilder expectationFailed();

    /**
     * @return Builder for a 422 UNPROCESSABLE_ENTITY Outcome
     */
    OutcomeBuilder unprocessableEntity();

    /**
     * @return Builder for a 423 LOCKED Outcome
     */
    OutcomeBuilder locked();

    /**
     * @return Builder for a 424 FAILED_DEPENDENCY Outcome
     */
    OutcomeBuilder failedDependency();

    /**
     * @return Builder for a 429 TOO_MANY_REQUEST Outcome
     */
    OutcomeBuilder tooManyRequest();

    /**
     * @return Builder for a 500 INTERNAL_SERVER_ERROR Outcome
     */
    OutcomeBuilder internalServerError();

    /**
     * @return Builder for a 501 NOT_IMPLEMENTED Outcome
     */
    OutcomeBuilder notImplemented();

    /**
     * @return Builder for a 502 BAD_GATEWAY Outcome
     */
    OutcomeBuilder badGateway();

    /**
     * @return Builder for a 503 SERVICE_UNAVAILABLE Outcome
     */
    OutcomeBuilder serviceUnavailable();

    /**
     * @return Builder for a 504 GATEWAY_TIMEOUT Outcome
     */
    OutcomeBuilder gatewayTimeout();

    /**
     * @return Builder for a 505 HTTP_VERSION_NOT_SUPPORTED Outcome
     */
    OutcomeBuilder httpVersionNotSupported();

    /**
     * @return Builder for a 507 INSUFFICIENT_STORAGE Outcome
     */
    OutcomeBuilder insufficientStorage();
}
