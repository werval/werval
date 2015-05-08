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
package io.werval.api.context;

import java.util.concurrent.ExecutorService;

import io.werval.api.Application;
import io.werval.api.MetaData;
import io.werval.api.http.Request;
import io.werval.api.http.ResponseHeader;
import io.werval.api.http.Session;
import io.werval.api.outcomes.Outcomes;
import io.werval.api.routes.Route;

/**
 * HTTP Interaction Context.
 * <p>
 * Accessible using {@link CurrentContext#get()} and other static helpers in {@link CurrentContext}.
 *
 * @navassoc 1 - 1 Application
 * @navassoc 1 - 1 Session
 * @navassoc 1 - 1 Route
 * @navassoc 1 - 1 Outcomes
 * @navassoc 1 - 1 ExecutorService
 * @navcomposed 1 - 1 Request
 * @navcomposed 1 - 1 ResponseHeader
 * @navcomposed 1 - 1 MetaData
 */
public interface Context
{
    /**
     * @return Current Application
     */
    Application application();

    /**
     * @return Current Session
     */
    Session session();

    /**
     * @return Current Route
     */
    Route route();

    /**
     * @return Current Request
     */
    Request request();

    /**
     * @return Current Response Header
     */
    ResponseHeader response();

    /**
     * @return Current Outcome builder
     */
    Outcomes outcomes();

    /**
     * @return Current Context MetaData
     */
    MetaData metaData();

    /**
     * Current Executor.
     * <p>
     * Convey the current {@literal Context} to parallel threads.
     * <p>
     * Use when composing {@literal CompletableFutures} or to submit paralled {@literal Stream} operations.
     *
     * @return Current Executor
     */
    ExecutorService executor();
}
