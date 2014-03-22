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
package org.qiweb.spi.server;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper object for HttpServer implementations.
 */
public class HttpServerHelper
{
    private final String requestIdentityPrefix = UUID.randomUUID().toString() + "-";
    private final AtomicLong requestIdentityCount = new AtomicLong();

    /**
     * Generates a new request identity.
     *
     * Request identity has the following form: {@literal UUID-COUNT}.
     * <p>
     * One instance of {@link HttpServerHelper} holds a final {@link UUID} and an {@link AtomicLong} based counter.
     * <p>
     * Each call to this method concatenates the UUID and the counter, the later is incremented along the way.
     * 
     * @return a new request identity.
     */
    public String generateNewRequestIdentity()
    {
        return new StringBuilder( requestIdentityPrefix ).
            append( requestIdentityCount.getAndIncrement() ).toString();
    }
}
