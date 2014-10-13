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
package org.qiweb.api;

import java.util.List;

/**
 * Application errors.
 * <p>
 * Records errors happening in the application.
 * <p>
 * To prevent OOME when errors are frequent, it is possible to limit the amount of recorded errors with the
 * `qiweb.errors.max` configuration property. This default to 100.
 * <p>
 * Errors is an Iterable&lt;Error&gt;.
 * Implementations are required to provide predictible and sorted iteration starting with the last error and
 * ending with the oldest recorded error.
 */
public interface Errors
    extends Iterable<org.qiweb.api.Error>
{
    /**
     * Get a list of all Errors.
     *
     * @return Immutable list of all Errors.
     */
    List<Error> asList();

    /**
     * Record a new Error.
     *
     * @param requestId Request identitity
     * @param message   Error message
     * @param cause     Cause
     *
     * @return the recorded Error
     */
    Error record( String requestId, String message, Throwable cause );

    /**
     * @return Count of recorded errors
     */
    int count();

    /**
     * Clear recorded errors.
     */
    void clear();

    /**
     * Get an Error by its identity.
     *
     * @param errorId Error identity
     *
     * @return Error or null if not found
     */
    Error get( String errorId );

    /**
     * Get last recorded Error.
     *
     * @return Last recorded Error
     */
    Error last();

    /**
     * Get a List&lt;Error&gt; containing only Errors pertaining to a given request.
     *
     * @param requestId Request identity
     *
     * @return All recorded Errors pertaining to the given request
     */
    List<Error> ofRequest( String requestId );

    /**
     * Get last recorder error pertaining to a given request.
     *
     * @param requestIdentity Request identity
     *
     * @return Last recorded Error pertaining to the given request
     */
    Error lastOfRequest( String requestIdentity );
}
